package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.entity.Book;
import com.accounting.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 账本控制器
 */
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 获取账本列表
     */
    @GetMapping("/list")
    public Result<List<Book>> getBookList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Book> books = bookService.getBookList(userId);
        return Result.success(books);
    }

    /**
     * 获取默认账本
     */
    @GetMapping("/default")
    public Result<Book> getDefaultBook(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Book book = bookService.getDefaultBook(userId);
        return Result.success(book);
    }

    /**
     * 创建账本
     */
    @PostMapping
    public Result<Book> createBook(HttpServletRequest request, @RequestBody Book book) {
        Long userId = (Long) request.getAttribute("userId");
        Book newBook = bookService.createBook(userId, book);
        return Result.success(newBook);
    }

    /**
     * 更新账本
     */
    @PutMapping
    public Result<?> updateBook(HttpServletRequest request, @RequestBody Book book) {
        Long userId = (Long) request.getAttribute("userId");
        bookService.updateBook(userId, book);
        return Result.success();
    }

    /**
     * 删除账本
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteBook(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        bookService.deleteBook(userId, id);
        return Result.success();
    }
}
