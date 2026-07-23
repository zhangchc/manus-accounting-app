package com.accounting.service;

import com.accounting.dto.StockTradeRecordSaveDTO;
import com.accounting.vo.StockTradeOperationVO;
import com.accounting.vo.StockTradeRecordVO;

import java.util.List;

/**
 * 做T交易记录 Service 接口
 */
public interface StockTradeRecordService {

    /**
     * 查询规则下的所有档位列表（含触发状态）
     *
     * @param configId 规则ID
     */
    List<StockTradeOperationVO> listOperations(Long configId);

    /**
     * 新增交易记录，同时标记对应档位为已触发
     */
    void saveRecord(StockTradeRecordSaveDTO dto);

    /**
     * 查询规则下的交易流水列表，配对盈亏实时 FIFO 计算
     *
     * @param configId 规则ID
     */
    List<StockTradeRecordVO> listRecords(Long configId);
}
