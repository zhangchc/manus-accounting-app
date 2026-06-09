package com.accounting.vo;

import lombok.Data;

/**
 * AI解析返回VO
 */
@Data
public class AiParseVO {
    private String text;
    private AiDraftVO draft;
}
