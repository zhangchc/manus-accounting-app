package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.RecordQueryDTO;
import com.accounting.service.RecordService;
import com.accounting.vo.CategorySimpleVO;
import com.accounting.vo.RecordVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @GetMapping("/page")
    public Result<Page<RecordVO>> page(RecordQueryDTO dto) {
        return Result.success(recordService.page(dto));
    }

    @GetMapping("/categories")
    public Result<List<CategorySimpleVO>> categories() {
        return Result.success(recordService.getCategories());
    }

    @GetMapping("/export")
    public void export(RecordQueryDTO dto, HttpServletResponse response) {
        recordService.export(dto, response);
    }
}
