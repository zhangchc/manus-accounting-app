package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 语音 -> Agent 的结构化返回
 */
@Data
public class AiAgentReplyVO {

    /**
     * chat / accounting / weather
     */
    private String type;

    /**
     * 前端直接展示的文案
     */
    private String displayText;

    /**
     * 语音记账时的类目（可选）
     */
    private String categoryName;

    /**
     * 语音记账时的金额（可选）
     */
    private BigDecimal amount;

    /**
     * 多条账目（可选），用于前端逐条展示
     */
    private List<AiAccountingItemVO> items;
}

