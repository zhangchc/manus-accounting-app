package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.accounting.common.BusinessException;
import com.accounting.dto.TradeRecordDTO;
import com.accounting.dto.TradeStrategyDTO;
import com.accounting.entity.StockPosition;
import com.accounting.entity.TradeRecord;
import com.accounting.entity.TradeStrategy;
import com.accounting.mapper.TradeRecordMapper;
import com.accounting.mapper.TradeStrategyMapper;
import com.accounting.service.StockPositionService;
import com.accounting.service.TradeService;
import com.accounting.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private TradeStrategyMapper strategyMapper;

    @Autowired
    private TradeRecordMapper recordMapper;

    @Autowired
    private StockPositionService stockPositionService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public TradeStrategyVO getStrategy() {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<TradeStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeStrategy::getUserId, userId)
                .orderByDesc(TradeStrategy::getUpdatedAt).last("LIMIT 1");
        TradeStrategy entity = strategyMapper.selectOne(wrapper);

        TradeStrategyVO vo = new TradeStrategyVO();
        if (entity != null) {
            BeanUtil.copyProperties(entity, vo);
            if (entity.getUpdatedAt() != null) {
                vo.setUpdatedAt(entity.getUpdatedAt().toString().replace("T", " "));
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public TradeStrategyVO saveStrategy(TradeStrategyDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<TradeStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeStrategy::getUserId, userId)
                .orderByDesc(TradeStrategy::getUpdatedAt).last("LIMIT 1");
        TradeStrategy existing = strategyMapper.selectOne(wrapper);

        TradeStrategy entity;
        if (existing != null) {
            entity = existing;
        } else {
            entity = new TradeStrategy();
            entity.setStockName("常山药业");
            entity.setStockCode("sz300255");
            entity.setStatus("ACTIVE");
            entity.setSellCount(0);
            entity.setBuyCount(0);
        }

        entity.setUserId(userId);
        entity.setBasePrice(dto.getBasePrice());
        entity.setSellShares(dto.getSellShares());
        entity.setBuyShares(dto.getBuyShares());
        entity.setMaxSellCount(dto.getMaxSellCount());
        entity.setMaxBuyCount(dto.getMaxBuyCount());
        entity.setAlertWarningPrice(dto.getAlertWarningPrice());
        entity.setAlertCriticalPrice(dto.getAlertCriticalPrice());

        if (existing != null) {
            strategyMapper.updateById(entity);
        } else {
            strategyMapper.insert(entity);
        }

        TradeStrategyVO vo = new TradeStrategyVO();
        BeanUtil.copyProperties(entity, vo);
        if (entity.getUpdatedAt() != null) {
            vo.setUpdatedAt(entity.getUpdatedAt().toString().replace("T", " "));
        }
        return vo;
    }

    @Override
    public TradePrecheckVO precheck(String type, BigDecimal currentPrice) {
        TradeStrategy strategy = getStrategyEntity();
        if (strategy == null) {
            throw new BusinessException("请先配置做T策略");
        }

        int[] counts = getUnmatchedCounts(strategy.getId());
        TradePrecheckVO vo = new TradePrecheckVO();
        String trend = detectTrend(strategy, type, currentPrice);

        if ("SELL".equalsIgnoreCase(type)) {
            buildSellPrecheck(vo, strategy, counts, trend);
        } else {
            buildBuyPrecheck(vo, strategy, counts, trend);
        }

        return vo;
    }

    private void buildSellPrecheck(TradePrecheckVO vo, TradeStrategy strategy, int[] counts, String trend) {
        int unmatchedSells = counts[0];
        int unmatchedBuys = counts[1];
        int totalUnmatchedSellShares = counts[2];
        int nextSellNo = unmatchedSells + 1;
        int maxSell = strategy.getMaxSellCount();
        vo.setNextSellNo(nextSellNo);
        vo.setNextBuyNo(unmatchedBuys + 1);

        // 从持仓表读取当前实时持股数
        StockPosition position = stockPositionService.getCurrentEntity();
        int currentHolding = position != null ? position.getShares() : 0;
        int holdingAfterSell = currentHolding - strategy.getSellShares();
        vo.setCurrentHolding(currentHolding);
        vo.setHoldingAfterOp(holdingAfterSell);

        // 剩余底仓 = 当前持仓 - 已卖出股数（来自实际记录） - 剩余计划卖出次数 × 每股卖出股数
        int remainingSellSlots = Math.max(0, maxSell - unmatchedSells);
        int remainingBase = Math.max(0, currentHolding - totalUnmatchedSellShares - remainingSellSlots * strategy.getSellShares());
        boolean isRising = "UP".equals(trend);

        if (nextSellNo <= maxSell) {
            if (nextSellNo == maxSell) {
                vo.setOpLevel("BOUNDARY");
                vo.setReasonRequired(true);
                if (isRising) {
                    vo.setScenario("末次卖出-上涨中");
                    vo.setWarning("计划内最后一次卖出！当前股价处于上涨趋势，卖出后剩余底仓"
                            + remainingBase + "股。如果股价继续上涨将踏空，无法继续跟随趋势获利。请确认操作理由。");
                    vo.setSuggestedReasons(Arrays.asList(
                            "股价已达目标，按计划完成最后一次卖出，保留底仓应对持续上涨",
                            "锁定利润但担心趋势反转，先卖出观望",
                            "分批止盈，保护已有利润"
                    ));
                } else {
                    vo.setScenario("末次卖出");
                    vo.setWarning("计划内最后一次卖出！确认后累计卖出"
                            + (nextSellNo * strategy.getSellShares())
                            + "股，剩余" + remainingBase + "股底仓。请确认操作理由。");
                    vo.setSuggestedReasons(Arrays.asList(
                            "按计划完成最后一次卖出，等待回补机会",
                            "先卖出后等待更低价格回补"
                    ));
                }
            } else {
                vo.setOpLevel("NORMAL");
                vo.setReasonRequired(false);
                vo.setWarning("");
                if (isRising) {
                    vo.setScenario("计划内卖出-上涨中");
                    vo.setSuggestedReasons(Arrays.asList(
                            "股价持续上涨，按计划分批卖出",
                            "达到目标价位，执行卖出计划"
                    ));
                } else {
                    vo.setScenario("计划内卖出");
                    vo.setSuggestedReasons(Arrays.asList(
                            "按计划执行卖出"
                    ));
                }
            }
        } else {
            vo.setOpLevel("OVERLIMIT");
            vo.setReasonRequired(true);
            if (isRising) {
                vo.setScenario("超限卖出-上涨中");
                vo.setWarning("已超出计划卖出次数！当前股价仍在上涨，继续卖出将减少底仓至"
                        + remainingBase + "股以下。继续上涨将造成严重踏空，损失底仓筹码。请务必确认操作理由！");
                vo.setSuggestedReasons(Arrays.asList(
                        "股价强势上涨但已达目标，超限卖出锁定部分利润",
                        "担心趋势即将反转，提前获利了结",
                        "调整持仓策略，主动降低仓位比例"
                ));
            } else {
                vo.setScenario("超限卖出");
                vo.setWarning("已超出计划卖出次数！继续卖出将减少底仓至"
                        + remainingBase + "股以下。请务必谨慎操作并填写理由！");
                vo.setSuggestedReasons(Arrays.asList(
                        "股价持续下跌，减仓控制风险",
                        "调整策略，降低持仓比例"
                ));
            }
        }
    }

    private void buildBuyPrecheck(TradePrecheckVO vo, TradeStrategy strategy, int[] counts, String trend) {
        int unmatchedSells = counts[0];
        int unmatchedBuys = counts[1];
        int nextBuyNo = unmatchedBuys + 1;
        int maxBuy = strategy.getMaxBuyCount();
        vo.setNextSellNo(unmatchedSells + 1);
        vo.setNextBuyNo(nextBuyNo);

        // 从持仓表读取当前实时持股数
        StockPosition position = stockPositionService.getCurrentEntity();
        int currentHolding = position != null ? position.getShares() : 0;
        vo.setCurrentHolding(currentHolding);
        vo.setHoldingAfterOp(currentHolding + strategy.getBuyShares());

        boolean isFalling = "DOWN".equals(trend);

        if (nextBuyNo <= maxBuy) {
            if (nextBuyNo == maxBuy) {
                vo.setOpLevel("BOUNDARY");
                vo.setReasonRequired(true);
                if (isFalling) {
                    vo.setScenario("末次买入-下跌中");
                    vo.setWarning("计划内最后一次买入！当前股价持续下跌，此次买入后现金筹码将耗尽，"
                            + "无法继续补仓摊薄成本。如果股价继续下跌将无筹码可用。请确认有足够资金并填写操作理由。");
                    vo.setSuggestedReasons(Arrays.asList(
                            "股价持续下跌，最后一次补仓摊薄持仓成本",
                            "用完计划筹码额度，等待市场反弹",
                            "看好长期价值，按计划完成最后一笔买入"
                    ));
                } else {
                    vo.setScenario("末次买入");
                    vo.setWarning("计划内最后一次买入！此次买入后现金筹码将耗尽。请确认有足够资金并填写操作理由。");
                    vo.setSuggestedReasons(Arrays.asList(
                            "按计划完成最后一次买入",
                            "用完计划筹码，等待后续走势"
                    ));
                }
            } else {
                vo.setOpLevel("NORMAL");
                vo.setReasonRequired(false);
                vo.setWarning("");
                if (isFalling) {
                    vo.setScenario("计划内买入-下跌中");
                    vo.setSuggestedReasons(Arrays.asList(
                            "股价回落至目标回补价，按计划买入",
                            "股价下跌，按计划执行回补操作"
                    ));
                } else {
                    vo.setScenario("计划内买入");
                    vo.setSuggestedReasons(Arrays.asList(
                            "按计划执行买入"
                    ));
                }
            }
        } else {
            vo.setOpLevel("OVERLIMIT");
            vo.setReasonRequired(true);
            if (isFalling) {
                vo.setScenario("超限买入-下跌中");
                vo.setWarning("已超出计划买入次数！当前股价仍在下跌，继续买入需要额外资金。"
                        + "现金筹码可能已耗尽，继续追加将承担更大风险。请务必确认操作理由！");
                vo.setSuggestedReasons(Arrays.asList(
                        "股价超跌但看好长期价值，追加买入摊薄成本",
                        "调整策略，增加持仓比例等待反弹",
                        "市场恐慌性下跌，逆势加仓"
                ));
            } else {
                vo.setScenario("超限买入");
                vo.setWarning("已超出计划买入次数！继续买入需要额外资金。请确认有足够现金并填写操作理由！");
                vo.setSuggestedReasons(Arrays.asList(
                        "看好后市，追加投资",
                        "调整策略，增加持仓"
                ));
            }
        }
    }

    private String detectTrend(TradeStrategy strategy, String type, BigDecimal currentPrice) {
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        LambdaQueryWrapper<TradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeRecord::getStrategyId, strategy.getId())
                .eq(TradeRecord::getTradeType, type.toUpperCase())
                .orderByDesc(TradeRecord::getCreatedAt)
                .last("LIMIT 1");
        TradeRecord lastTrade = recordMapper.selectOne(wrapper);
        if (lastTrade != null) {
            int cmp = currentPrice.compareTo(lastTrade.getTradePrice());
            if (cmp > 0) return "UP";
            if (cmp < 0) return "DOWN";
            return "STABLE";
        }
        int cmp = currentPrice.compareTo(strategy.getBasePrice());
        if (cmp > 0) return "UP";
        if (cmp < 0) return "DOWN";
        return "STABLE";
    }

    @Override
    @Transactional
    public TradeRecordVO sell(TradeRecordDTO dto) {
        TradeStrategy strategy = getStrategyEntity();
        if (strategy == null) {
            throw new BusinessException("请先配置做T策略");
        }

        int[] counts = getUnmatchedCounts(strategy.getId());
        int unmatchedSells = counts[0];
        int unmatchedBuys = counts[1];
        int nextSellNo = strategy.getSellCount() + 1;
        int nextUnmatchedSell = unmatchedSells + 1;

        String opLevel;
        if (nextUnmatchedSell <= strategy.getMaxSellCount()) {
            opLevel = nextUnmatchedSell == strategy.getMaxSellCount() ? "BOUNDARY" : "NORMAL";
        } else {
            opLevel = "OVERLIMIT";
        }

        boolean reasonRequired = "BOUNDARY".equals(opLevel) || "OVERLIMIT".equals(opLevel);
        if (reasonRequired && StrUtil.isBlank(dto.getReason())) {
            throw new BusinessException("第" + nextUnmatchedSell + "次卖出为" + ("BOUNDARY".equals(opLevel) ? "边界" : "超限") + "操作，必须填写操作理由");
        }

        // 校验当前持仓是否足够
        StockPosition position = stockPositionService.getCurrentEntity();
        if (position == null || position.getShares() < dto.getShares()) {
            throw new BusinessException("当前持仓不足，无法卖出 " + dto.getShares() + " 股");
        }

        TradeRecord record = new TradeRecord();
        record.setUserId((Long) request.getAttribute("userId"));
        record.setStrategyId(strategy.getId());
        record.setStockName(strategy.getStockName());
        record.setStockCode(strategy.getStockCode());
        record.setTradeType("SELL");
        record.setTradePrice(dto.getTradePrice());
        record.setShares(dto.getShares());
        record.setSellNo(nextSellNo);
        record.setOpLevel(opLevel);
        record.setBackBuyPrice(dto.getTradePrice().divide(new BigDecimal("1.05"), 2, RoundingMode.HALF_UP));
        record.setScenario(determineScenario(strategy, "SELL", opLevel, dto.getTradePrice()));
        record.setReason(dto.getReason() != null ? dto.getReason() : "");
        record.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        // 计算卖出后持仓（position 已在上面校验环节读取）
        int currentHolding = position.getShares() - dto.getShares();
        record.setCurrentHolding(currentHolding);

        recordMapper.insert(record);

        // 摊薄法更新持仓：卖出回笼资金，减少持股
        stockPositionService.updateAfterTrade("SELL", dto.getShares(), dto.getTradePrice(), dto.getTradePrice(), record.getId());

        strategy.setSellCount(strategy.getSellCount() + 1);
        strategyMapper.updateById(strategy);

        return toRecordVO(record);
    }

    @Override
    @Transactional
    public TradeRecordVO buy(TradeRecordDTO dto) {
        TradeStrategy strategy = getStrategyEntity();
        if (strategy == null) {
            throw new BusinessException("请先配置做T策略");
        }

        int[] counts = getUnmatchedCounts(strategy.getId());
        int unmatchedSells = counts[0];
        int unmatchedBuys = counts[1];
        int nextBuyNo = strategy.getBuyCount() + 1;
        int nextUnmatchedBuy = unmatchedBuys + 1;

        String opLevel;
        if (nextUnmatchedBuy <= strategy.getMaxBuyCount()) {
            opLevel = nextUnmatchedBuy == strategy.getMaxBuyCount() ? "BOUNDARY" : "NORMAL";
        } else {
            opLevel = "OVERLIMIT";
        }

        boolean reasonRequired = "BOUNDARY".equals(opLevel) || "OVERLIMIT".equals(opLevel);
        if (reasonRequired && StrUtil.isBlank(dto.getReason())) {
            throw new BusinessException("第" + nextUnmatchedBuy + "次买入为" + ("BOUNDARY".equals(opLevel) ? "边界" : "超限") + "操作，必须填写操作理由");
        }

        // 查找最新的未配对卖出记录，配对买入回补
        List<TradeRecord> allSells = recordMapper.selectList(
                new LambdaQueryWrapper<TradeRecord>()
                        .eq(TradeRecord::getStrategyId, strategy.getId())
                        .eq(TradeRecord::getTradeType, "SELL")
                        .orderByDesc(TradeRecord::getCreatedAt)
        );

        List<TradeRecord> allBuys = recordMapper.selectList(
                new LambdaQueryWrapper<TradeRecord>()
                        .eq(TradeRecord::getStrategyId, strategy.getId())
                        .eq(TradeRecord::getTradeType, "BUY")
        );

        Set<Long> matchedSellIds = new HashSet<>();
        for (TradeRecord buy : allBuys) {
            if (buy.getMatchedSellId() != null) {
                matchedSellIds.add(buy.getMatchedSellId());
            }
        }

        TradeRecord matchedSell = null;
        for (TradeRecord sell : allSells) {
            if (!matchedSellIds.contains(sell.getId())) {
                matchedSell = sell;
                break;
            }
        }

        TradeRecord record = new TradeRecord();
        record.setUserId((Long) request.getAttribute("userId"));
        record.setStrategyId(strategy.getId());
        record.setStockName(strategy.getStockName());
        record.setStockCode(strategy.getStockCode());
        record.setTradeType("BUY");
        record.setTradePrice(dto.getTradePrice());
        record.setShares(dto.getShares());
        record.setBuyNo(nextBuyNo);
        record.setOpLevel(opLevel);
        record.setScenario(determineScenario(strategy, "BUY", opLevel, dto.getTradePrice()));
        record.setReason(dto.getReason() != null ? dto.getReason() : "");
        record.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        if (matchedSell != null) {
            record.setMatchedSellId(matchedSell.getId());
            BigDecimal profit = matchedSell.getTradePrice().subtract(dto.getTradePrice())
                    .multiply(BigDecimal.valueOf(dto.getShares()))
                    .setScale(2, RoundingMode.HALF_UP);
            record.setProfit(profit);
        }

        // 从持仓表读取当前实时持股数，计算买入后持仓
        StockPosition position = stockPositionService.getCurrentEntity();
        int currentHolding = position != null ? position.getShares() + dto.getShares() : 0;
        record.setCurrentHolding(currentHolding);

        recordMapper.insert(record);

        // 摊薄法更新持仓：买入追加现金，增加持股
        stockPositionService.updateAfterTrade("BUY", dto.getShares(), dto.getTradePrice(), dto.getTradePrice(), record.getId());

        strategy.setBuyCount(strategy.getBuyCount() + 1);
        strategyMapper.updateById(strategy);

        return toRecordVO(record);
    }

    @Override
    public Page<TradeRecordVO> getRecords(Integer page, Integer pageSize) {
        TradeStrategy strategy = getStrategyEntity();
        if (strategy == null) {
            return new Page<>(page, pageSize);
        }

        LambdaQueryWrapper<TradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeRecord::getStrategyId, strategy.getId())
                .orderByDesc(TradeRecord::getCreatedAt);

        Page<TradeRecord> pageParam = new Page<>(page, pageSize);
        Page<TradeRecord> pageResult = recordMapper.selectPage(pageParam, wrapper);

        List<TradeRecordVO> voList = new ArrayList<>();
        for (TradeRecord entity : pageResult.getRecords()) {
            voList.add(toRecordVO(entity));
        }

        Page<TradeRecordVO> voPage = new Page<>();
        BeanUtil.copyProperties(pageResult, voPage);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public TradeSummaryVO getSummary() {
        TradeSummaryVO summary = new TradeSummaryVO();

        TradeStrategy strategy = getStrategyEntity();
        if (strategy == null) {
            return summary;
        }

        summary.setStrategy(getStrategy());

        // Fetch current price
        try {
            Map<String, Object> priceInfo = stockPositionService.getStockPrice(strategy.getStockCode());
            summary.setCurrentPrice((BigDecimal) priceInfo.get("price"));
            summary.setChangeAmount((BigDecimal) priceInfo.get("change"));
            summary.setChangePercent((String) priceInfo.get("changePercent"));
        } catch (Exception e) {
            // 获取行情失败时不设置价格，前端显示"暂无行情数据"
        }

        // 查询所有卖出和买入记录（仅查一次，后续复用）
        LambdaQueryWrapper<TradeRecord> sellWrapper = new LambdaQueryWrapper<>();
        sellWrapper.eq(TradeRecord::getStrategyId, strategy.getId())
                .eq(TradeRecord::getTradeType, "SELL")
                .orderByAsc(TradeRecord::getCreatedAt);
        List<TradeRecord> allSells = recordMapper.selectList(sellWrapper);

        LambdaQueryWrapper<TradeRecord> buyWrapper = new LambdaQueryWrapper<>();
        buyWrapper.eq(TradeRecord::getStrategyId, strategy.getId())
                .eq(TradeRecord::getTradeType, "BUY");
        List<TradeRecord> allBuys = recordMapper.selectList(buyWrapper);

        // 计算配对状态：哪些卖出已被回补
        Set<Long> matchedSellIds = new HashSet<>();
        for (TradeRecord buy : allBuys) {
            if (buy.getMatchedSellId() != null) {
                matchedSellIds.add(buy.getMatchedSellId());
            }
        }

        // 未配对卖出列表（按时间升序，前N个对应卖一、卖二...）
        List<TradeRecord> unmatchedSells = new ArrayList<>();
        for (TradeRecord sell : allSells) {
            if (!matchedSellIds.contains(sell.getId())) {
                unmatchedSells.add(sell);
            }
        }
        int unmatchedSellCount = unmatchedSells.size();
        int unmatchedBuyCount = allBuys.size() - matchedSellIds.size();
        summary.setUnmatchedSellCount(unmatchedSellCount);
        summary.setUnmatchedBuyCount(unmatchedBuyCount);

        // Sell levels: 链式动态，以未配对卖出为锚点
        List<Map<String, Object>> sellLevels = new ArrayList<>();
        BigDecimal anchor = strategy.getBasePrice();
        BigDecimal step = new BigDecimal("1.05");
        for (int i = 1; i <= strategy.getMaxSellCount(); i++) {
            Map<String, Object> level = new HashMap<>();
            TradeRecord executed = i <= unmatchedSells.size() ? unmatchedSells.get(i - 1) : null;
            BigDecimal levelPrice;
            BigDecimal backBuyPrice;
            if (executed != null) {
                levelPrice = executed.getTradePrice();
                backBuyPrice = levelPrice.divide(step, 2, RoundingMode.HALF_UP);
                anchor = levelPrice;
            } else {
                levelPrice = anchor.multiply(step).setScale(2, RoundingMode.HALF_UP);
                backBuyPrice = anchor.setScale(2, RoundingMode.HALF_UP);
                anchor = levelPrice;
            }
            level.put("level", i);
            level.put("price", levelPrice);
            level.put("done", executed != null);
            level.put("backBuyPrice", backBuyPrice);
            sellLevels.add(level);
        }
        summary.setSellLevels(sellLevels);

        // Buy levels: 未配对卖出即为待回补
        List<Map<String, Object>> buyLevels = new ArrayList<>();
        for (TradeRecord sell : unmatchedSells) {
            Map<String, Object> level = new HashMap<>();
            level.put("sellId", sell.getId());
            level.put("sellPrice", sell.getTradePrice());
            level.put("buyPrice", sell.getBackBuyPrice());
            level.put("sellNo", sell.getSellNo());
            buyLevels.add(level);
        }
        summary.setBuyLevels(buyLevels);

        // 从持仓表读取当前实时持股数
        StockPosition position = stockPositionService.getCurrentEntity();

        // Alerts
        List<Map<String, Object>> alerts = new ArrayList<>();
        if (summary.getCurrentPrice() != null && summary.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal price = summary.getCurrentPrice();

            if (strategy.getAlertCriticalPrice() != null
                    && price.compareTo(strategy.getAlertCriticalPrice()) <= 0) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("level", "CRITICAL");
                alert.put("msg", "股价 " + price + " 已跌破 " + strategy.getAlertCriticalPrice() + " 元紧急警戒线！");
                alerts.add(alert);
            } else if (strategy.getAlertWarningPrice() != null
                    && price.compareTo(strategy.getAlertWarningPrice()) <= 0) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("level", "WARNING");
                alert.put("msg", "股价 " + price + " 已接近 " + strategy.getAlertWarningPrice() + " 元预警线");
                alerts.add(alert);
            }

            if (unmatchedSellCount >= strategy.getMaxSellCount()) {
                int totalUnmatchedSellShares = 0;
                for (TradeRecord sell : unmatchedSells) {
                    totalUnmatchedSellShares += sell.getShares();
                }
                Map<String, Object> alert = new HashMap<>();
                alert.put("level", "CRITICAL");
                alert.put("msg", "净未配对卖出已达" + unmatchedSellCount + "次（" + totalUnmatchedSellShares + "股），剩余底仓" + Math.max(0, position != null ? position.getShares() - totalUnmatchedSellShares : 0) + "股，请重新评估");
                alerts.add(alert);
            }
            if (unmatchedBuyCount >= strategy.getMaxBuyCount()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("level", "WARNING");
                alert.put("msg", "净未配对买入已达" + unmatchedBuyCount + "次，现金筹码即将耗尽");
                alerts.add(alert);
            }

            // Approaching sell level?
            for (Map<String, Object> level : sellLevels) {
                if (!(Boolean) level.get("done")) {
                    BigDecimal target = (BigDecimal) level.get("price");
                    BigDecimal diff = target.subtract(price).divide(target, 4, RoundingMode.HALF_UP).abs();
                    if (diff.compareTo(new BigDecimal("0.02")) <= 0) {
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("level", "INFO");
                        alert.put("msg", "股价 " + price + " 接近第" + level.get("level") + "次卖出阶梯 " + target);
                        alerts.add(alert);
                    }
                    break;
                }
            }

            // Approaching buy-back level?
            for (Map<String, Object> level : buyLevels) {
                BigDecimal target = (BigDecimal) level.get("buyPrice");
                BigDecimal diff = target.subtract(price).divide(target, 4, RoundingMode.HALF_UP).abs();
                if (diff.compareTo(new BigDecimal("0.02")) <= 0) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("level", "INFO");
                    alert.put("msg", "股价 " + price + " 接近回补价 " + target + "（对应卖价 " + level.get("sellPrice") + "）");
                    alerts.add(alert);
                }
            }
        }
        summary.setAlerts(alerts);

        // Stats (累计全部记录，不受配对影响)
        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalSellCount = 0;
        int totalSellShares = 0;
        int totalBuyCount = 0;
        int totalBuyShares = 0;
        for (TradeRecord r : allSells) { totalSellCount++; totalSellShares += r.getShares(); }
        for (TradeRecord r : allBuys) {
            totalBuyCount++;
            totalBuyShares += r.getShares();
            if (r.getProfit() != null) {
                totalProfit = totalProfit.add(r.getProfit());
            }
        }
        summary.setTotalProfit(totalProfit);
        summary.setTotalSellCount(totalSellCount);
        summary.setTotalSellShares(totalSellShares);
        summary.setTotalBuyCount(totalBuyCount);
        summary.setTotalBuyShares(totalBuyShares);

        int holding = position != null ? position.getShares() : 0;
        summary.setCurrentHolding(holding);
        if (summary.getCurrentPrice() != null && summary.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
            summary.setCurrentMarketValue(summary.getCurrentPrice().multiply(BigDecimal.valueOf(holding)));
        }

        return summary;
    }

    @Override
    @Transactional
    public void reset() {
        TradeStrategy strategy = getStrategyEntity();
        if (strategy != null) {
            strategy.setSellCount(0);
            strategy.setBuyCount(0);
            strategyMapper.updateById(strategy);
        }
    }

    private String determineScenario(TradeStrategy strategy, String tradeType, String opLevel, BigDecimal tradePrice) {
        LambdaQueryWrapper<TradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeRecord::getStrategyId, strategy.getId())
                .eq(TradeRecord::getTradeType, tradeType)
                .orderByDesc(TradeRecord::getCreatedAt)
                .last("LIMIT 1");
        TradeRecord lastTrade = recordMapper.selectOne(wrapper);

        String trend = "";
        if (lastTrade != null) {
            int cmp = tradePrice.compareTo(lastTrade.getTradePrice());
            trend = cmp > 0 ? "-上涨中" : cmp < 0 ? "-下跌中" : "";
        } else {
            int cmp = tradePrice.compareTo(strategy.getBasePrice());
            trend = cmp > 0 ? "-上涨中" : cmp < 0 ? "-下跌中" : "";
        }

        if ("SELL".equals(tradeType)) {
            if ("NORMAL".equals(opLevel)) return "计划内卖出" + trend;
            if ("BOUNDARY".equals(opLevel)) return "末次卖出" + trend;
            return "超限卖出" + trend;
        } else {
            if ("NORMAL".equals(opLevel)) return "计划内买入" + trend;
            if ("BOUNDARY".equals(opLevel)) return "末次买入" + trend;
            return "超限买入" + trend;
        }
    }

    /**
     * 计算未配对数量 [未配对卖出数, 未配对买入数, 未配对卖出总股数]
     * 卖出被买入回补后互相抵消，只统计净未配对
     */
    private int[] getUnmatchedCounts(Long strategyId) {
        LambdaQueryWrapper<TradeRecord> sellWrapper = new LambdaQueryWrapper<>();
        sellWrapper.eq(TradeRecord::getStrategyId, strategyId)
                .eq(TradeRecord::getTradeType, "SELL");
        List<TradeRecord> sells = recordMapper.selectList(sellWrapper);

        LambdaQueryWrapper<TradeRecord> buyWrapper = new LambdaQueryWrapper<>();
        buyWrapper.eq(TradeRecord::getStrategyId, strategyId)
                .eq(TradeRecord::getTradeType, "BUY");
        List<TradeRecord> buys = recordMapper.selectList(buyWrapper);

        Set<Long> matchedSellIds = new HashSet<>();
        int matchedBuyCount = 0;
        for (TradeRecord buy : buys) {
            if (buy.getMatchedSellId() != null) {
                matchedSellIds.add(buy.getMatchedSellId());
                matchedBuyCount++;
            }
        }

        int matchedSellCount = 0;
        int totalUnmatchedSellShares = 0;
        for (TradeRecord sell : sells) {
            if (matchedSellIds.contains(sell.getId())) {
                matchedSellCount++;
            } else {
                totalUnmatchedSellShares += sell.getShares();
            }
        }

        return new int[]{sells.size() - matchedSellCount, buys.size() - matchedBuyCount, totalUnmatchedSellShares};
    }

    private TradeStrategy getStrategyEntity() {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<TradeStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeStrategy::getUserId, userId)
                .orderByDesc(TradeStrategy::getUpdatedAt).last("LIMIT 1");
        return strategyMapper.selectOne(wrapper);
    }

    private TradeRecordVO toRecordVO(TradeRecord entity) {
        TradeRecordVO vo = new TradeRecordVO();
        BeanUtil.copyProperties(entity, vo);
        if (entity.getCreatedAt() != null) {
            vo.setCreatedAt(entity.getCreatedAt().toString().replace("T", " "));
        }
        return vo;
    }

}
