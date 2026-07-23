package com.accounting;

import com.accounting.service.WeChatNotifyService;
import com.accounting.task.StockPriceMonitorTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信推送手动验证测试，走 DB 读取 SendKey，真实调用 Server酱 API
 *
 * 使用方式：
 * 1. 确保 sys_config 表中 notify.send_key 已配置你的 SendKey
 * 2. 确保 notify.enable 为 true（本测试不检查开关，直接调推送）
 * 3. 运行：mvn test -Dtest=WeChatNotifyServiceTest
 */
@SpringBootTest
public class WeChatNotifyServiceTest {

    @Autowired
    private WeChatNotifyService notifyService;
    @Autowired
    private StockPriceMonitorTask stockPriceMonitorTask;

    /**
     * 模拟卖出档位到达通知
     */
    @Test
    public void testSendSellAlert() {
        boolean sent = notifyService.sendLevelAlert(
                "常山药业",      // stockName
                "300255",        // stockCode
                2,               // direction: 2-卖出
                1,               // levelNo: 第1档
                new BigDecimal("30.47"),   // triggerPrice
                new BigDecimal("30.37"),   // currentPrice
                1000              // fixedShares
        );
        assertTrue(sent, "推送失败，请检查 SendKey 是否正确配置");
    }

    /**
     * 模拟买入档位到达通知
     */
    @Test
    public void testSendBuyAlert() {
        boolean sent = notifyService.sendLevelAlert(
                "测试股票",
                "000001",
                1,               // direction: 1-买入
                2,               // levelNo: 第2档
                new BigDecimal("9.80"),
                new BigDecimal("9.78"),
                100
        );
        assertTrue(sent, "推送失败，请检查 SendKey 是否正确配置");
    }

    @Test
    public void testStockPriceMonitorTask() {
         stockPriceMonitorTask.monitor();
    }
}
