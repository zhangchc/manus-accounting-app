package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.accounting.common.BusinessException;
import com.accounting.dto.MenuCreateDTO;
import com.accounting.dto.MenuUpdateDTO;
import com.accounting.entity.SysMenu;
import com.accounting.mapper.SysMenuMapper;
import com.accounting.service.MenuService;
import com.accounting.vo.MenuVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public List<MenuVO> queryTree() {
        List<SysMenu> all = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort)
        );
        Map<Long, List<SysMenu>> childrenMap = all.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() != null ? m.getParentId() : 0L));
        return childrenMap.getOrDefault(0L, Collections.emptyList()).stream()
                .map(m -> toVO(m, childrenMap))
                .collect(Collectors.toList());
    }

    @Override
    public void create(MenuCreateDTO dto) {
        validateType(dto.getType());
        if (dto.getParentId() != null && dto.getParentId() != 0) {
            SysMenu parent = menuMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("上级菜单不存在");
            }
            validateHierarchy(parent.getType(), dto.getType());
        } else {
            if ("btn".equals(dto.getType())) {
                throw new BusinessException("按钮不能作为顶级菜单");
            }
        }

        SysMenu menu = new SysMenu();
        BeanUtil.copyProperties(dto, menu);
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0);
        menu.setStatus(dto.getStatus() != null && dto.getStatus() ? 1 : 0);
        Long userId = (Long) request.getAttribute("userId");
        menu.setCreatedUser(userId);
        menu.setUpdatedUser(userId);

        menuMapper.insert(menu);
    }

    @Override
    public void update(MenuUpdateDTO dto) {
        validateType(dto.getType());
        SysMenu menu = menuMapper.selectById(dto.getId());
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        if (!menu.getType().equals(dto.getType())) {
            throw new BusinessException("菜单类型不允许变更");
        }
        if (dto.getParentId() != null && dto.getParentId() != 0) {
            if (dto.getParentId().equals(dto.getId())) {
                throw new BusinessException("上级菜单不能是自己");
            }
            if (wouldCreateCycle(dto.getId(), dto.getParentId())) {
                throw new BusinessException("上级菜单不能是自己的子节点");
            }
            SysMenu parent = menuMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("上级菜单不存在");
            }
            validateHierarchy(parent.getType(), dto.getType());
        } else {
            if ("btn".equals(dto.getType())) {
                throw new BusinessException("按钮不能作为顶级菜单");
            }
        }

        BeanUtil.copyProperties(dto, menu, "createdUser", "createdAt");
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0);
        menu.setStatus(dto.getStatus() != null && dto.getStatus() ? 1 : 0);
        Long userId = (Long) request.getAttribute("userId");
        menu.setUpdatedUser(userId);

        menuMapper.updateById(menu);
    }

    @Override
    public void delete(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id)
        );
        if (count > 0) {
            throw new BusinessException("该菜单下存在子节点，请先删除子节点");
        }
        menuMapper.deleteById(id);
    }

    private void validateType(String type) {
        if (!"dir".equals(type) && !"menu".equals(type) && !"btn".equals(type)) {
            throw new BusinessException("菜单类型必须为 dir、menu 或 btn");
        }
    }

    private void validateHierarchy(String parentType, String childType) {
        if ("dir".equals(parentType) && !"menu".equals(childType)) {
            throw new BusinessException("目录下只能新建菜单");
        }
        if ("menu".equals(parentType) && !"btn".equals(childType)) {
            throw new BusinessException("菜单下只能新建按钮");
        }
    }

    /**
     * 检测将 nodeId 的上级设为 newParentId 是否会产生循环引用
     */
    private boolean wouldCreateCycle(Long nodeId, Long newParentId) {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, Long> parentMap = new HashMap<>();
        for (SysMenu m : all) {
            parentMap.put(m.getId(), m.getParentId() != null ? m.getParentId() : 0L);
        }
        Long current = newParentId;
        while (current != 0) {
            if (current.equals(nodeId)) return true;
            current = parentMap.getOrDefault(current, 0L);
        }
        return false;
    }

    private MenuVO toVO(SysMenu menu, Map<Long, List<SysMenu>> childrenMap) {
        MenuVO vo = new MenuVO();
        BeanUtil.copyProperties(menu, vo);
        vo.setStatus(menu.getStatus() == 1);
        List<MenuVO> children = childrenMap.getOrDefault(menu.getId(), Collections.emptyList()).stream()
                .map(m -> toVO(m, childrenMap))
                .collect(Collectors.toList());
        vo.setChildren(children);
        return vo;
    }
}
