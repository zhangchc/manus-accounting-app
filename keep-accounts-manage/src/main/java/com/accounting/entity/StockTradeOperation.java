package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 做T操作表（网格档位快照）
 */
@Data
@TableName("t_stock_operation")
public class StockTradeOperation {

    /** 主键ID（雪花ID） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联规则表 t_stock_config.id */
    private Long configId;

    /** 股票代码（冗余，方便查询） */
    private String stockCode;

    /** 档位编号 1,2,3... */
    private Integer levelNo;

    /** 买卖方向 1-买入 2-卖出 */
    private Integer direction;

    /** 档位价格（规则生成时计算） */
    private BigDecimal levelPrice;

    /** 是否已触发 0-未触发 1-已触发 */
    private Integer triggered;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
