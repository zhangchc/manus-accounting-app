package com.accounting.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.accounting.service.SysConfigService;
import com.accounting.service.WeChatNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信通知服务实现，通过 Server酱 推送
 */
@Slf4j
@Service
public class WeChatNotifyServiceImpl implements WeChatNotifyService {

    @Autowired
    private SysConfigService sysConfigService;

    private static final String SERVER_CHAN_URL = "https://sctapi.ftqq.com/%s.send";

    @Override
    public boolean sendLevelAlert(String stockName, String stockCode, int direction,
                                   int levelNo, BigDecimal triggerPrice,
                                   BigDecimal currentPrice, int fixedShares) {
        String sendKey = sysConfigService.getByKey("notify.send_key");
        if (StrUtil.isBlank(sendKey)) {
            log.warn("SendKey 未配置，跳过推送");
            return false;
        }

        String dirName = direction == 2 ? "卖出" : "买入";
        String emoji = direction == 2 ? "📈" : "📉";
        String title = emoji + " " + dirName + "点位到达 · " + stockName;

        StringBuilder desp = new StringBuilder();
        desp.append(dirName).append("第").append(levelNo).append("档 · ¥").append(triggerPrice).append("\n");
        desp.append("当前价 ¥").append(currentPrice).append(" · 已触及\n");
        desp.append("建议按计划").append(dirName).append(" ").append(fixedShares).append(" 股");

        try {
            String url = String.format(SERVER_CHAN_URL, sendKey);
            Map<String, Object> body = new HashMap<>();
            body.put("title", title);
            body.put("desp", desp.toString());

            String response = HttpUtil.createPost(url)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .execute()
                    .body();
            log.info("Server酱推送成功: {}", response);
            return true;
        } catch (Exception e) {
            log.error("Server酱推送失败: {}", e.getMessage());
            return false;
        }
    }
}
