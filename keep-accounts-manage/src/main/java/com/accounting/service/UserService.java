package com.accounting.service;

import com.accounting.dto.UserCreateDTO;
import com.accounting.dto.UserUpdateDTO;
import com.accounting.vo.UserVO;

import java.util.List;

public interface UserService {

    List<UserVO> list();

    void create(UserCreateDTO dto);

    void update(UserUpdateDTO dto);

    void delete(Long id);

    List<Long> getRoleIdsByUserId(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);
}
