package com.accounting.service.impl;

import com.accounting.common.BusinessException;
import com.accounting.dto.StockTradeRecordSaveDTO;
import com.accounting.entity.StockTradeConfig;
import com.accounting.entity.StockPosition;
import com.accounting.entity.StockTradeOperation;
import com.accounting.entity.StockTradeRecord;
import com.accounting.mapper.StockPositionMapper;
import com.accounting.mapper.StockTradeConfigMapper;
import com.accounting.mapper.StockTradeOperationMapper;
import com.accounting.mapper.StockTradeRecordMapper;
import com.accounting.service.StockTradeRecordService;
import com.accounting.vo.StockTradeOperationVO;
import com.accounting.vo.StockTradeRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 做T交易记录 Service 实现
 */
@Slf4j
@Service
public class StockTradeRecordServiceImpl implements StockTradeRecordService {

    @Autowired
    private StockTradeConfigMapper configMapper;

    @Autowired
    private StockTradeOperationMapper operationMapper;

    @Autowired
    private StockTradeRecordMapper recordMapper;

    @Autowired
    private StockPositionMapper positionMapper;

    @Override
    public List<StockTradeOperationVO> listOperations(Long configId) {
        LambdaQueryWrapper<StockTradeOperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTradeOperation::getConfigId, configId)
                .orderByAsc(StockTradeOperation::getDirection)
                .orderByAsc(StockTradeOperation::getLevelNo);
        List<StockTradeOperation> ops = operationMapper.selectList(wrapper);

        List<StockTradeOperationVO> voList = new ArrayList<>();
        for (StockTradeOperation op : ops) {
            StockTradeOperationVO vo = new StockTradeOperationVO();
            vo.setId(op.getId());
            vo.setConfigId(op.getConfigId());
            vo.setStockCode(op.getStockCode());
            vo.setLevelNo(op.getLevelNo());
            vo.setDirection(op.getDirection());
            vo.setLevelPrice(op.getLevelPrice());
            vo.setTriggered(op.getTriggered());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecord(StockTradeRecordSaveDTO dto) {
        // 标记当前档位为已触发，同时重置对方档位为未触发
        if (dto.getOperationId() != null) {
            StockTradeOperation op = new StockTradeOperation();
            op.setId(dto.getOperationId());
            op.setTriggered(1);
            operationMapper.updateById(op);

            StockTradeOperation currentOp = operationMapper.selectById(dto.getOperationId());
            if (currentOp != null) {
                // 对方方向：买入→重置卖出，卖出→重置买入
                int oppositeDirection = currentOp.getDirection() == 1 ? 2 : 1;
                LambdaQueryWrapper<StockTradeOperation> resetWrapper = new LambdaQueryWrapper<>();
                resetWrapper.eq(StockTradeOperation::getConfigId, dto.getConfigId())
                        .eq(StockTradeOperation::getDirection, oppositeDirection)
                        .eq(StockTradeOperation::getLevelNo, currentOp.getLevelNo());
                StockTradeOperation oppositeOp = operationMapper.selectOne(resetWrapper);
                if (oppositeOp != null) {
                    StockTradeOperation reset = new StockTradeOperation();
                    reset.setId(oppositeOp.getId());
                    reset.setTriggered(0);
                    operationMapper.updateById(reset);
                }
            }
        }

        StockTradeRecord record = new StockTradeRecord();
        record.setConfigId(dto.getConfigId());
        record.setOperationId(dto.getOperationId());
        record.setStockCode(getStockCode(dto.getConfigId()));
        record.setDirection(dto.getDirection());
        record.setShares(dto.getShares());
        record.setPrice(dto.getPrice());
        record.setReason(dto.getReason());
        // 解析交易时间，前端传入格式 "yyyy-MM-dd'T'HH:mm" 或 "yyyy-MM-dd HH:mm"
        try {
            String timeStr = dto.getTradeTime().replace("T", " ");
            if (timeStr.length() == 16) {
                timeStr += ":00";
            }
            record.setTradeTime(LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception e) {
            throw new BusinessException("交易时间格式错误");
        }
        recordMapper.insert(record);

        // 联动更新持仓股数和成本价
        updatePosition(record);
    }

    /**
     * 成交后联动更新 stock_position 表
     * 同花顺移动摊薄成本法：成本价 = (原总成本 ± 交易金额) / (原股数 ± 交易股数)
     * 买入累加，卖出扣减，盈亏分摊到剩余持仓
     */
    private void updatePosition(StockTradeRecord record) {
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockPosition::getStockCode, record.getStockCode());
        StockPosition position = positionMapper.selectOne(wrapper);

        BigDecimal oldTotalCost = position.getCostPrice().multiply(BigDecimal.valueOf(position.getShares()));
        BigDecimal tradeAmount = record.getPrice().multiply(BigDecimal.valueOf(record.getShares()));

        if (record.getDirection() == 1) {
            int newShares = position.getShares() + record.getShares();
            BigDecimal newCostPrice = oldTotalCost.add(tradeAmount)
                    .divide(BigDecimal.valueOf(newShares), 4, BigDecimal.ROUND_HALF_UP);

            StockPosition update = new StockPosition();
            update.setId(position.getId());
            update.setShares(newShares);
            update.setCostPrice(newCostPrice);
            positionMapper.updateById(update);
        } else {
            if (position.getShares() < record.getShares()) {
                throw new BusinessException("持仓不足，当前持有 " + position.getShares() + " 股，无法卖出 " + record.getShares() + " 股");
            }
            int newShares = position.getShares() - record.getShares();
            // 清仓时成本归零，避免除零
            BigDecimal newCostPrice = BigDecimal.ZERO;
            if (newShares > 0) {
                newCostPrice = oldTotalCost.subtract(tradeAmount)
                        .divide(BigDecimal.valueOf(newShares), 4, BigDecimal.ROUND_HALF_UP);
            }

            StockPosition update = new StockPosition();
            update.setId(position.getId());
            update.setShares(newShares);
            update.setCostPrice(newCostPrice);
            positionMapper.updateById(update);
        }
    }

    @Override
    public List<StockTradeRecordVO> listRecords(Long configId) {
        LambdaQueryWrapper<StockTradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTradeRecord::getConfigId, configId)
                .orderByAsc(StockTradeRecord::getTradeTime);
        List<StockTradeRecord> records = recordMapper.selectList(wrapper);

        // Entity 转 VO
        List<StockTradeRecordVO> voList = new ArrayList<>();
        for (StockTradeRecord r : records) {
            StockTradeRecordVO vo = new StockTradeRecordVO();
            vo.setId(r.getId());
            vo.setConfigId(r.getConfigId());
            vo.setOperationId(r.getOperationId());
            vo.setStockCode(r.getStockCode());
            vo.setDirection(r.getDirection());
            vo.setShares(r.getShares());
            vo.setPrice(r.getPrice());
            vo.setReason(r.getReason());
            vo.setTradeTime(r.getTradeTime());
            voList.add(vo);
        }

        // 实时 FIFO 计算配对盈亏
        calculatePairProfits(voList);
        return voList;
    }

