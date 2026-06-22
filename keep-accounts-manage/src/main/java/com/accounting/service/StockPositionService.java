package com.accounting.service;

import com.accounting.dto.StockPositionDTO;
import com.accounting.vo.StockPositionVO;

import java.util.Map;

public interface StockPositionService {

    StockPositionVO getPosition();

    StockPositionVO saveOrUpdate(StockPositionDTO dto);

    Map<String, Object> getStockPrice(String code);
}
