package com.accounting.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.accounting.common.BusinessException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 股票行情工具类，封装腾讯免费行情接口调用
 */
public class StockPriceUtil {

    private StockPriceUtil() {}

    /**
     * 批量获取股票实时行情
     *
     * @param codes 无前缀股票代码列表，如 ["600519", "300750"]
     * @return Map<stockCode, StockQuote>，key 为无前缀代码
     */
    public static Map<String, StockQuote> fetchBatch(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new HashMap<>();
        }

        // 逐只拼接带前缀的请求参数
        StringBuilder sb = new StringBuilder();
        for (String code : codes) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(addExchangePrefix(code));
        }

        String url = "https://qt.gtimg.cn/q=" + sb.toString();
        String response = HttpUtil.createGet(url).charset("GBK").execute().body();

        if (StrUtil.isBlank(response)) {
            throw new BusinessException("获取股票行情失败");
        }

        // 每只股票返回一行，以 ~ 分隔字段
        Map<String, StockQuote> result = new HashMap<>();
        for (String line : response.split("\n")) {
            if (StrUtil.isBlank(line)) {
                continue;
            }
            String[] parts = line.split("~");
            if (parts.length < 33) {
                continue;
            }

            StockQuote quote = new StockQuote();
            quote.stockCode = parts[2];          // 无前缀代码
            quote.stockName = parts[1];          // 股票名称
            try {
                quote.currentPrice = new BigDecimal(parts[3]);   // 最新价
                quote.prevClose = new BigDecimal(parts[4]);      // 昨收价
                quote.change = new BigDecimal(parts[31]);        // 涨跌额
            } catch (NumberFormatException e) {
                throw new BusinessException("股票行情数据解析失败，股票代码: " + parts[2]);
            }
            quote.changePercent = parts[32];                 // 涨跌幅，如 "+1.23"

            // 以无前缀代码作为 key，与库中 stock_code 对齐
            result.put(parts[2], quote);
        }
        return result;
    }

    /**
     * 根据股票代码自动补充交易所前缀
     * <p>腾讯行情接口要求区分 sh(上海) / sz(深圳)</p>
     * <ul>
     *   <li>上海交易所：600/601/603/605/688/588 开头 → sh</li>
     *   <li>深圳交易所：000/001/002/003/300/301 开头 → sz</li>
     * </ul>
     *
     * @param stockCode 无前缀股票代码
     * @return 带前缀的代码，如 sh600519
     */
    private static String addExchangePrefix(String stockCode) {
        if (stockCode.matches("^6(00|01|03|05|88).*|^588.*")) {
            return "sh" + stockCode;
        }
        if (stockCode.matches("^(000|001|002|003|300|301).*")) {
            return "sz" + stockCode;
        }
        throw new BusinessException("不支持的股票代码，无法识别交易所: " + stockCode);
    }

    /**
     * 股票行情数据
     */
    public static class StockQuote {
        /** 股票代码（无前缀） */
        public String stockCode;
        /** 股票名称 */
        public String stockName;
        /** 最新价 */
        public BigDecimal currentPrice;
        /** 昨收价 */
        public BigDecimal prevClose;
        /** 涨跌额（最新价 − 昨收价） */
        public BigDecimal change;
        /** 涨跌幅，百分比字符串，如 "+1.23" */
        public String changePercent;
    }
}
