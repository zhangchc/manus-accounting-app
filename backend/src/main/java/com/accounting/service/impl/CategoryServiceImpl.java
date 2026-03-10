package com.accounting.service.impl;

import com.accounting.common.BusinessException;
import com.accounting.entity.Category;
import com.accounting.mapper.CategoryMapper;
import com.accounting.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getCategoryList(Long userId, Integer type) {
        return this.list(new LambdaQueryWrapper<Category>()
                .in(Category::getUserId, 0L, userId)
                .eq(type != null, Category::getType, type)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));
    }

    @Override
    public Category createCategory(Long userId, Category category) {
        category.setUserId(userId);
        this.save(category);
        return category;
    }

    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = this.getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (category.getUserId() == 0L) {
            throw new BusinessException("系统预设分类不能删除");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此分类");
        }
        this.removeById(categoryId);
    }
}
