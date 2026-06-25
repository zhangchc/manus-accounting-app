package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.accounting.dto.StockPositionDTO;
import com.accounting.entity.CostHistory;
import com.accounting.entity.StockPosition;
import com.accounting.entity.TradeRecord;
import com.accounting.mapper.CostHistoryMapper;
import com.accounting.mapper.StockPositionMapper;
import com.accounting.mapper.TradeRecordMapper;
import com.accounting.service.StockPositionService;
import com.accounting.vo.CostHistoryVO;
import com.accounting.vo.StockPositionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockPositionServiceImpl implements StockPositionService {

    @Autowired
    private StockPositionMapper stockPositionMapper;

    @Autowired
    private CostHistoryMapper costHistoryMapper;

    @Autowired
    private TradeRecordMapper tradeRecordMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public StockPositionVO getPosition() {
        // 当前登录用户ID，用于查询该用户的持仓记录
        Long userId = (Long) request.getAttribute("userId");
        // 构建查询条件：按更新时间倒序取最新一条持仓
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockPosition::getUserId, userId)
                .orderByDesc(StockPosition::getUpdatedAt).last("LIMIT 1");
        // 数据库中的持仓实体（用户最近一条持股记录）
        StockPosition entity = stockPositionMapper.selectOne(wrapper);

        // 返回前端的持仓视图对象，持仓不存在时返回空对象（全字段null）
        StockPositionVO vo = new StockPositionVO();
        if (entity == null) {
            return vo;
        }

        vo.setId(entity.getId());
        vo.setStockName(entity.getStockName());
        vo.setStockCode(entity.getStockCode());
        vo.setCostPrice(entity.getCostPrice());
        vo.setShares(entity.getShares());
        vo.setNetInvestment(entity.getNetInvestment());
        if (entity.getUpdatedAt() != null) {
            vo.setUpdatedAt(entity.getUpdatedAt().toString().replace("T", " "));
        }

        // 持仓总成本 = 成本价 × 持股数（前端「持仓成本」卡片）
        BigDecimal totalCost = entity.getCostPrice().multiply(BigDecimal.valueOf(entity.getShares()));
        vo.setTotalCost(totalCost);

        try {
            // 从腾讯股票API获取的实时行情数据（含最新价、涨跌额、涨跌幅、昨收等）
            Map<String, Object> priceInfo = fetchStockPrice(entity.getStockCode());
            // 股票最新成交价（前端「实时行情」最新价）
            BigDecimal currentPrice = (BigDecimal) priceInfo.get("price");
            // 前一日收盘价，用于计算当日涨跌
            BigDecimal prevClose = (BigDecimal) priceInfo.get("prevClose");
            vo.setCurrentPrice(currentPrice);
            vo.setChange((BigDecimal) priceInfo.get("change"));
            vo.setChangePercent((String) priceInfo.get("changePercent"));

            // 当前市值 = 最新价 × 持股数（前端「当前市值」卡片）
            BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(entity.getShares()));
            vo.setCurrentValue(currentValue);

            // 浮动盈亏 = (最新价 - 成本价) × 持股数（前端「浮动盈亏」卡片）
            BigDecimal profitLoss = currentPrice.subtract(entity.getCostPrice())
                    .multiply(BigDecimal.valueOf(entity.getShares()));
            vo.setProfitLoss(profitLoss);

            if (entity.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                // 浮动盈亏百分比 = (最新价 - 成本价) / 成本价 × 100%（前端「盈亏比例」卡片）
                BigDecimal percent = currentPrice.subtract(entity.getCostPrice())
                        .divide(entity.getCostPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                vo.setProfitLossPercent(percent);
            }

            if (prevClose != null && prevClose.compareTo(BigDecimal.ZERO) > 0) {
                // 当日盈亏 = 收盘市值 + 净现金流 - 开盘市值（含日内买卖操作影响）
                int todaySellShares = 0;
                int todayBuyShares = 0;
                BigDecimal todaySellAmount = BigDecimal.ZERO;
                BigDecimal todayBuyAmount = BigDecimal.ZERO;

                // 查询当日该股票的交易记录
                java.time.LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                LambdaQueryWrapper<TradeRecord> tradeWrapper = new LambdaQueryWrapper<>();
                tradeWrapper.eq(TradeRecord::getUserId, userId)
                        .eq(TradeRecord::getStockCode, entity.getStockCode())
                        .ge(TradeRecord::getCreatedAt, todayStart);
                List<TradeRecord> todayTrades = tradeRecordMapper.selectList(tradeWrapper);

                for (TradeRecord t : todayTrades) {
                    BigDecimal tradeCash = t.getTradePrice().multiply(BigDecimal.valueOf(t.getShares()));
                    if ("SELL".equals(t.getTradeType())) {
                        todaySellShares += t.getShares();
                        todaySellAmount = todaySellAmount.add(tradeCash);
                    } else if ("BUY".equals(t.getTradeType())) {
                        todayBuyShares += t.getShares();
                        todayBuyAmount = todayBuyAmount.add(tradeCash);
                    }
                }

                // 起始股数 = 当前持股 - 今日买入 + 今日卖出
                int startShares = entity.getShares() - todayBuyShares + todaySellShares;
                BigDecimal startValue = prevClose.multiply(BigDecimal.valueOf(startShares));
                BigDecimal netCashFlow = todaySellAmount.subtract(todayBuyAmount);
                // 当日盈亏 = 当前市值 + 净现金流 - 开盘市值
                BigDecimal dailyPl = currentValue.add(netCashFlow).subtract(startValue);
                vo.setDailyProfitLoss(dailyPl);
            }
        } catch (Exception e) {
            // 获取股价失败时，不设置现价相关字段
        }

        return vo;
    }

    @Override
    @Transactional
    public StockPositionVO saveOrUpdate(StockPositionDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockPosition::getUserId, userId)
                .orderByDesc(StockPosition::getUpdatedAt).last("LIMIT 1");
        StockPosition existing = stockPositionMapper.selectOne(wrapper);

        StockPosition entity;
        if (existing != null) {
            entity = existing;
        } else {
            entity = new StockPosition();
        }

        entity.setUserId(userId);
        entity.setStockName(dto.getStockName());
        entity.setStockCode(dto.getStockCode());
        entity.setCostPrice(dto.getCostPrice());
        entity.setShares(dto.getShares());

        if (existing != null) {
            // 存量数据兼容：netInvestment 为 0 时从当前成本和股数初始化
            if (entity.getNetInvestment() == null || entity.getNetInvestment().compareTo(BigDecimal.ZERO) <= 0) {
                entity.setNetInvestment(entity.getCostPrice().multiply(BigDecimal.valueOf(entity.getShares())));
            }
            stockPositionMapper.updateById(entity);
        } else {
            // 新建时初始化累计净投入 = 成本价 × 持股数
            entity.setNetInvestment(dto.getCostPrice().multiply(BigDecimal.valueOf(dto.getShares())));
            stockPositionMapper.insert(entity);
        }

        return getPosition();
    }

    @Override
    public StockPosition getCurrentEntity() {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockPosition::getUserId, userId)
                .orderByDesc(StockPosition::getUpdatedAt).last("LIMIT 1");
        return stockPositionMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void updateAfterTrade(String tradeType, Integer tradeShares, BigDecimal tradePrice, BigDecimal currentPrice, Long tradeRecordId) {
        // 获取当前用户最新持仓，执行摊薄法更新
        StockPosition position = getCurrentEntity();
        if (position == null) {
            return;
        }

        // 存量数据兼容：netInvestment 未初始化时，从当前成本价和股数推算
        if (position.getNetInvestment() == null || position.getNetInvestment().compareTo(BigDecimal.ZERO) <= 0) {
            position.setNetInvestment(position.getCostPrice().multiply(BigDecimal.valueOf(position.getShares())));
        }

        BigDecimal tradeCash = tradePrice.multiply(BigDecimal.valueOf(tradeShares));
        if ("BUY".equals(tradeType)) {
            // 买入：追加净投入，增加持股
            position.setShares(position.getShares() + tradeShares);
            position.setNetInvestment(position.getNetInvestment().add(tradeCash));
        } else {
            // 卖出：回收现金，减少持股
            position.setShares(position.getShares() - tradeShares);
            position.setNetInvestment(position.getNetInvestment().subtract(tradeCash));
        }

        // 摊薄法回本价 = 累计净投入 / 当前持股数
        if (position.getShares() > 0) {
            position.setCostPrice(position.getNetInvestment()
                    .divide(BigDecimal.valueOf(position.getShares()), 4, RoundingMode.HALF_UP));
        }
        stockPositionMapper.updateById(position);

        // 插入成本历史记录
        CostHistory history = new CostHistory();
        history.setUserId((Long) request.getAttribute("userId"));
        history.setStockCode(position.getStockCode());
        history.setStockName(position.getStockName());
        history.setShares(position.getShares());
        history.setCostPrice(position.getCostPrice());
        history.setNetInvestment(position.getNetInvestment());
        history.setCurrentPrice(currentPrice);
        history.setTradeType(tradeType);
        history.setTradeRecordId(tradeRecordId);
        costHistoryMapper.insert(history);
    }

    @Override
    public List<CostHistoryVO> getCostHistory() {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<CostHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostHistory::getUserId, userId)
                .orderByAsc(CostHistory::getCreatedAt);
        List<CostHistory> list = costHistoryMapper.selectList(wrapper);

        return list.stream().map(entity -> {
            CostHistoryVO vo = new CostHistoryVO();
            BeanUtil.copyProperties(entity, vo);
            if (entity.getCreatedAt() != null) {
                vo.setCreatedAt(entity.getCreatedAt().toString().replace("T", " "));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStockPrice(String code) {
        if (StrUtil.isBlank(code)) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("price", BigDecimal.ZERO);
            return empty;
        }
        return fetchStockPrice(code);
    }

    private Map<String, Object> fetchStockPrice(String code) {
        String url = "https://qt.gtimg.cn/q=" + code;
        String response = HttpUtil.createGet(url).charset("GBK").execute().body();

        if (StrUtil.isBlank(response)) {
            throw new RuntimeException("获取股票行情失败");
        }

        String[] parts = response.split("~");
        if (parts.length < 33) {
            throw new RuntimeException("解析股票行情数据失败");
        }

        BigDecimal currentPrice = new BigDecimal(parts[3]);
        BigDecimal prevClose = new BigDecimal(parts[4]);
        BigDecimal changeAmount = new BigDecimal(parts[31]);
        String changePercentStr = parts[32];

        Map<String, Object> result = new HashMap<>();
        result.put("name", parts[1]);
        result.put("price", currentPrice);
        result.put("prevClose", prevClose);
        result.put("change", changeAmount);
        result.put("changePercent", changePercentStr);
        return result;
    }
}
