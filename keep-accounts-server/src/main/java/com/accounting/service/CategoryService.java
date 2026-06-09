package com.accounting.service;

import com.accounting.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类列表（系统预设 + 用户自定义）
     */
    List<Category> getCategoryList(Long userId, Integer type);

    /**
     * 创建自定义分类
     */
    Category createCategory(Long userId, Category category);

    /**
     * 删除自定义分类
     */
    void deleteCategory(Long userId, Long categoryId);
}
