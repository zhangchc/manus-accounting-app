package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.StockPositionDTO;
import com.accounting.service.StockPositionService;
import com.accounting.vo.CostHistoryVO;
import com.accounting.vo.StockPositionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private StockPositionService stockPositionService;

    @GetMapping("/position")
    public Result<StockPositionVO> getPosition() {
        return Result.success(stockPositionService.getPosition());
    }

    @PostMapping("/position")
    public Result<StockPositionVO> saveOrUpdate(@Valid @RequestBody StockPositionDTO dto) {
        return Result.success(stockPositionService.saveOrUpdate(dto));
    }

    @GetMapping("/price")
    public Result<Map<String, Object>> getPrice(@RequestParam String code) {
        return Result.success(stockPositionService.getStockPrice(code));
    }

    @GetMapping("/cost-history")
    public Result<List<CostHistoryVO>> getCostHistory() {
        return Result.success(stockPositionService.getCostHistory());
    }
}
