package com.accounting.service;

import com.accounting.dto.RecordDTO;
import com.accounting.entity.Record;
import com.accounting.vo.MonthBillVO;
import com.accounting.vo.StatisticsVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 记账记录服务接口
 */
public interface RecordService extends IService<Record> {

    /**
     * 添加记账记录
     */
    Record addRecord(Long userId, RecordDTO recordDTO);

    /**
     * 更新记账记录
     */
    void updateRecord(Long userId, RecordDTO recordDTO);

    /**
     * 删除记账记录
     */
    void deleteRecord(Long userId, Long recordId);

    /**
     * 获取月账单
     */
    MonthBillVO getMonthBill(Long userId, Long bookId, String yearMonth);

    /**
     * 获取统计数据
     */
    StatisticsVO getStatistics(Long userId, String yearMonth, Integer type);

    /**
     * 获取年度汇总
     */
    Map<String, Object> getYearSummary(Long userId, String year);

    /**
     * 获取今日支出
     */
    BigDecimal getTodayExpense(Long userId);
}
