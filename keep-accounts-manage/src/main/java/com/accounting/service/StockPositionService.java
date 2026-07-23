package com.accounting.service;

import com.accounting.dto.StockPositionAddDTO;
import com.accounting.dto.StockPositionQueryDTO;
import com.accounting.dto.StockPositionSaveDTO;
import com.accounting.vo.StockPositionSummaryVO;
import com.accounting.vo.StockPositionVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 股票持仓 Service 接口
 */
public interface StockPositionService {

    /**
     * 持仓概览汇总查询，实时计算总市值、总成本、总盈亏、收益率、总仓位
     */
    StockPositionSummaryVO getSummary();

    /**
     * 新增持仓
     */
    void add(StockPositionAddDTO dto);

    /**
     * 分页查询持仓列表，含实时现价和计算指标
     */
    Page<StockPositionVO> list(StockPositionQueryDTO dto);

    /**
     * 修改或删除持仓（deleted=0 修改，deleted=1 逻辑删除）
     */
    void save(StockPositionSaveDTO dto);
}
