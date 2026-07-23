package com.accounting.task;

import com.accounting.entity.StockPriceAlert;
import com.accounting.entity.StockTradeConfig;
import com.accounting.entity.StockTradeOperation;
import com.accounting.mapper.StockPriceAlertMapper;
import com.accounting.mapper.StockTradeConfigMapper;
import com.accounting.mapper.StockTradeOperationMapper;
import com.accounting.service.SysConfigService;
import com.accounting.service.WeChatNotifyService;
import com.accounting.utils.StockPriceUtil;
import com.accounting.utils.StockPriceUtil.StockQuote;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 股价到达监控定时任务，每分钟轮询行情并推送微信通知
 */
@Slf4j
@Component
public class StockPriceMonitorTask {

    @Autowired
    private StockTradeConfigMapper configMapper;

    @Autowired
    private StockTradeOperationMapper operationMapper;

    @Autowired
    private StockPriceAlertMapper alertMapper;

    @Autowired
    private WeChatNotifyService notifyService;

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 每分钟执行，周一至周五 9:00-15:00，代码内过滤非交易时段
     */
    @Scheduled(cron = "0 * 9-15 * * 1-5")
    public void monitor() {
        // 交易时段过滤：上午 9:30-11:30，下午 13:00-15:00
        LocalTime now = LocalTime.now();
        if (!isTradingTime(now)) {
            return;
        }

        // 检查通知开关
        String enable = sysConfigService.getByKey("notify.enable");
        if (!"true".equals(enable)) {
            return;
        }

        // 查询所有启用的做T规则
        LambdaQueryWrapper<StockTradeConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(StockTradeConfig::getActive, 1);
        List<StockTradeConfig> configs = configMapper.selectList(configWrapper);
        if (configs.isEmpty()) {
            return;
        }

        // 批量获取行情
        List<String> codes = configs.stream()
                .map(StockTradeConfig::getStockCode)
                .distinct()
                .collect(Collectors.toList());
        Map<String, StockQuote> quoteMap;
        try {
            quoteMap = StockPriceUtil.fetchBatch(codes);
        } catch (Exception e) {
            log.error("获取行情失败", e);
            return;
        }

        // 逐条规则检查档位触发
        for (StockTradeConfig config : configs) {
            StockQuote quote = quoteMap.get(config.getStockCode());
            if (quote == null || quote.currentPrice == null) {
                continue;
            }

            LambdaQueryWrapper<StockTradeOperation> opWrapper = new LambdaQueryWrapper<>();
            opWrapper.eq(StockTradeOperation::getConfigId, config.getId())
                    .eq(StockTradeOperation::getTriggered, 0);
            List<StockTradeOperation> ops = operationMapper.selectList(opWrapper);

            for (StockTradeOperation op : ops) {
                if (op.getLevelPrice() == null) {
                    continue;
                }

                // 判断是否到达档位价
                boolean reached;
                if (op.getDirection() == 2) {
                    reached = quote.currentPrice.compareTo(op.getLevelPrice()) >= 0;
                } else {
                    reached = quote.currentPrice.compareTo(op.getLevelPrice()) <= 0;
                }
                if (!reached) {
                    continue;
                }

                // 当天已通知则跳过，防止重复推送
                if (alreadyAlertedToday(config.getId(), op.getDirection(), op.getLevelNo())) {
                    continue;
                }

                // 发送微信通知，失败则跳过告警记录写入，下次轮询重试
                boolean sent = notifyService.sendLevelAlert(
                        config.getStockName(), config.getStockCode(),
                        op.getDirection(), op.getLevelNo(),
                        op.getLevelPrice(), quote.currentPrice,
                        config.getFixedShares());
                if (!sent) {
                    continue;
                }

                // 写入告警记录
                StockPriceAlert alert = new StockPriceAlert();
                alert.setConfigId(config.getId());
                alert.setStockCode(config.getStockCode());
                alert.setStockName(config.getStockName());
                alert.setDirection(op.getDirection());
                alert.setLevelNo(op.getLevelNo());
                alert.setTriggerPrice(op.getLevelPrice());
                alert.setCurrentPrice(quote.currentPrice);
                alert.setSendKey(sysConfigService.getByKey("notify.send_key"));
                alertMapper.insert(alert);

                log.info("档位触发通知: {} {}第{}档 ¥{}",
                        config.getStockName(),
                        op.getDirection() == 2 ? "卖出" : "买入",
                        op.getLevelNo(), op.getLevelPrice());
            }
        }
    }

    /**
     * 判断当前时间是否在交易时段内
     */
    private boolean isTradingTime(LocalTime now) {
        // 上午 9:30 - 11:30
        if (!now.isBefore(LocalTime.of(9, 30)) && !now.isAfter(LocalTime.of(11, 30))) {
            return true;
        }
        // 下午 13:00 - 15:00
        if (!now.isBefore(LocalTime.of(13, 0)) && !now.isAfter(LocalTime.of(15, 0))) {
            return true;
        }
        return false;
    }

    /**
     * 检查当天是否已经对该档位推送过通知
     */
    private boolean alreadyAlertedToday(Long configId, int direction, int levelNo) {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<StockPriceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockPriceAlert::getConfigId, configId)
                .eq(StockPriceAlert::getDirection, direction)
                .eq(StockPriceAlert::getLevelNo, levelNo)
                .between(StockPriceAlert::getCreateTime, todayStart, todayEnd);
        return alertMapper.selectCount(wrapper) > 0;
    }
}
