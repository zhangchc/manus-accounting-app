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
import com.accounting.vo.AiAgentReplyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
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
    public AiAgentReplyVO voiceAgent(Long userId, MultipartFile audioFile) {
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
            return mapAgentOutputToReply(out.trim());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("百炼Agent调用失败", e);
            throw new BusinessException("Agent调用失败");
        }
    }

    private AiAgentReplyVO mapAgentOutputToReply(String agentText) {
        AiAgentReplyVO vo = new AiAgentReplyVO();
        if (!StringUtils.hasText(agentText)) {
            vo.setType("chat");
            vo.setDisplayText("");
            return vo;
        }

        // 期望 agent 返回 JSON：{"result": "..."} / {"result2": {...}} / {"result3": "..."}
        JSONObject root = null;
        try {
            root = JSONUtil.parseObj(agentText);
        } catch (Exception ignore) {
            // 非 JSON：按普通对话展示
        }

        if (root == null) {
            vo.setType("chat");
            vo.setDisplayText(agentText);
            return vo;
        }

        if (root.containsKey("result2")) {
            vo.setType("accounting");
            Object r2 = root.get("result2");

            List<JSONObject> objs = normalizeResult2ToObjectList(r2);
            List<com.accounting.vo.AiAccountingItemVO> items = new ArrayList<>();

            if (objs != null && !objs.isEmpty()) {
                for (JSONObject obj : objs) {
                    com.accounting.vo.AiAccountingItemVO item = new com.accounting.vo.AiAccountingItemVO();
                    String categoryName = firstNonBlank(obj,
                            "categoryName", "category", "类目", "分类", "category_name");
                    BigDecimal amount = parseAmount(obj.get("amount"));
                    if (amount == null) {
                        amount = parseAmount(obj.get("金额"));
                    }
                    item.setCategoryName(normalizeCategory(categoryName));
                    item.setAmount(amount);
                    items.add(item);
                }
            } else {
                // 允许 result2 是字符串：尝试抓“类目/金额”
                if (r2 instanceof String) {
                    String s = ((String) r2).trim();
                    com.accounting.vo.AiAccountingItemVO item = new com.accounting.vo.AiAccountingItemVO();
                    String categoryName = extractByLabel(s, "类目");
                    if (!StringUtils.hasText(categoryName)) {
                        categoryName = extractByLabel(s, "分类");
                    }
                    BigDecimal amount = extractAmountFromText(s);
                    item.setCategoryName(normalizeCategory(categoryName));
                    item.setAmount(amount);
                    items.add(item);
                }
            }

            // displayText：逐条展示即可，不做“一键入账”区的额外展示
            vo.setItems(items);
            if (items != null && !items.isEmpty()) {
                vo.setCategoryName(items.get(0).getCategoryName());
                vo.setAmount(items.get(0).getAmount());
            }
            vo.setDisplayText(buildAccountingDisplayText(items));
            return vo;
        }

        if (root.containsKey("result3")) {
            vo.setType("weather");
            vo.setDisplayText(String.valueOf(root.get("result3")).trim());
            return vo;
        }

        if (root.containsKey("result")) {
            vo.setType("chat");
            vo.setDisplayText(String.valueOf(root.get("result")).trim());
            return vo;
        }

        // 兜底
        vo.setType("chat");
        vo.setDisplayText(agentText);
        return vo;
    }

    private String firstNonBlank(JSONObject obj, String... keys) {
        for (String k : keys) {
            String v = obj.getStr(k);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }

    private BigDecimal parseAmount(Object val) {
        if (val == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractByLabel(String text, String label) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        int idx = text.indexOf(label);
        if (idx < 0) {
            return null;
        }
        // 形如：类目：餐饮，金额：12
        int colon = text.indexOf("：", idx);
        if (colon < 0) {
            colon = text.indexOf(":", idx);
        }
        if (colon < 0) {
            return null;
        }
        int end = text.indexOf("，", colon);
        if (end < 0) {
            end = text.indexOf(",", colon);
        }
        if (end < 0) {
            end = text.length();
        }
        return text.substring(colon + 1, end).trim();
    }

    private BigDecimal extractAmountFromText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String num = text.replaceAll("[^0-9.]", " ").trim();
        if (!StringUtils.hasText(num)) {
            return null;
        }
        String[] parts = num.split("\\s+");
        for (String p : parts) {
            if (p.matches("\\d+(\\.\\d{1,2})?")) {
                try {
                    return new BigDecimal(p);
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    private String normalizeCategory(String categoryName) {
        String safe = categoryName;
        if (safe != null) {
            safe = safe.trim();
        }
        return StringUtils.hasText(safe) ? safe : "未知";
    }

    private String buildAccountingDisplayText(List<com.accounting.vo.AiAccountingItemVO> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("小主！你要记录的账目如下：");
        if (items == null || items.isEmpty()) {
            return sb.append("\n（1）类目：未知，金额：未知").toString();
        }
        int idx = 1;
        for (com.accounting.vo.AiAccountingItemVO it : items) {
            String cat = it == null ? null : it.getCategoryName();
            BigDecimal amt = it == null ? null : it.getAmount();
            String amtText = amt == null ? "未知" : amt.stripTrailingZeros().toPlainString();
            sb.append("\n（").append(idx).append("）类目：")
                    .append(StringUtils.hasText(cat) ? cat : "未知")
                    .append("，金额：").append(amtText);
            idx++;
        }
        return sb.toString();
    }

    private List<JSONObject> normalizeResult2ToObjectList(Object r2) {
        if (r2 == null) {
            return null;
        }
        List<JSONObject> out = new ArrayList<>();

        if (r2 instanceof JSONArray) {
            JSONArray arr = (JSONArray) r2;
            for (Object el : arr) {
                try {
                    out.add(JSONUtil.parseObj(el));
                } catch (Exception ignore) {
                }
            }
            return out;
        }

        if (r2 instanceof JSONObject) {
            JSONObject obj = (JSONObject) r2;
            Object items = obj.get("items");
            if (items instanceof JSONArray) {
                JSONArray arr = (JSONArray) items;
                for (Object el : arr) {
                    try {
                        out.add(JSONUtil.parseObj(el));
                    } catch (Exception ignore) {
                    }
                }
                return out;
            }
            // 单条对象
            out.add(obj);
            return out;
        }

        if (r2 instanceof String) {
            String s = ((String) r2).trim();
            try {
                // 字符串数组
                JSONArray arr = JSONUtil.parseArray(s);
                for (Object el : arr) {
                    try {
                        out.add(JSONUtil.parseObj(el));
                    } catch (Exception ignore) {
                    }
                }
                if (!out.isEmpty()) {
                    return out;
                }
            } catch (Exception ignore) {
            }
            try {
                // 字符串对象
                JSONObject obj = JSONUtil.parseObj(s);
                out.add(obj);
                return out;
            } catch (Exception ignore) {
            }
        }

        return null;
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
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        JSONObject root;
        try {
            root = JSONUtil.parseObj(rawJson);
        } catch (Exception e) {
            return null;
        }
        JSONArray sentences = root.getJSONArray("sentences");
        if (sentences == null || sentences.isEmpty()) {
            return null;
        }

        Map<Integer, String> finals = new LinkedHashMap<>();
        for (Object el : sentences) {
            JSONObject s;
            try {
                s = JSONUtil.parseObj(el);
            } catch (Exception ignore) {
                continue;
            }
            if (!s.getBool("sentence_end", false)) {
                continue;
            }
            Integer id = s.getInt("sentence_id", -1);
            String t = s.getStr("text", "").trim();
            if (id != null && id >= 0 && StringUtils.hasText(t)) {
                finals.put(id, t);
            }
        }
        if (finals.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String t : finals.values()) {
            if (!StringUtils.hasText(t)) {
                continue;
            }
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

