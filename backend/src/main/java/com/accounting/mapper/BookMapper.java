package com.accounting.mapper;

import com.accounting.entity.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账本Mapper
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
