package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.accounting.dto.AppCategoryQueryDTO;
import com.accounting.entity.AppCategory;
import com.accounting.mapper.AppCategoryMapper;
import com.accounting.service.AppCategoryService;
import com.accounting.vo.AppCategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppCategoryServiceImpl implements AppCategoryService {

    @Autowired
    private AppCategoryMapper appCategoryMapper;

    @Override
    public Page<AppCategoryVO> page(AppCategoryQueryDTO dto) {
        LambdaQueryWrapper<AppCategory> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(dto.getName())) {
            wrapper.like(AppCategory::getName, dto.getName());
        }
        if (dto.getType() != null) {
            wrapper.eq(AppCategory::getType, dto.getType());
        }
        wrapper.orderByAsc(AppCategory::getSortOrder, AppCategory::getId);

        Page<AppCategory> pageParam = new Page<>(dto.getPage(), dto.getPageSize());
        Page<AppCategory> pageResult = appCategoryMapper.selectPage(pageParam, wrapper);

        List<AppCategoryVO> voList = pageResult.getRecords().stream().map(entity -> {
            AppCategoryVO vo = new AppCategoryVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());

        Page<AppCategoryVO> voPage = new Page<>();
        BeanUtil.copyProperties(pageResult, voPage);
        voPage.setRecords(voList);
        return voPage;
    }
}
