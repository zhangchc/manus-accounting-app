package com.accounting.service.impl;

import com.accounting.common.BusinessException;
import com.accounting.entity.Book;
import com.accounting.mapper.BookMapper;
import com.accounting.service.BookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账本服务实现
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Override
    public List<Book> getBookList(Long userId) {
        return this.list(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, userId)
                .orderByAsc(Book::getSortOrder)
                .orderByAsc(Book::getId));
    }

    @Override
    public Book getDefaultBook(Long userId) {
        Book book = this.getOne(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, userId)
                .eq(Book::getIsDefault, 1));
        if (book == null) {
            // 如果没有默认账本，取第一个
            book = this.getOne(new LambdaQueryWrapper<Book>()
                    .eq(Book::getUserId, userId)
                    .orderByAsc(Book::getId)
                    .last("LIMIT 1"));
        }
        return book;
    }

    @Override
    public Book createBook(Long userId, Book book) {
        book.setUserId(userId);
        book.setIsDefault(0);
        this.save(book);
        return book;
    }

    @Override
    public void updateBook(Long userId, Book book) {
        Book existBook = this.getById(book.getId());
        if (existBook == null || !existBook.getUserId().equals(userId)) {
            throw new BusinessException("账本不存在");
        }
        book.setUserId(userId);
        this.updateById(book);
    }

    @Override
    public void deleteBook(Long userId, Long bookId) {
        Book book = this.getById(bookId);
        if (book == null || !book.getUserId().equals(userId)) {
            throw new BusinessException("账本不存在");
        }
        if (book.getIsDefault() == 1) {
            throw new BusinessException("默认账本不能删除");
        }
        this.removeById(bookId);
    }
}
