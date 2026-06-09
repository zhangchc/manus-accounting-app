package com.accounting.service;

import com.accounting.entity.Book;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 账本服务接口
 */
public interface BookService extends IService<Book> {

    /**
     * 获取用户账本列表
     */
    List<Book> getBookList(Long userId);

    /**
     * 获取默认账本
     */
    Book getDefaultBook(Long userId);

    /**
     * 创建账本
     */
    Book createBook(Long userId, Book book);

    /**
     * 更新账本
     */
    void updateBook(Long userId, Book book);

    /**
     * 删除账本
     */
    void deleteBook(Long userId, Long bookId);
}
