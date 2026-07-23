package com.accounting.mapper;

import com.accounting.entity.StockTradeOperation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 做T操作表 Mapper
 */
@Mapper
public interface StockTradeOperationMapper extends BaseMapper<StockTradeOperation> {
}
