package com.accounting.service;

import com.accounting.dto.RoleCreateDTO;
import com.accounting.dto.RoleUpdateDTO;
import com.accounting.vo.RoleVO;

import java.util.List;

public interface RoleService {

    List<RoleVO> list();

    void create(RoleCreateDTO dto);

    void update(RoleUpdateDTO dto);

    void delete(Long id);

    List<Long> getMenuIdsByRoleId(Long roleId);

    void assignMenus(Long roleId, List<Long> menuIds);
}
