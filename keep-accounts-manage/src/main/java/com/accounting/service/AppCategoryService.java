package com.accounting.service;

import com.accounting.dto.AppCategoryQueryDTO;
import com.accounting.vo.AppCategoryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface AppCategoryService {

    Page<AppCategoryVO> page(AppCategoryQueryDTO dto);
}
