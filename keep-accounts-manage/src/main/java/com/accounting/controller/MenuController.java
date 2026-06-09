package com.accounting.controller;

import com.accounting.common.RequirePermission;
import com.accounting.common.Result;
import com.accounting.dto.MenuCreateDTO;
import com.accounting.dto.MenuUpdateDTO;
import com.accounting.service.MenuService;
import com.accounting.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/list")
    public Result<List<MenuVO>> list() {
        return Result.success(menuService.queryTree());
    }

    @RequirePermission("sys:menu:create")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody MenuCreateDTO dto) {
        menuService.create(dto);
        return Result.success();
    }

    @RequirePermission("sys:menu:edit")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody MenuUpdateDTO dto) {
        menuService.update(dto);
        return Result.success();
    }

    @RequirePermission("sys:menu:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
