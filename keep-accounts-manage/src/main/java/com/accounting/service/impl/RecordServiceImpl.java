package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.accounting.dto.RecordQueryDTO;
import com.accounting.entity.AppCategory;
import com.accounting.entity.AppUser;
import com.accounting.entity.Book;
import com.accounting.entity.Record;
import com.accounting.mapper.AppCategoryMapper;
import com.accounting.mapper.AppUserMapper;
import com.accounting.mapper.BookMapper;
import com.accounting.mapper.RecordMapper;
import com.accounting.service.RecordService;
import com.accounting.vo.CategorySimpleVO;
import com.accounting.vo.RecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl implements RecordService {

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private AppCategoryMapper appCategoryMapper;

    @Autowired
    private BookMapper bookMapper;

    @Override
    public Page<RecordVO> page(RecordQueryDTO dto) {
        LambdaQueryWrapper<Record> wrapper = buildQueryWrapper(dto);
        Page<Record> pageParam = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Record> pageResult;
        if (wrapper == null) {
            pageResult = new Page<>(dto.getPage(), dto.getPageSize());
            pageResult.setRecords(Collections.<Record>emptyList());
            pageResult.setTotal(0);
        } else {
            pageResult = recordMapper.selectPage(pageParam, wrapper);
        }

        List<RecordVO> voList = toVoList(pageResult.getRecords());
        Page<RecordVO> voPage = new Page<>();
        BeanUtil.copyProperties(pageResult, voPage);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<CategorySimpleVO> getCategories() {
        LambdaQueryWrapper<AppCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AppCategory::getSortOrder, AppCategory::getId);
        List<AppCategory> categories = appCategoryMapper.selectList(wrapper);
        return categories.stream().map(c -> {
            CategorySimpleVO vo = new CategorySimpleVO();
            BeanUtil.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void export(RecordQueryDTO dto, HttpServletResponse response) {
        LambdaQueryWrapper<Record> wrapper = buildQueryWrapper(dto);
        List<Record> records;
        if (wrapper == null) {
            records = Collections.emptyList();
        } else {
            records = recordMapper.selectList(wrapper);
        }

        List<RecordVO> voList = toVoList(records);

        List<Map<String, Object>> exportData = voList.stream().map(vo -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userNickName", vo.getUserNickName());
            row.put("categoryName", vo.getCategoryName());
            row.put("type", vo.getType() != null && vo.getType() == 1 ? "支出" : "收入");
            row.put("amount", vo.getAmount() != null ? vo.getAmount().doubleValue() : 0);
            row.put("remark", vo.getRemark());
            row.put("bookName", vo.getBookName());
            row.put("recordTime", vo.getRecordTime());
            return row;
        }).collect(Collectors.toList());

        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("userNickName", "用户昵称");
        writer.addHeaderAlias("categoryName", "分类");
        writer.addHeaderAlias("type", "类型");
        writer.addHeaderAlias("amount", "金额");
        writer.addHeaderAlias("remark", "备注");
        writer.addHeaderAlias("bookName", "账本");
        writer.addHeaderAlias("recordTime", "记账时间");
        writer.setOnlyAlias(true);
        writer.write(exportData, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=record_export.xlsx");
        try {
            writer.flush(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        } finally {
            writer.close();
        }
    }

    private LambdaQueryWrapper<Record> buildQueryWrapper(RecordQueryDTO dto) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(dto.getUserNickName())) {
            LambdaQueryWrapper<AppUser> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(AppUser::getNickName, dto.getUserNickName());
            List<AppUser> users = appUserMapper.selectList(userWrapper);
            if (users.isEmpty()) {
                return null;
            }
            List<Long> userIds = users.stream().map(AppUser::getId).collect(Collectors.toList());
            wrapper.in(Record::getUserId, userIds);
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(Record::getCategoryId, dto.getCategoryId());
        }
        if (dto.getType() != null) {
            wrapper.eq(Record::getType, dto.getType());
        }
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            wrapper.ge(Record::getRecordDate, dto.getStartDate());
        }
        if (StrUtil.isNotBlank(dto.getEndDate())) {
            wrapper.le(Record::getRecordDate, dto.getEndDate());
        }
        if (dto.getMinAmount() != null) {
            wrapper.ge(Record::getAmount, dto.getMinAmount());
        }
        if (dto.getMaxAmount() != null) {
            wrapper.le(Record::getAmount, dto.getMaxAmount());
        }
        wrapper.orderByDesc(Record::getRecordTime);
        return wrapper;
    }

    private List<RecordVO> toVoList(List<Record> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = records.stream().map(Record::getUserId).collect(Collectors.toSet());
        Set<Long> categoryIds = records.stream().map(Record::getCategoryId).collect(Collectors.toSet());
        Set<Long> bookIds = records.stream().map(Record::getBookId).collect(Collectors.toSet());

        Map<Long, AppUser> userMap;
        if (!userIds.isEmpty()) {
            List<AppUser> users = appUserMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(AppUser::getId, u -> u));
        } else {
            userMap = Collections.emptyMap();
        }

        Map<Long, AppCategory> categoryMap;
        if (!categoryIds.isEmpty()) {
            List<AppCategory> categories = appCategoryMapper.selectBatchIds(categoryIds);
            categoryMap = categories.stream().collect(Collectors.toMap(AppCategory::getId, c -> c));
        } else {
            categoryMap = Collections.emptyMap();
        }

        Map<Long, Book> bookMap;
        if (!bookIds.isEmpty()) {
            List<Book> books = bookMapper.selectBatchIds(bookIds);
            bookMap = books.stream().collect(Collectors.toMap(Book::getId, b -> b));
        } else {
            bookMap = Collections.emptyMap();
        }

        return records.stream().map(r -> {
            RecordVO vo = new RecordVO();
            vo.setId(r.getId());
            vo.setCategoryId(r.getCategoryId());
            vo.setType(r.getType());
            vo.setAmount(r.getAmount());
            vo.setRemark(r.getRemark());
            vo.setBookId(r.getBookId());
            if (r.getRecordTime() != null) {
                vo.setRecordTime(r.getRecordTime().toString().replace("T", " "));
            }
            AppUser user = userMap.get(r.getUserId());
            if (user != null) {
                vo.setUserNickName(user.getNickName());
                vo.setUserAvatarUrl(user.getAvatarUrl());
            }
            AppCategory cat = categoryMap.get(r.getCategoryId());
            if (cat != null) {
                vo.setCategoryName(cat.getName());
                vo.setCategoryIcon(cat.getIcon());
            }
            Book book = bookMap.get(r.getBookId());
            if (book != null) {
                vo.setBookName(book.getName());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
