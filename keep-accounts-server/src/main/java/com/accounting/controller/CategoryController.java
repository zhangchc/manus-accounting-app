package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.entity.Category;
import com.accounting.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList(HttpServletRequest request,
                                                   @RequestParam(required = false) Integer type) {
        Long userId = (Long) request.getAttribute("userId");
        List<Category> categories = categoryService.getCategoryList(userId, type);
        return Result.success(categories);
    }

    /**
     * 创建自定义分类
     */
    @PostMapping
    public Result<Category> createCategory(HttpServletRequest request, @RequestBody Category category) {
        Long userId = (Long) request.getAttribute("userId");
        Category newCategory = categoryService.createCategory(userId, category);
        return Result.success(newCategory);
    }

    /**
     * 删除自定义分类
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        categoryService.deleteCategory(userId, id);
        return Result.success();
    }
}
