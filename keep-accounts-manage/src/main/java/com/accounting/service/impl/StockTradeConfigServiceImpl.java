package com.accounting.service.impl;

import com.accounting.common.BusinessException;
import com.accounting.dto.StockTradeConfigSaveDTO;
import com.accounting.entity.StockTradeConfig;
import com.accounting.entity.StockTradeOperation;
import com.accounting.entity.StockTradeRecord;
import com.accounting.mapper.StockTradeConfigMapper;
import com.accounting.mapper.StockTradeOperationMapper;
import com.accounting.mapper.StockTradeRecordMapper;
import com.accounting.service.StockTradeConfigService;
import com.accounting.vo.StockTradeConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 做T管理规则 Service 实现
 */
@Slf4j
@Service
public class StockTradeConfigServiceImpl implements StockTradeConfigService {

    @Autowired
    private StockTradeConfigMapper configMapper;

    @Autowired
    private StockTradeOperationMapper operationMapper;

    @Autowired
    private StockTradeRecordMapper recordMapper;

    @Override
    public List<StockTradeConfigVO> list() {
        List<StockTradeConfig> configs = configMapper.selectList(null);
        List<StockTradeConfigVO> voList = new ArrayList<>();

        for (StockTradeConfig config : configs) {
            StockTradeConfigVO vo = toVO(config);
            // 统计当前规则下的交易数据
            fillTradeStats(vo, config.getId());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public StockTradeConfigVO queryByStockCode(String stockCode) {
        LambdaQueryWrapper<StockTradeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTradeConfig::getStockCode, stockCode);
        StockTradeConfig config = configMapper.selectOne(wrapper);
        if (config == null) {
            return null;
        }
        StockTradeConfigVO vo = toVO(config);
        fillTradeStats(vo, config.getId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(StockTradeConfigSaveDTO dto) {
        // 检查 stock_code 是否已有规则
        LambdaQueryWrapper<StockTradeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTradeConfig::getStockCode, dto.getStockCode());
        StockTradeConfig existing = configMapper.selectOne(wrapper);

        Long configId;
        if (existing != null) {
            // 已存在：更新规则行（保留 id），删除旧档位
            configId = existing.getId();
            StockTradeConfig update = new StockTradeConfig();
            update.setId(configId);
            update.setStockName(dto.getStockName());
            update.setBasePrice(dto.getBasePrice());
            update.setLevels(dto.getLevels());
            update.setUpPct(dto.getUpPct());
            update.setDownPct(dto.getDownPct());
            update.setFixedShares(dto.getFixedShares());
            update.setActive(dto.getActive() != null ? dto.getActive() : 1);
            configMapper.updateById(update);

            // 删除旧档位行
            LambdaQueryWrapper<StockTradeOperation> opWrapper = new LambdaQueryWrapper<>();
            opWrapper.eq(StockTradeOperation::getConfigId, configId);
            operationMapper.delete(opWrapper);
        } else {
            // 新增
            StockTradeConfig entity = new StockTradeConfig();
            entity.setStockCode(dto.getStockCode());
            entity.setStockName(dto.getStockName());
            entity.setBasePrice(dto.getBasePrice());
            entity.setLevels(dto.getLevels());
            entity.setUpPct(dto.getUpPct());
            entity.setDownPct(dto.getDownPct());
            entity.setFixedShares(dto.getFixedShares());
            entity.setActive(dto.getActive() != null ? dto.getActive() : 1);
            configMapper.insert(entity);
            configId = entity.getId();
        }

        // 生成新档位行并批量插入
        List<StockTradeOperation> operations = generateOperations(configId, dto.getStockCode(),
                dto.getBasePrice(), dto.getLevels(), dto.getUpPct(), dto.getDownPct());
        for (StockTradeOperation op : operations) {
            operationMapper.insert(op);
        }
    }

    /**
     * 根据规则参数生成网格档位行
     * 卖出档位：每档在前一档基础上累乘 (1 + upPct/100)
     * 买入档位：每档在前一档基础上累乘 (1 − downPct/100)
     */
    private List<StockTradeOperation> generateOperations(Long configId, String stockCode,
            BigDecimal basePrice, Integer levels, BigDecimal upPct, BigDecimal downPct) {
        List<StockTradeOperation> ops = new ArrayList<>();

        BigDecimal upMultiplier = BigDecimal.ONE.add(
                upPct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal downMultiplier = BigDecimal.ONE.subtract(
                downPct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));

        // 卖出档位（direction=2），每档在前一档基础上累乘
        BigDecimal sellPrice = basePrice;
        for (int i = 1; i <= levels; i++) {
            sellPrice = sellPrice.multiply(upMultiplier).setScale(2, RoundingMode.HALF_UP);
            StockTradeOperation op = new StockTradeOperation();
            op.setConfigId(configId);
            op.setStockCode(stockCode);
            op.setLevelNo(i);
            op.setDirection(2);
            op.setLevelPrice(sellPrice);
            op.setTriggered(0);
            ops.add(op);
        }

        // 买入档位（direction=1），每档在前一档基础上累乘
        BigDecimal buyPrice = basePrice;
        for (int i = 1; i <= levels; i++) {
            buyPrice = buyPrice.multiply(downMultiplier).setScale(2, RoundingMode.HALF_UP);
            StockTradeOperation op = new StockTradeOperation();
            op.setConfigId(configId);
            op.setStockCode(stockCode);
            op.setLevelNo(i);
            op.setDirection(1);
            op.setLevelPrice(buyPrice);
            op.setTriggered(0);
            ops.add(op);
        }

        return ops;
    }

    /**
     * 填充规则的交易统计（买卖次数、FIFO配对盈亏）
     * totalPnl 只计入已配对（买+卖闭环）部分的盈亏，单独的买入或卖出不计入
     */
    private void fillTradeStats(StockTradeConfigVO vo, Long configId) {
        LambdaQueryWrapper<StockTradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTradeRecord::getConfigId, configId)
                .orderByAsc(StockTradeRecord::getTradeTime);
        List<StockTradeRecord> records = recordMapper.selectList(wrapper);

        int sellCount = 0, buyCount = 0;
        for (StockTradeRecord r : records) {
            if (r.getDirection() == null) {
                continue;
            }
            if (r.getDirection() == 2) {
                sellCount++;
            } else {
                buyCount++;
            }
        }

        // FIFO 配对计算盈亏，与 listRecords 的 calculatePairProfits 逻辑一致
        BigDecimal totalPnl = calculateTotalPnlByFifo(records);

        vo.setSellCount(sellCount);
        vo.setBuyCount(buyCount);
        vo.setTotalPnl(totalPnl);
    }

    /**
     * FIFO 配对计算总盈亏，只计入已闭环的买卖配对盈亏
     */
    private BigDecimal calculateTotalPnlByFifo(List<StockTradeRecord> records) {
        List<StockTradeRecord> sells = new ArrayList<>();
        List<StockTradeRecord> buys = new ArrayList<>();
        for (StockTradeRecord r : records) {
            if (r.getDirection() == null || r.getPrice() == null || r.getShares() == null) {
                continue;
            }
            if (r.getDirection() == 2) {
                sells.add(r);
            } else {
                buys.add(r);
            }
        }

        int si = 0, bi = 0;
        int remainSell = 0, remainBuy = 0;
        BigDecimal totalPnl = BigDecimal.ZERO;

        while (si < sells.size() && bi < buys.size()) {
            StockTradeRecord sell = sells.get(si);
            StockTradeRecord buy = buys.get(bi);

            int sellShares = remainSell > 0 ? remainSell : sell.getShares();
            int buyShares = remainBuy > 0 ? remainBuy : buy.getShares();
            int paired = Math.min(sellShares, buyShares);

            BigDecimal pairProfit = sell.getPrice().subtract(buy.getPrice())
                    .multiply(BigDecimal.valueOf(paired));
            totalPnl = totalPnl.add(pairProfit);

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
        return totalPnl;
    }

    private StockTradeConfigVO toVO(StockTradeConfig config) {
        StockTradeConfigVO vo = new StockTradeConfigVO();
        vo.setId(config.getId());
        vo.setStockCode(config.getStockCode());
        vo.setStockName(config.getStockName());
        vo.setBasePrice(config.getBasePrice());
        vo.setLevels(config.getLevels());
        vo.setUpPct(config.getUpPct());
        vo.setDownPct(config.getDownPct());
        vo.setFixedShares(config.getFixedShares());
        vo.setActive(config.getActive());
        return vo;
    }
}
