package com.accounting.service;

import com.accounting.dto.BudgetDTO;
import com.accounting.entity.Budget;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 预算服务接口
 */
public interface BudgetService extends IService<Budget> {

    /**
     * 设置预算
     */
    Budget setBudget(Long userId, BudgetDTO budgetDTO);

    /**
     * 获取预算
     */
    Budget getBudget(Long userId, Long categoryId, String yearMonth);

    /**
     * 获取月度预算列表
     */
    List<Budget> getMonthBudgets(Long userId, String yearMonth);
}
