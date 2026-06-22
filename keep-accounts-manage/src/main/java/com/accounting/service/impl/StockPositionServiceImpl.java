package com.accounting.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.accounting.dto.StockPositionDTO;
import com.accounting.entity.StockPosition;
import com.accounting.mapper.StockPositionMapper;
import com.accounting.service.StockPositionService;
import com.accounting.vo.StockPositionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockPositionServiceImpl implements StockPositionService {

    @Autowired
    private StockPositionMapper stockPositionMapper;

    @Override
    public StockPositionVO getPosition() {
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StockPosition::getUpdatedAt).last("LIMIT 1");
        StockPosition entity = stockPositionMapper.selectOne(wrapper);

        StockPositionVO vo = new StockPositionVO();
        if (entity == null) {
            return vo;
        }

        vo.setId(entity.getId());
        vo.setStockName(entity.getStockName());
        vo.setStockCode(entity.getStockCode());
        vo.setCostPrice(entity.getCostPrice());
        vo.setShares(entity.getShares());
        if (entity.getUpdatedAt() != null) {
            vo.setUpdatedAt(entity.getUpdatedAt().toString().replace("T", " "));
        }

        BigDecimal totalCost = entity.getCostPrice().multiply(BigDecimal.valueOf(entity.getShares()));
        vo.setTotalCost(totalCost);

        try {
            Map<String, Object> priceInfo = fetchStockPrice(entity.getStockCode());
            BigDecimal currentPrice = (BigDecimal) priceInfo.get("price");
            BigDecimal prevClose = (BigDecimal) priceInfo.get("prevClose");
            vo.setCurrentPrice(currentPrice);
            vo.setChange((BigDecimal) priceInfo.get("change"));
            vo.setChangePercent((String) priceInfo.get("changePercent"));

            BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(entity.getShares()));
            vo.setCurrentValue(currentValue);

            BigDecimal profitLoss = currentPrice.subtract(entity.getCostPrice())
                    .multiply(BigDecimal.valueOf(entity.getShares()));
            vo.setProfitLoss(profitLoss);

            if (entity.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percent = currentPrice.subtract(entity.getCostPrice())
                        .divide(entity.getCostPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                vo.setProfitLossPercent(percent);
            }

            if (prevClose != null && prevClose.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dailyPl = currentPrice.subtract(prevClose)
                        .multiply(BigDecimal.valueOf(entity.getShares()));
                vo.setDailyProfitLoss(dailyPl);
            }
        } catch (Exception e) {
            // 获取股价失败时，不设置现价相关字段
        }

        return vo;
    }

    @Override
    @Transactional
    public StockPositionVO saveOrUpdate(StockPositionDTO dto) {
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StockPosition::getUpdatedAt).last("LIMIT 1");
        StockPosition existing = stockPositionMapper.selectOne(wrapper);

        StockPosition entity;
        if (existing != null) {
            entity = existing;
        } else {
            entity = new StockPosition();
        }

        entity.setStockName(dto.getStockName());
        entity.setStockCode(dto.getStockCode());
        entity.setCostPrice(dto.getCostPrice());
        entity.setShares(dto.getShares());

        if (existing != null) {
            stockPositionMapper.updateById(entity);
        } else {
            stockPositionMapper.insert(entity);
        }

        return getPosition();
    }

    @Override
    public Map<String, Object> getStockPrice(String code) {
        if (StrUtil.isBlank(code)) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("price", BigDecimal.ZERO);
            return empty;
        }
        return fetchStockPrice(code);
    }

    private Map<String, Object> fetchStockPrice(String code) {
        String url = "https://qt.gtimg.cn/q=" + code;
        String response = HttpUtil.createGet(url).charset("GBK").execute().body();

        if (StrUtil.isBlank(response)) {
            throw new RuntimeException("获取股票行情失败");
        }

        String[] parts = response.split("~");
        if (parts.length < 33) {
            throw new RuntimeException("解析股票行情数据失败");
        }

        BigDecimal currentPrice = new BigDecimal(parts[3]);
        BigDecimal prevClose = new BigDecimal(parts[4]);
        BigDecimal changeAmount = new BigDecimal(parts[31]);
        String changePercentStr = parts[32];

        Map<String, Object> result = new HashMap<>();
        result.put("name", parts[1]);
        result.put("price", currentPrice);
        result.put("prevClose", prevClose);
        result.put("change", changeAmount);
        result.put("changePercent", changePercentStr);
        return result;
    }
}
