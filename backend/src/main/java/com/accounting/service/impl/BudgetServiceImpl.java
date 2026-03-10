package com.accounting.service.impl;

import com.accounting.dto.BudgetDTO;
import com.accounting.entity.Budget;
import com.accounting.mapper.BudgetMapper;
import com.accounting.service.BudgetService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预算服务实现
 */
@Service
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget> implements BudgetService {

    @Override
    public Budget setBudget(Long userId, BudgetDTO budgetDTO) {
        // 查询是否已存在
        Budget budget = this.getOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(budgetDTO.getCategoryId() != null, Budget::getCategoryId, budgetDTO.getCategoryId())
                .isNull(budgetDTO.getCategoryId() == null, Budget::getCategoryId)
                .eq(Budget::getYearMonth, budgetDTO.getYearMonth()));

        if (budget != null) {
            budget.setAmount(budgetDTO.getAmount());
            this.updateById(budget);
        } else {
            budget = new Budget();
            budget.setUserId(userId);
            budget.setCategoryId(budgetDTO.getCategoryId());
            budget.setAmount(budgetDTO.getAmount());
            budget.setYearMonth(budgetDTO.getYearMonth());
            this.save(budget);
        }
        return budget;
    }

    @Override
    public Budget getBudget(Long userId, Long categoryId, String yearMonth) {
        return this.getOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(categoryId != null, Budget::getCategoryId, categoryId)
                .isNull(categoryId == null, Budget::getCategoryId)
                .eq(Budget::getYearMonth, yearMonth));
    }

    @Override
    public List<Budget> getMonthBudgets(Long userId, String yearMonth) {
        return this.list(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getYearMonth, yearMonth));
    }
}
