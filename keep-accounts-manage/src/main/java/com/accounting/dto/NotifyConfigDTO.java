package com.accounting.dto;

import lombok.Data;

/**
 * 通知配置 DTO
 */
@Data
public class NotifyConfigDTO {

    /** Server酱 SendKey */
    private String sendKey;

    /** 通知开关 "true" / "false" */
    private String enable;
}
