package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.RoleCreateDTO;
import com.accounting.dto.RoleUpdateDTO;
import com.accounting.service.RoleService;
import com.accounting.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.list());
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody RoleCreateDTO dto) {
        roleService.create(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody RoleUpdateDTO dto) {
        roleService.update(dto);
        return Result.success();
    }

    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> getMenus(@PathVariable Long roleId) {
        return Result.success(roleService.getMenuIdsByRoleId(roleId));
    }

    @PutMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }
}
