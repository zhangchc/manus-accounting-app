package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.AiConfirmRecordDTO;
import com.accounting.entity.Record;
import com.accounting.service.AiAccountingService;
import com.accounting.vo.AiAgentReplyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * AI记账控制器
 */
@RestController
@RequestMapping("/ai-accounting")
public class AiAccountingController {

    @Autowired
    private AiAccountingService aiAccountingService;

    @PostMapping("/confirm")
    public Result<Record> confirm(HttpServletRequest request, @Valid @RequestBody AiConfirmRecordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiAccountingService.confirmRecord(userId, dto));
    }

    @PostMapping("/voice-agent")
    public Result<AiAgentReplyVO> voiceAgent(HttpServletRequest request,
                                            @RequestParam("audio") MultipartFile audioFile) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiAccountingService.voiceAgent(userId, audioFile));
    }
}

