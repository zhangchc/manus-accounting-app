package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.AppUserQueryDTO;
import com.accounting.service.AppUserService;
import com.accounting.vo.AppUserDetailVO;
import com.accounting.vo.AppUserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-user")
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    @GetMapping("/page")
    public Result<Page<AppUserVO>> page(AppUserQueryDTO dto) {
        return Result.success(appUserService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public Result<AppUserDetailVO> detail(@PathVariable Long id) {
        return Result.success(appUserService.getUserDetail(id));
    }
}
