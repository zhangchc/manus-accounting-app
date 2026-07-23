package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.StockPositionAddDTO;
import com.accounting.dto.StockPositionQueryDTO;
import com.accounting.dto.StockPositionSaveDTO;
import com.accounting.service.StockPositionService;
import com.accounting.vo.StockPositionSummaryVO;
import com.accounting.vo.StockPositionVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 股票持仓 Controller
 */
@RestController
@RequestMapping("/stock/position")
public class StockController {

    @Autowired
    private StockPositionService stockPositionService;

    /**
     * 持仓概览汇总查询
     */
    @GetMapping("/summary")
    public Result<StockPositionSummaryVO> summary() {
        return Result.success(stockPositionService.getSummary());
    }

    /**
     * 新增持仓
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody StockPositionAddDTO dto) {
        stockPositionService.add(dto);
        return Result.success();
    }

    /**
     * 分页查询持仓列表
     */
    @GetMapping("/list")
    public Result<Page<StockPositionVO>> list(StockPositionQueryDTO dto) {
        return Result.success(stockPositionService.list(dto));
    }

    /**
     * 修改 / 删除持仓
     */
    @PostMapping("/update")
    public Result<Void> save(@Valid @RequestBody StockPositionSaveDTO dto) {
        stockPositionService.save(dto);
        return Result.success();
    }
}
