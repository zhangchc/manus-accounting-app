package com.accounting.mapper;

import com.accounting.entity.StockPriceAlert;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 股价到达告警记录 Mapper
 */
@Mapper
public interface StockPriceAlertMapper extends BaseMapper<StockPriceAlert> {
}
