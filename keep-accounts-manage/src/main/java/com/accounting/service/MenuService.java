package com.accounting.service;

import com.accounting.dto.MenuCreateDTO;
import com.accounting.dto.MenuUpdateDTO;
import com.accounting.vo.MenuVO;

import java.util.List;

public interface MenuService {

    /**
     * 查询菜单树
     */
    List<MenuVO> queryTree();

    /**
     * 新增菜单
     */
    void create(MenuCreateDTO dto);

    /**
     * 更新菜单
     */
    void update(MenuUpdateDTO dto);

    /**
     * 删除菜单（逻辑删除，有子节点时拒绝）
     */
    void delete(Long id);

    /**
     * 根据用户ID获取其拥有的菜单树（包含 btn 类型，由调用方自行过滤）
     */
    List<MenuVO> getMenusByUserId(Long userId);

    /**
     * 根据用户ID获取其拥有的权限码集合
     */
    java.util.Set<String> getPermissionCodesByUserId(Long userId);
}
