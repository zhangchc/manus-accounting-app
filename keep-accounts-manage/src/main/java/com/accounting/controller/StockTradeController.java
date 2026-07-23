package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.StockTradeConfigSaveDTO;
import com.accounting.dto.StockTradeRecordSaveDTO;
import com.accounting.service.StockTradeConfigService;
import com.accounting.service.StockTradeRecordService;
import com.accounting.vo.StockTradeConfigVO;
import com.accounting.vo.StockTradeOperationVO;
import com.accounting.vo.StockTradeRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 做T管理 Controller
 */
@RestController
@RequestMapping("/stock/trade")
public class StockTradeController {

    @Autowired
    private StockTradeConfigService configService;

    @Autowired
    private StockTradeRecordService recordService;

    /**
     * 查询T配置列表
     */
    @GetMapping("/config/list")
    public Result<List<StockTradeConfigVO>> configList() {
        return Result.success(configService.list());
    }

    /**
     * 根据股票代码查询T配置（新增页面联动回填）
     */
    @GetMapping("/config/query")
    public Result<StockTradeConfigVO> configQuery(@RequestParam String stockCode) {
        return Result.success(configService.queryByStockCode(stockCode));
    }

    /**
     * 保存T配置（新增 / 修改）
     */
    @PostMapping("/config/save")
    public Result<Void> configSave(@Valid @RequestBody StockTradeConfigSaveDTO dto) {
        configService.save(dto);
        return Result.success();
    }

    /**
     * 查询档位列表（含触发状态）
     */
    @GetMapping("/operation/list")
    public Result<List<StockTradeOperationVO>> operationList(@RequestParam Long configId) {
        return Result.success(recordService.listOperations(configId));
    }

    /**
     * 新增交易记录
     */
    @PostMapping("/record/save")
    public Result<Void> recordSave(@Valid @RequestBody StockTradeRecordSaveDTO dto) {
        recordService.saveRecord(dto);
        return Result.success();
    }

    /**
     * 查询交易流水（含 FIFO 配对盈亏）
     */
    @GetMapping("/record/list")
    public Result<List<StockTradeRecordVO>> recordList(@RequestParam Long configId) {
        return Result.success(recordService.listRecords(configId));
    }
}
