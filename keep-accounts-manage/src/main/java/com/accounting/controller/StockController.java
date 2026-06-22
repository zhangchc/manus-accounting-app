package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.StockPositionDTO;
import com.accounting.service.StockPositionService;
import com.accounting.vo.StockPositionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
}
