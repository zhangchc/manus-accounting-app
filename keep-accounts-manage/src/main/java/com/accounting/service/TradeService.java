package com.accounting.service;

import com.accounting.dto.TradeRecordDTO;
import com.accounting.dto.TradeStrategyDTO;
import com.accounting.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.Map;

public interface TradeService {

    TradeStrategyVO getStrategy();

    TradeStrategyVO saveStrategy(TradeStrategyDTO dto);

    TradePrecheckVO precheck(String type, BigDecimal currentPrice);

    TradeRecordVO sell(TradeRecordDTO dto);

    TradeRecordVO buy(TradeRecordDTO dto);

    Page<TradeRecordVO> getRecords(Integer page, Integer pageSize);

    TradeSummaryVO getSummary();

    void reset();
}
