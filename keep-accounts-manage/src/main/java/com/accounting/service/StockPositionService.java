package com.accounting.service;

import com.accounting.dto.StockPositionDTO;
import com.accounting.entity.StockPosition;
import com.accounting.vo.CostHistoryVO;
import com.accounting.vo.StockPositionVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StockPositionService {

    StockPositionVO getPosition();

    StockPositionVO saveOrUpdate(StockPositionDTO dto);

    Map<String, Object> getStockPrice(String code);

    StockPosition getCurrentEntity();

    void updateAfterTrade(String tradeType, Integer tradeShares, BigDecimal tradePrice, BigDecimal currentPrice, Long tradeRecordId);

    List<CostHistoryVO> getCostHistory();
}
