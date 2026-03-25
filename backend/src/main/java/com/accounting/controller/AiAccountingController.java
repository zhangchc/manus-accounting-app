package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.AiConfirmRecordDTO;
import com.accounting.dto.AiTextParseDTO;
import com.accounting.entity.Record;
import com.accounting.service.AiAccountingService;
import com.accounting.vo.AiParseVO;
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

    @PostMapping("/text-parse")
    public Result<AiParseVO> textParse(HttpServletRequest request, @Valid @RequestBody AiTextParseDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiAccountingService.parseText(userId, dto.getText()));
    }

    @PostMapping("/voice-parse")
    public Result<AiParseVO> voiceParse(HttpServletRequest request,
                                        @RequestParam("audio") MultipartFile audioFile) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiAccountingService.parseVoice(userId, audioFile));
    }

    @PostMapping("/confirm")
    public Result<Record> confirm(HttpServletRequest request, @Valid @RequestBody AiConfirmRecordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiAccountingService.confirmRecord(userId, dto));
    }

    @PostMapping("/voice-agent")
    public Result<String> voiceAgent(HttpServletRequest request,
                                     @RequestParam("audio") MultipartFile audioFile) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiAccountingService.voiceAgent(userId, audioFile));
    }
}

