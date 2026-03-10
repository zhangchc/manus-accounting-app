package com.accounting.mapper;

import com.accounting.entity.Budget;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算Mapper
 */
@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {
}
