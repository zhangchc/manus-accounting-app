package com.accounting.service;

import java.math.BigDecimal;

/**
 * 微信通知服务接口，封装 Server酱 推送
 */
public interface WeChatNotifyService {

    /**
     * 发送档位到达通知
     *
     * @param stockName    股票名称
     * @param stockCode    股票代码
     * @param direction    1-买入 2-卖出
     * @param levelNo      档位编号
     * @param triggerPrice 触发价
     * @param currentPrice 当前价
     * @param fixedShares  计划操作股数
     * @return true-发送成功 false-失败
     */
    boolean sendLevelAlert(String stockName, String stockCode, int direction,
                           int levelNo, BigDecimal triggerPrice,
                           BigDecimal currentPrice, int fixedShares);
}
