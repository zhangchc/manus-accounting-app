package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * AI文本解析请求DTO
 */
@Data
public class AiTextParseDTO {

    @NotBlank(message = "文本内容不能为空")
    private String text;
}
