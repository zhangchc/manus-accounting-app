package com.accounting.controller;

import com.accounting.common.BusinessException;
import com.accounting.common.Result;
import com.accounting.dto.LoginDTO;
import com.accounting.entity.SysUser;
import com.accounting.mapper.SysUserMapper;
import com.accounting.service.MenuService;
import com.accounting.utils.JwtUtil;
import com.accounting.vo.LoginVO;
import com.accounting.vo.MenuVO;
import com.accounting.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MenuService menuService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        return Result.success(vo);
    }

    @GetMapping("/userinfo")
    public Result<UserInfoVO> userinfo() {
        Long userId = (Long) request.getAttribute("userId");

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<MenuVO> allMenus = menuService.getMenusByUserId(userId);

        List<MenuVO> tree = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        for (MenuVO m : allMenus) {
            splitMenu(m, tree, permissions);
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setMenus(tree);
        vo.setPermissions(permissions);
        return Result.success(vo);
    }

    private void splitMenu(MenuVO menu, List<MenuVO> tree, List<String> permissions) {
        if ("btn".equals(menu.getType())) {
            if (menu.getPermission() != null && !menu.getPermission().isEmpty()) {
                permissions.add(menu.getPermission());
            }
        } else {
            MenuVO node = new MenuVO();
            node.setId(menu.getId());
            node.setName(menu.getName());
            node.setIcon(menu.getIcon());
            node.setType(menu.getType());
            node.setPath(menu.getPath());
            node.setSort(menu.getSort());
            node.setChildren(new ArrayList<>());
            if (menu.getChildren() != null) {
                for (MenuVO child : menu.getChildren()) {
                    splitMenu(child, node.getChildren(), permissions);
                }
            }
            tree.add(node);
        }
    }
}
