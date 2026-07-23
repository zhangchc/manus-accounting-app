package com.accounting.service.impl;

import cn.hutool.core.util.StrUtil;
import com.accounting.common.BusinessException;
import com.accounting.dto.StockPositionAddDTO;
import com.accounting.dto.StockPositionQueryDTO;
import com.accounting.dto.StockPositionSaveDTO;
import com.accounting.entity.StockPosition;
import com.accounting.mapper.StockPositionMapper;
import com.accounting.service.StockPositionService;
import com.accounting.utils.StockPriceUtil;
import com.accounting.utils.StockPriceUtil.StockQuote;
import com.accounting.vo.StockPositionSummaryVO;
import com.accounting.vo.StockPositionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 股票持仓 Service 实现
 */
@Slf4j
@Service
public class StockPositionServiceImpl implements StockPositionService {

    @Autowired
    private StockPositionMapper stockPositionMapper;

    @Override
    public StockPositionSummaryVO getSummary() {
        // 1. 查询所有持仓
        List<StockPosition> positions = stockPositionMapper.selectList(null);
        if (positions.isEmpty()) {
            return zeroSummary();
        }

        // 2. 提取所有 stock_code，批量获取行情
        List<String> codes = positions.stream()
                .map(StockPosition::getStockCode)
                .collect(Collectors.toList());
        Map<String, StockQuote> quoteMap = StockPriceUtil.fetchBatch(codes);

        // 3. 逐条计算汇总指标
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalDailyPnl = BigDecimal.ZERO;

        for (StockPosition p : positions) {
            // 每只股票的成本 = 成本价 × 股数
            BigDecimal cost = p.getCostPrice().multiply(BigDecimal.valueOf(p.getShares()));
            totalCost = totalCost.add(cost);

            // 每只股票的市值 = 现价 × 股数，现价来自行情接口
            StockQuote quote = quoteMap.get(p.getStockCode());
            if (quote != null) {
                BigDecimal marketValue = quote.currentPrice.multiply(BigDecimal.valueOf(p.getShares()));
                totalMarketValue = totalMarketValue.add(marketValue);

                // 当日盈亏 = 涨跌额 × 股数
                if (quote.change != null) {
                    BigDecimal dailyPnl = quote.change.multiply(BigDecimal.valueOf(p.getShares()));
                    totalDailyPnl = totalDailyPnl.add(dailyPnl);
                }
            }
        }

        // 4. 汇总：总盈亏 = 总市值 − 总成本
        BigDecimal totalProfit = totalMarketValue.subtract(totalCost);

        // 收益率 = 总盈亏 ÷ 总成本 × 100%
        BigDecimal totalProfitRate = BigDecimal.ZERO;
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitRate = totalProfit.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 总仓位 = 总市值 ÷ 总成本 × 100%
        BigDecimal positionRate = BigDecimal.ZERO;
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            positionRate = totalMarketValue.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        StockPositionSummaryVO vo = new StockPositionSummaryVO();
        vo.setTotalCost(totalCost);
        vo.setTotalMarketValue(totalMarketValue);
        vo.setTotalDailyPnl(totalDailyPnl);
        vo.setTotalProfit(totalProfit);
        vo.setTotalProfitRate(totalProfitRate);
        vo.setPositionRate(positionRate);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StockPositionAddDTO dto) {
        // stock_code 唯一性校验
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockPosition::getStockCode, dto.getStockCode());
        if (stockPositionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该股票已存在，请使用修改功能");
        }

        StockPosition entity = new StockPosition();
        entity.setStockCode(dto.getStockCode());
        entity.setStockName(dto.getStockName());
        entity.setShares(dto.getShares());
        entity.setCostPrice(dto.getCostPrice());
        stockPositionMapper.insert(entity);
    }

    @Override
    public Page<StockPositionVO> list(StockPositionQueryDTO dto) {
        // 模糊搜索条件
        LambdaQueryWrapper<StockPosition> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(dto.getStockCode())) {
            wrapper.like(StockPosition::getStockCode, dto.getStockCode());
        }
        if (StrUtil.isNotBlank(dto.getStockName())) {
            wrapper.like(StockPosition::getStockName, dto.getStockName());
        }

        Page<StockPosition> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<StockPosition> result = stockPositionMapper.selectPage(page, wrapper);

        // 批量获取行情，填充实时价格
        List<String> codes = result.getRecords().stream()
                .map(StockPosition::getStockCode)
                .collect(Collectors.toList());
        Map<String, StockQuote> quoteMap = StockPriceUtil.fetchBatch(codes);

        // 转为 VO，计算每只股票的市值、盈亏、盈亏比例
        List<StockPositionVO> voList = new ArrayList<>();
        for (StockPosition p : result.getRecords()) {
            StockPositionVO vo = new StockPositionVO();
            vo.setId(p.getId());
            vo.setStockCode(p.getStockCode());
            vo.setStockName(p.getStockName());
            vo.setShares(p.getShares());
            vo.setCostPrice(p.getCostPrice());

            // 从行情接口获取现价
            StockQuote quote = quoteMap.get(p.getStockCode());
            if (quote != null) {
                vo.setCurrentPrice(quote.currentPrice);

                // 市值 = 现价 × 股数
                BigDecimal marketValue = quote.currentPrice
                        .multiply(BigDecimal.valueOf(p.getShares()));
                vo.setMarketValue(marketValue);

                // 盈亏 = (现价 − 成本价) × 股数
                BigDecimal profit = quote.currentPrice.subtract(p.getCostPrice())
                        .multiply(BigDecimal.valueOf(p.getShares()));
                vo.setProfit(profit);

                // 盈亏比例 = (现价 − 成本价) ÷ 成本价 × 100%
                if (p.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal profitRate = quote.currentPrice.subtract(p.getCostPrice())
                            .divide(p.getCostPrice(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    vo.setProfitRate(profitRate);
                }
            }
            voList.add(vo);
        }

        Page<StockPositionVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(StockPositionSaveDTO dto) {
        // deleted = 1：逻辑删除
        if (Integer.valueOf(1).equals(dto.getDeleted())) {
            int rows = stockPositionMapper.deleteById(dto.getId());
            if (rows == 0) {
                throw new BusinessException("持仓记录不存在");
            }
            return;
        }

        // deleted = 0：修改字段
        StockPosition entity = new StockPosition();
        entity.setId(dto.getId());
        entity.setStockName(dto.getStockName());
        entity.setShares(dto.getShares());
        entity.setCostPrice(dto.getCostPrice());
        stockPositionMapper.updateById(entity);
    }

    /**
     * 无持仓时返回全零的汇总数据
     */
    private StockPositionSummaryVO zeroSummary() {
        StockPositionSummaryVO vo = new StockPositionSummaryVO();
        vo.setTotalCost(BigDecimal.ZERO);
        vo.setTotalMarketValue(BigDecimal.ZERO);
        vo.setTotalDailyPnl(BigDecimal.ZERO);
        vo.setTotalProfit(BigDecimal.ZERO);
        vo.setTotalProfitRate(BigDecimal.ZERO);
        vo.setPositionRate(BigDecimal.ZERO);
        return vo;
    }
}
