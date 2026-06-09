package com.accounting.service;

import com.accounting.dto.RecordQueryDTO;
import com.accounting.vo.CategorySimpleVO;
import com.accounting.vo.RecordVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface RecordService {

    Page<RecordVO> page(RecordQueryDTO dto);

    List<CategorySimpleVO> getCategories();

    void export(RecordQueryDTO dto, HttpServletResponse response);
}
