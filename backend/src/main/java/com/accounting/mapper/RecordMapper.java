package com.accounting.mapper;

import com.accounting.entity.Record;
import com.accounting.vo.RecordVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 记账记录Mapper
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

    /**
     * 查询记录列表（关联分类信息）
     */
    List<RecordVO> selectRecordList(@Param("userId") Long userId,
                                     @Param("bookId") Long bookId,
                                     @Param("yearMonth") String yearMonth,
                                     @Param("type") Integer type);

    /**
     * 按分类统计金额
     */
    List<RecordVO> selectCategoryStats(@Param("userId") Long userId,
                                        @Param("yearMonth") String yearMonth,
                                        @Param("type") Integer type);

    /**
     * 按日统计金额
     */
    List<RecordVO> selectDayStats(@Param("userId") Long userId,
                                   @Param("yearMonth") String yearMonth,
                                   @Param("type") Integer type);
}
