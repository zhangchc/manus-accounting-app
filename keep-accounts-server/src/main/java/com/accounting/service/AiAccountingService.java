package com.accounting.service;

import com.accounting.dto.AiConfirmRecordDTO;
import com.accounting.entity.Record;
import com.accounting.vo.AiAgentReplyVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI记账服务接口
 */
public interface AiAccountingService {


    Record confirmRecord(Long userId, AiConfirmRecordDTO dto);

    /**
     * 语音 -> 转文字 -> 调用百炼Agent，返回Agent输出文本
     */
    AiAgentReplyVO voiceAgent(Long userId, MultipartFile audioFile);
}

