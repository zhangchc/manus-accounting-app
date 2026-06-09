package com.accounting.service.impl;

import com.accounting.common.BusinessException;
import com.accounting.dto.RecordDTO;
import com.accounting.entity.Book;
import com.accounting.entity.Budget;
import com.accounting.entity.Record;
import com.accounting.mapper.RecordMapper;
import com.accounting.service.BookService;
import com.accounting.service.BudgetService;
import com.accounting.service.RecordService;
import com.accounting.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 记账记录服务实现
 */
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Autowired
    @Lazy
    private BudgetService budgetService;

    @Autowired
    @Lazy
    private BookService bookService;

    private static final String[] WEEK_DAYS = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    @Override
    public Record addRecord(Long userId, RecordDTO recordDTO) {
        Record record = new Record();
        record.setUserId(userId);

        // 如果没有传bookId，自动获取默认账本
        Long bookId = recordDTO.getBookId();
        if (bookId == null) {
            Book defaultBook = bookService.getDefaultBook(userId);
            if (defaultBook != null) {
                bookId = defaultBook.getId();
            } else {
                throw new BusinessException("请先创建一个账本");
            }
        }
        record.setBookId(bookId);

        record.setCategoryId(recordDTO.getCategoryId());
        record.setType(recordDTO.getType());
        record.setAmount(recordDTO.getAmount());
        record.setRemark(recordDTO.getRemark() != null ? recordDTO.getRemark() : "");

        LocalDate recordDate = LocalDate.parse(recordDTO.getRecordDate());
        record.setRecordDate(recordDate);

        if (recordDTO.getRecordTime() != null && !recordDTO.getRecordTime().isEmpty()) {
            record.setRecordTime(LocalDateTime.parse(recordDTO.getRecordDate() + "T" + recordDTO.getRecordTime()));
        } else {
            record.setRecordTime(LocalDateTime.now());
        }

        this.save(record);
        return record;
    }

    @Override
    public void updateRecord(Long userId, RecordDTO recordDTO) {
        if (recordDTO.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        Record existRecord = this.getById(recordDTO.getId());
        if (existRecord == null || !existRecord.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }

        existRecord.setCategoryId(recordDTO.getCategoryId());
        existRecord.setType(recordDTO.getType());
        existRecord.setAmount(recordDTO.getAmount());
        existRecord.setRemark(recordDTO.getRemark() != null ? recordDTO.getRemark() : "");

        if (recordDTO.getBookId() != null) {
            existRecord.setBookId(recordDTO.getBookId());
        }

        if (recordDTO.getRecordDate() != null) {
            LocalDate recordDate = LocalDate.parse(recordDTO.getRecordDate());
            existRecord.setRecordDate(recordDate);
        }
        if (recordDTO.getRecordTime() != null && !recordDTO.getRecordTime().isEmpty()) {
            existRecord.setRecordTime(LocalDateTime.parse(recordDTO.getRecordDate() + "T" + recordDTO.getRecordTime()));
        }

        this.updateById(existRecord);
    }

    @Override
    public void deleteRecord(Long userId, Long recordId) {
        Record record = this.getById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        this.removeById(recordId);
    }

    @Override
    public MonthBillVO getMonthBill(Long userId, Long bookId, String yearMonth) {
        MonthBillVO monthBill = new MonthBillVO();
        monthBill.setYearMonth(yearMonth);

        // 查询当月所有记录
        List<RecordVO> records = baseMapper.selectRecordList(userId, bookId, yearMonth, null);

        // 计算月收入和支出
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (RecordVO record : records) {
            if (record.getType() == 2) {
                totalIncome = totalIncome.add(record.getAmount());
            } else {
                totalExpense = totalExpense.add(record.getAmount());
            }
        }
        monthBill.setTotalIncome(totalIncome);
        monthBill.setTotalExpense(totalExpense);
        monthBill.setBalance(totalIncome.subtract(totalExpense));

        // 查询预算
        Budget budget = budgetService.getBudget(userId, null, yearMonth);
        if (budget != null) {
            monthBill.setBudget(budget.getAmount());
            monthBill.setBudgetRemain(budget.getAmount().subtract(totalExpense));
        } else {
            monthBill.setBudget(BigDecimal.ZERO);
            monthBill.setBudgetRemain(BigDecimal.ZERO);
        }

        // 按日分组
        Map<String, List<RecordVO>> dateMap = records.stream()
                .collect(Collectors.groupingBy(RecordVO::getRecordDate, LinkedHashMap::new, Collectors.toList()));

        List<DailyBillVO> dailyList = new ArrayList<>();
        for (Map.Entry<String, List<RecordVO>> entry : dateMap.entrySet()) {
            DailyBillVO daily = new DailyBillVO();
            daily.setDate(entry.getKey());

            // 计算星期
            LocalDate date = LocalDate.parse(entry.getKey());
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            daily.setWeekDay(WEEK_DAYS[dayOfWeek.getValue() % 7]);

            BigDecimal dayIncome = BigDecimal.ZERO;
            BigDecimal dayExpense = BigDecimal.ZERO;
            for (RecordVO record : entry.getValue()) {
                if (record.getType() == 2) {
                    dayIncome = dayIncome.add(record.getAmount());
                } else {
                    dayExpense = dayExpense.add(record.getAmount());
                }
            }
            daily.setDayIncome(dayIncome);
            daily.setDayExpense(dayExpense);
            daily.setRecords(entry.getValue());
            dailyList.add(daily);
        }
        monthBill.setDailyList(dailyList);

        return monthBill;
    }

    @Override
    public StatisticsVO getStatistics(Long userId, String yearMonth, Integer type) {
        StatisticsVO statistics = new StatisticsVO();

        // 查询当月记录
        List<RecordVO> records = baseMapper.selectRecordList(userId, null, yearMonth, null);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (RecordVO record : records) {
            if (record.getType() == 2) {
                totalIncome = totalIncome.add(record.getAmount());
            } else {
                totalExpense = totalExpense.add(record.getAmount());
            }
        }
        statistics.setTotalIncome(totalIncome);
        statistics.setTotalExpense(totalExpense);
        statistics.setBalance(totalIncome.subtract(totalExpense));

        // 分类统计
        List<RecordVO> categoryStats = baseMapper.selectCategoryStats(userId, yearMonth, type);
        BigDecimal totalAmount = type == 1 ? totalExpense : totalIncome;

        List<StatisticsVO.CategoryStatVO> categoryList = new ArrayList<>();
        for (RecordVO stat : categoryStats) {
            StatisticsVO.CategoryStatVO catStat = new StatisticsVO.CategoryStatVO();
            catStat.setCategoryId(stat.getCategoryId());
            catStat.setCategoryName(stat.getCategoryName());
            catStat.setCategoryIcon(stat.getCategoryIcon());
            catStat.setAmount(stat.getAmount());
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                catStat.setPercent(stat.getAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(totalAmount, 1, RoundingMode.HALF_UP)
                        .toString() + "%");
            } else {
                catStat.setPercent("0%");
            }
            categoryList.add(catStat);
        }
        statistics.setCategoryList(categoryList);

        // 日趋势
        List<RecordVO> dayStats = baseMapper.selectDayStats(userId, yearMonth, type);
        YearMonth ym = YearMonth.parse(yearMonth);
        int daysInMonth = ym.lengthOfMonth();

        Map<String, BigDecimal> dayMap = new HashMap<>();
        for (RecordVO stat : dayStats) {
            dayMap.put(stat.getRecordDate(), stat.getAmount());
        }

        List<StatisticsVO.DayTrendVO> trendList = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            StatisticsVO.DayTrendVO trend = new StatisticsVO.DayTrendVO();
            String dateStr = String.format("%s-%02d", yearMonth, i);
            trend.setDate(dateStr);
            trend.setAmount(dayMap.getOrDefault(dateStr, BigDecimal.ZERO));
            trendList.add(trend);
        }
        statistics.setTrendList(trendList);

        return statistics;
    }

    @Override
    public Map<String, Object> getYearSummary(Long userId, String year) {
        Map<String, Object> result = new HashMap<>();

        BigDecimal yearIncome = BigDecimal.ZERO;
        BigDecimal yearExpense = BigDecimal.ZERO;
        List<Map<String, Object>> monthList = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            String yearMonth = String.format("%s-%02d", year, m);
            List<RecordVO> records = baseMapper.selectRecordList(userId, null, yearMonth, null);

            BigDecimal monthIncome = BigDecimal.ZERO;
            BigDecimal monthExpense = BigDecimal.ZERO;
            for (RecordVO record : records) {
                if (record.getType() == 2) {
                    monthIncome = monthIncome.add(record.getAmount());
                } else {
                    monthExpense = monthExpense.add(record.getAmount());
                }
            }

            yearIncome = yearIncome.add(monthIncome);
            yearExpense = yearExpense.add(monthExpense);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", yearMonth);
            monthData.put("income", monthIncome);
            monthData.put("expense", monthExpense);
            monthData.put("balance", monthIncome.subtract(monthExpense));
            monthList.add(monthData);
        }

        result.put("year", year);
        result.put("totalIncome", yearIncome);
        result.put("totalExpense", yearExpense);
        result.put("balance", yearIncome.subtract(yearExpense));
        result.put("monthList", monthList);

        return result;
    }

    @Override
    public BigDecimal getTodayExpense(Long userId) {
        LocalDate today = LocalDate.now();
        List<Record> records = this.list(new LambdaQueryWrapper<Record>()
                .eq(Record::getUserId, userId)
                .eq(Record::getType, 1)
                .eq(Record::getRecordDate, today));

        BigDecimal total = BigDecimal.ZERO;
        for (Record record : records) {
            total = total.add(record.getAmount());
        }
        return total;
    }
}
