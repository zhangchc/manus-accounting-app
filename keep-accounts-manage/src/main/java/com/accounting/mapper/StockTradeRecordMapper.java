package com.accounting.mapper;

import com.accounting.entity.StockTradeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 做T流水表 Mapper
 */
@Mapper
public interface StockTradeRecordMapper extends BaseMapper<StockTradeRecord> {
}
