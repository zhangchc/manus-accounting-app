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
}
