package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.TradeRecordDTO;
import com.accounting.dto.TradeStrategyDTO;
import com.accounting.service.TradeService;
import com.accounting.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/stock/trade")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @GetMapping("/strategy")
    public Result<TradeStrategyVO> getStrategy() {
        return Result.success(tradeService.getStrategy());
    }

    @PostMapping("/strategy")
    public Result<TradeStrategyVO> saveStrategy(@Valid @RequestBody TradeStrategyDTO dto) {
        return Result.success(tradeService.saveStrategy(dto));
    }

    @GetMapping("/precheck")
    public Result<TradePrecheckVO> precheck(@RequestParam String type,
                                            @RequestParam(required = false) BigDecimal currentPrice) {
        return Result.success(tradeService.precheck(type, currentPrice));
    }

    @PostMapping("/sell")
    public Result<TradeRecordVO> sell(@Valid @RequestBody TradeRecordDTO dto) {
        return Result.success(tradeService.sell(dto));
    }

    @PostMapping("/buy")
    public Result<TradeRecordVO> buy(@Valid @RequestBody TradeRecordDTO dto) {
        return Result.success(tradeService.buy(dto));
    }

    @GetMapping("/records")
    public Result<Page<TradeRecordVO>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(tradeService.getRecords(page, pageSize));
    }

    @GetMapping("/summary")
    public Result<TradeSummaryVO> summary() {
        return Result.success(tradeService.getSummary());
    }

    @PostMapping("/reset")
    public Result<Void> reset() {
        tradeService.reset();
        return Result.success();
    }
}