    /**
     * FIFO 配对盈亏计算
     * 买卖队列按时间顺序逐一配对，正确消耗股数：
     *   买100 → 卖50（配对50股）→ 买剩余50 → 继续配下一笔卖
     * 每笔交易的 pairProfit 为它参与配对部分的盈亏
     * 未配对的交易 pairProfit 为 null
     */
    private void calculatePairProfits(List<StockTradeRecordVO> records) {
        List<StockTradeRecordVO> sells = records.stream()
                .filter(r -> r.getDirection() == 2)
                .collect(Collectors.toList());
        List<StockTradeRecordVO> buys = records.stream()
                .filter(r -> r.getDirection() == 1)
                .collect(Collectors.toList());

        int si = 0, bi = 0;
        int remainSell = 0, remainBuy = 0;

        while (si < sells.size() && bi < buys.size()) {
            StockTradeRecordVO sell = sells.get(si);
            StockTradeRecordVO buy = buys.get(bi);

            int sellShares = remainSell > 0 ? remainSell : sell.getShares();
            int buyShares = remainBuy > 0 ? remainBuy : buy.getShares();
            int paired = Math.min(sellShares, buyShares);

            BigDecimal pairProfit = sell.getPrice().subtract(buy.getPrice())
                    .multiply(BigDecimal.valueOf(paired));

            // 配对盈亏只记在卖出记录上，买入记录配对后标记为 0
            if (sell.getPairProfit() == null) {
                sell.setPairProfit(pairProfit);
            } else {
                sell.setPairProfit(sell.getPairProfit().add(pairProfit));
            }
            if (buy.getPairProfit() == null) {
                buy.setPairProfit(BigDecimal.ZERO);
            }

            // 消耗股数，移动指针
            if (sellShares > paired) {
                remainSell = sellShares - paired;
                remainBuy = 0;
                bi++;
            } else if (buyShares > paired) {
                remainBuy = buyShares - paired;
                remainSell = 0;
                si++;
            } else {
                remainSell = 0;
                remainBuy = 0;
                si++;
                bi++;
            }
        }
        // 未配对的 pairProfit 保持 null，前端显示"待匹配"
    }

    /**
     * 通过 configId 获取 stock_code
     */
    private String getStockCode(Long configId) {
        StockTradeConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException("做T规则不存在");
        }
        return config.getStockCode();
    }
}
