package com.accounting.service;

import com.accounting.dto.AppUserQueryDTO;
import com.accounting.vo.AppUserDetailVO;
import com.accounting.vo.AppUserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface AppUserService {

    Page<AppUserVO> page(AppUserQueryDTO dto);

    AppUserDetailVO getUserDetail(Long userId);
}
