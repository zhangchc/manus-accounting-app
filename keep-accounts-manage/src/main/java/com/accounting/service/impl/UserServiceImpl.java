package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.accounting.common.BusinessException;
import com.accounting.dto.UserCreateDTO;
import com.accounting.dto.UserUpdateDTO;
import com.accounting.entity.SysRole;
import com.accounting.entity.SysUser;
import com.accounting.entity.SysUserRole;
import com.accounting.mapper.SysRoleMapper;
import com.accounting.mapper.SysUserMapper;
import com.accounting.mapper.SysUserRoleMapper;
import com.accounting.service.UserService;
import com.accounting.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public List<UserVO> list() {
        List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId)
        );

        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysUserRole> allUserRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds)
        );
        Map<Long, List<Long>> userRoleMap = allUserRoles.stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));

        List<Long> allRoleIds = allUserRoles.stream().map(SysUserRole::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, String> roleNameMap = Collections.emptyMap();
        if (!allRoleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getId, allRoleIds)
            );
            roleNameMap = roles.stream().collect(Collectors.toMap(SysRole::getId, SysRole::getName));
        }

        Map<Long, String> finalRoleNameMap = roleNameMap;
        return users.stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(u, vo);
            vo.setStatus(u.getStatus() == 1);
            List<Long> rids = userRoleMap.getOrDefault(u.getId(), Collections.emptyList());
            vo.setRoleNames(rids.stream().map(finalRoleNameMap::get).filter(n -> n != null).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void create(UserCreateDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtil.copyProperties(dto, user);
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setStatus(dto.getStatus() != null && dto.getStatus() ? 1 : 0);
        Long userId = (Long) request.getAttribute("userId");
        user.setCreatedUser(userId);
        user.setUpdatedUser(userId);

        userMapper.insert(user);
    }

    @Override
    @Transactional
    public void update(UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .ne(SysUser::getId, dto.getId())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        BeanUtil.copyProperties(dto, user, "password", "createdUser", "createdAt");
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        }
        user.setStatus(dto.getStatus() != null && dto.getStatus() ? 1 : 0);
        Long userId = (Long) request.getAttribute("userId");
        user.setUpdatedUser(userId);

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Long currentUserId = (Long) request.getAttribute("userId");
        if (id.equals(currentUserId)) {
            throw new BusinessException("不能删除自己");
        }
        userMapper.deleteById(id);
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        List<SysUserRole> list = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        return list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> list = roleIds.stream().map(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            }).collect(Collectors.toList());
            userRoleMapper.insertBatch(list);
        }
    }
}
