package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.BudgetDTO;
import com.accounting.entity.Budget;
import com.accounting.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 预算控制器
 */
@RestController
@RequestMapping("/budget")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    /**
     * 设置预算
     */
    @PostMapping
    public Result<Budget> setBudget(HttpServletRequest request, @Valid @RequestBody BudgetDTO budgetDTO) {
        Long userId = (Long) request.getAttribute("userId");
        Budget budget = budgetService.setBudget(userId, budgetDTO);
        return Result.success(budget);
    }

    /**
     * 获取月度预算列表
     */
    @GetMapping("/month")
    public Result<List<Budget>> getMonthBudgets(HttpServletRequest request,
                                                 @RequestParam String yearMonth) {
        Long userId = (Long) request.getAttribute("userId");
        List<Budget> budgets = budgetService.getMonthBudgets(userId, yearMonth);
        return Result.success(budgets);
    }
}
