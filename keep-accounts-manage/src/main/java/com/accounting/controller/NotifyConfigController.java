package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.NotifyConfigDTO;
import com.accounting.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知配置 Controller
 */
@RestController
@RequestMapping("/notify")
public class NotifyConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 获取通知配置
     */
    @GetMapping("/config")
    public Result<NotifyConfigDTO> getConfig() {
        NotifyConfigDTO dto = new NotifyConfigDTO();
        dto.setSendKey(sysConfigService.getByKey("notify.send_key"));
        dto.setEnable(sysConfigService.getByKey("notify.enable"));
        return Result.success(dto);
    }

    /**
     * 保存通知配置
     */
    @PostMapping("/config/save")
    public Result<Void> saveConfig(@RequestBody NotifyConfigDTO dto) {
        sysConfigService.save("notify.send_key", dto.getSendKey() != null ? dto.getSendKey() : "");
        sysConfigService.save("notify.enable", "true".equals(dto.getEnable()) ? "true" : "false");
        return Result.success();
    }
}
