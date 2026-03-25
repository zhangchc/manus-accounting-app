package com.accounting.service;

import com.accounting.dto.AiConfirmRecordDTO;
import com.accounting.entity.Record;
import com.accounting.vo.AiParseVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI记账服务接口
 */
public interface AiAccountingService {

    AiParseVO parseText(Long userId, String text);

    AiParseVO parseVoice(Long userId, MultipartFile audioFile);

    Record confirmRecord(Long userId, AiConfirmRecordDTO dto);

    /**
     * 语音 -> 转文字 -> 调用百炼Agent，返回Agent输出文本
     */
    String voiceAgent(Long userId, MultipartFile audioFile);
}

