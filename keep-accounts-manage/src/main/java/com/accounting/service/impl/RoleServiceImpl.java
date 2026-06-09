package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.accounting.common.BusinessException;
import com.accounting.dto.RoleCreateDTO;
import com.accounting.dto.RoleUpdateDTO;
import com.accounting.entity.SysRole;
import com.accounting.entity.SysRoleMenu;
import com.accounting.mapper.SysRoleMapper;
import com.accounting.mapper.SysRoleMenuMapper;
import com.accounting.service.RoleService;
import com.accounting.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public List<RoleVO> list() {
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort)
        );
        return roles.stream().map(r -> {
            RoleVO vo = new RoleVO();
            BeanUtil.copyProperties(r, vo);
            vo.setStatus(r.getStatus() == 1);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void create(RoleCreateDTO dto) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, dto.getCode())
        );
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = new SysRole();
        BeanUtil.copyProperties(dto, role);
        role.setStatus(dto.getStatus() != null && dto.getStatus() ? 1 : 0);
        Long userId = (Long) request.getAttribute("userId");
        role.setCreatedUser(userId);
        role.setUpdatedUser(userId);

        roleMapper.insert(role);
    }

    @Override
    public void update(RoleUpdateDTO dto) {
        SysRole role = roleMapper.selectById(dto.getId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getCode, dto.getCode())
                        .ne(SysRole::getId, dto.getId())
        );
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        BeanUtil.copyProperties(dto, role, "createdUser", "createdAt");
        role.setStatus(dto.getStatus() != null && dto.getStatus() ? 1 : 0);
        Long userId = (Long) request.getAttribute("userId");
        role.setUpdatedUser(userId);

        roleMapper.updateById(role);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );
        return list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
