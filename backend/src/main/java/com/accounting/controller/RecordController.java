package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.RecordDTO;
import com.accounting.entity.Record;
import com.accounting.service.RecordService;
import com.accounting.vo.MonthBillVO;
import com.accounting.vo.StatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 记账记录控制器
 */
@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    /**
     * 添加记账记录
     */
    @PostMapping
    public Result<Record> addRecord(HttpServletRequest request, @Valid @RequestBody RecordDTO recordDTO) {
        Long userId = (Long) request.getAttribute("userId");
        Record record = recordService.addRecord(userId, recordDTO);
        return Result.success(record);
    }

    /**
     * 更新记账记录
     */
    @PutMapping
    public Result<?> updateRecord(HttpServletRequest request, @Valid @RequestBody RecordDTO recordDTO) {
        Long userId = (Long) request.getAttribute("userId");
        recordService.updateRecord(userId, recordDTO);
        return Result.success();
    }

    /**
     * 删除记账记录
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteRecord(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        recordService.deleteRecord(userId, id);
        return Result.success();
    }

    /**
     * 获取月账单
     */
    @GetMapping("/bill/month")
    public Result<MonthBillVO> getMonthBill(HttpServletRequest request,
                                             @RequestParam(required = false) Long bookId,
                                             @RequestParam String yearMonth) {
        Long userId = (Long) request.getAttribute("userId");
        MonthBillVO monthBill = recordService.getMonthBill(userId, bookId, yearMonth);
        return Result.success(monthBill);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<StatisticsVO> getStatistics(HttpServletRequest request,
                                               @RequestParam String yearMonth,
                                               @RequestParam(defaultValue = "1") Integer type) {
        Long userId = (Long) request.getAttribute("userId");
        StatisticsVO statistics = recordService.getStatistics(userId, yearMonth, type);
        return Result.success(statistics);
    }

    /**
     * 获取年度汇总
     */
    @GetMapping("/summary/year")
    public Result<Map<String, Object>> getYearSummary(HttpServletRequest request,
                                                       @RequestParam String year) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> summary = recordService.getYearSummary(userId, year);
        return Result.success(summary);
    }

    /**
     * 获取今日支出
     */
    @GetMapping("/today/expense")
    public Result<BigDecimal> getTodayExpense(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal expense = recordService.getTodayExpense(userId);
        return Result.success(expense);
    }
}
