package com.accounting.service;

import com.accounting.dto.StockTradeConfigSaveDTO;
import com.accounting.vo.StockTradeConfigVO;

import java.util.List;

/**
 * 做T管理规则 Service 接口
 */
public interface StockTradeConfigService {

    /**
     * 查询所有做T规则列表，含交易统计（总盈亏、买卖次数）
     */
    List<StockTradeConfigVO> list();

    /**
     * 根据股票代码查询规则，用于新增页面联动回填
     *
     * @param stockCode 股票代码
     * @return 规则详情，不存在返回 null
     */
    StockTradeConfigVO queryByStockCode(String stockCode);

    /**
     * 保存做T规则（新增 / 修改统一入口）
     * stock_code 不存在则新增，已存在则删除旧规则后重建
     */
    void save(StockTradeConfigSaveDTO dto);
}
