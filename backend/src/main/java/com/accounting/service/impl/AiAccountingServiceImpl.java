package com.accounting.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.accounting.common.BusinessException;
import com.accounting.dto.AiConfirmRecordDTO;
import com.accounting.dto.RecordDTO;
import com.accounting.entity.Record;
import com.accounting.service.AiAccountingService;
import com.accounting.service.RecordService;
import com.accounting.vo.AiParseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class AiAccountingServiceImpl implements AiAccountingService {

    @Autowired
    private RecordService recordService;

    @Value("${ai.bailian.api-key:}")
    private String bailianApiKey;

    @Value("${ai.bailian.agent.app-id:}")
    private String bailianAgentAppId;

    @Value("${ai.asr.realtime-model:paraformer-realtime-v2}")
    private String asrRealtimeModel;

    @Value("${ai.debug.save-voice:true}")
    private Boolean saveVoiceDebug;

    @Value("${ai.debug.voice-dir:backend/debug-audio}")
    private String debugVoiceDir;

    @Override
    public AiParseVO parseText(Long userId, String text) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("文本不能为空");
        }
        // 这里保留原先前端“智能解析”的返回结构：draft 由前端/或后续扩展生成
        AiParseVO vo = new AiParseVO();
        vo.setText(text);
        vo.setDraft(null);
        return vo;
    }

    @Override
    public AiParseVO parseVoice(Long userId, MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new BusinessException("语音文件不能为空");
        }
        String savedWav = saveVoiceFileForAsr(userId, audioFile, true);
        String text = transcribeAudioLocalRealtime(savedWav);
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("未识别到有效语音内容");
        }
        return parseText(userId, text.trim());
    }

    @Override
    public Record confirmRecord(Long userId, AiConfirmRecordDTO dto) {
        RecordDTO recordDTO = new RecordDTO();
        recordDTO.setCategoryId(dto.getCategoryId());
        recordDTO.setType(dto.getType());
        recordDTO.setAmount(dto.getAmount());
        recordDTO.setRemark(dto.getRemark());
        recordDTO.setRecordDate(StringUtils.hasText(dto.getRecordDate())
                ? dto.getRecordDate()
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        recordDTO.setRecordTime(StringUtils.hasText(dto.getRecordTime())
                ? dto.getRecordTime()
                : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return recordService.addRecord(userId, recordDTO);
    }

    @Override
    public String voiceAgent(Long userId, MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new BusinessException("语音文件不能为空");
        }
        if (!StringUtils.hasText(bailianApiKey)) {
            throw new BusinessException("未配置百炼API Key");
        }
        if (!StringUtils.hasText(bailianAgentAppId)) {
            throw new BusinessException("未配置百炼Agent应用ID");
        }

        String savedWav = saveVoiceFileForAsr(userId, audioFile, true);
        String asrText = transcribeAudioLocalRealtime(savedWav);
        if (!StringUtils.hasText(asrText)) {
            throw new BusinessException("未识别到有效语音内容");
        }

        try {
            ApplicationParam param = ApplicationParam.builder()
                    .apiKey(bailianApiKey)
                    .appId(bailianAgentAppId)
                    .prompt(asrText.trim())
                    .incrementalOutput(false)
                    .build();
            Application app = new Application();
            ApplicationResult result = app.call(param);
            String out = (result == null || result.getOutput() == null) ? null : result.getOutput().getText();
            if (out == null || !StringUtils.hasText(out)) {
                throw new BusinessException("Agent未返回有效内容");
            }
            return out.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("百炼Agent调用失败", e);
            throw new BusinessException("Agent调用失败");
        }
    }

    // 包内可见：便于你用 Spring 容器写 JUnit 直接调试
    String transcribeAudioLocalRealtime(String savedWavFileName) {
        try {
            Path baseDir = Paths.get(System.getProperty("user.dir")).resolve(debugVoiceDir).normalize();
            Path wavPath = baseDir.resolve(savedWavFileName).normalize();
            if (!wavPath.startsWith(baseDir) || !Files.exists(wavPath)) {
                throw new BusinessException("语音文件不存在");
            }

            Recognition recognizer = new Recognition();
            RecognitionParam param = RecognitionParam.builder()
                    .apiKey(bailianApiKey)
                    .model(asrRealtimeModel)
                    .format("wav")
                    .sampleRate(16000)
                    .parameter("language_hints", new String[]{"zh"})
                    .build();

            Object raw = recognizer.call(param, wavPath.toFile());
            String rawJson = String.valueOf(raw);
            return extractFinalTextFromRecognitionJson(rawJson);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("本地语音识别失败", e);
            throw new BusinessException("语音识别失败");
        }
    }

    static String extractFinalTextFromRecognitionJson(String rawJson) {
        if (!StringUtils.hasText(rawJson)) return null;
        JSONObject root;
        try {
            root = JSONUtil.parseObj(rawJson);
        } catch (Exception e) {
            return null;
        }
        JSONArray sentences = root.getJSONArray("sentences");
        if (sentences == null || sentences.isEmpty()) return null;

        Map<Integer, String> finals = new LinkedHashMap<>();
        for (Object el : sentences) {
            JSONObject s;
            try {
                s = JSONUtil.parseObj(el);
            } catch (Exception ignore) {
                continue;
            }
            if (!s.getBool("sentence_end", false)) continue;
            Integer id = s.getInt("sentence_id", -1);
            String t = s.getStr("text", "").trim();
            if (id != null && id >= 0 && StringUtils.hasText(t)) {
                finals.put(id, t);
            }
        }
        if (finals.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (String t : finals.values()) {
            if (!StringUtils.hasText(t)) continue;
            sb.append(t);
        }
        String out = sb.toString().trim();
        return StringUtils.hasText(out) ? out : null;
    }

    private String saveVoiceFileForAsr(Long userId, MultipartFile audioFile, boolean forceSave) {
        if (!forceSave && Boolean.FALSE.equals(saveVoiceDebug)) {
            return null;
        }
        try {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String baseName = String.format(Locale.ROOT, "u%s_%s", userId, ts);
            String rawFileName = baseName + ".upload";
            String wavFileName = baseName + ".wav";

            Path baseDir = Paths.get(System.getProperty("user.dir")).resolve(debugVoiceDir).normalize();
            Files.createDirectories(baseDir);
            Path rawTarget = baseDir.resolve(rawFileName);
            Path wavTarget = baseDir.resolve(wavFileName);

            Files.copy(audioFile.getInputStream(), rawTarget, StandardCopyOption.REPLACE_EXISTING);
            transcodeToWavWithFfmpeg(rawTarget, wavTarget);
            return wavFileName;
        } catch (IOException e) {
            log.warn("语音落地失败: {}", e.getMessage());
            return null;
        }
    }

    private void transcodeToWavWithFfmpeg(Path input, Path outputWav) throws IOException {
        java.util.List<String> cmd = java.util.Arrays.asList(
                "ffmpeg",
                "-y",
                "-i", input.toAbsolutePath().toString(),
                "-vn",
                "-ac", "1",
                "-ar", "16000",
                "-acodec", "pcm_s16le",
                outputWav.toAbsolutePath().toString()
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = p.getInputStream().read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        try {
            int code = p.waitFor();
            if (code != 0) {
                throw new IOException("ffmpeg 转码失败: " + new String(baos.toByteArray(), StandardCharsets.UTF_8));
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("ffmpeg 转码被中断", ie);
        }
    }
}

