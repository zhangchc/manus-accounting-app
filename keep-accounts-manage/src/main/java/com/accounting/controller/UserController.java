package com.accounting.controller;

import com.accounting.common.RequirePermission;
import com.accounting.common.Result;
import com.accounting.dto.UserCreateDTO;
import com.accounting.dto.UserUpdateDTO;
import com.accounting.service.UserService;
import com.accounting.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<UserVO>> list() {
        return Result.success(userService.list());
    }

    @RequirePermission("sys:admin:create")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @RequirePermission("sys:admin:edit")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        userService.update(dto);
        return Result.success();
    }

    @RequirePermission("sys:admin:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @GetMapping("/{userId}/roles")
    public Result<List<Long>> getRoles(@PathVariable Long userId) {
        return Result.success(userService.getRoleIdsByUserId(userId));
    }

    @RequirePermission("sys:admin:assign-role")
    @PutMapping("/{userId}/roles")
    public Result<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return Result.success();
    }
}
