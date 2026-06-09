package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.AppCategoryQueryDTO;
import com.accounting.service.AppCategoryService;
import com.accounting.vo.AppCategoryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-category")
public class AppCategoryController {

    @Autowired
    private AppCategoryService appCategoryService;

    @GetMapping("/page")
    public Result<Page<AppCategoryVO>> page(AppCategoryQueryDTO dto) {
        return Result.success(appCategoryService.page(dto));
    }
}
