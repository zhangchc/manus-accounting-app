package com.accounting.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.accounting.dto.AppUserQueryDTO;
import com.accounting.entity.AppCategory;
import com.accounting.entity.AppUser;
import com.accounting.entity.Book;
import com.accounting.entity.Record;
import com.accounting.mapper.AppCategoryMapper;
import com.accounting.mapper.AppUserMapper;
import com.accounting.mapper.BookMapper;
import com.accounting.mapper.RecordMapper;
import com.accounting.service.AppUserService;
import com.accounting.vo.AppUserDetailVO;
import com.accounting.vo.AppUserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private AppCategoryMapper appCategoryMapper;

    @Override
    public Page<AppUserVO> page(AppUserQueryDTO dto) {
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(dto.getNickName())) {
            wrapper.like(AppUser::getNickName, dto.getNickName());
        }
        if (StrUtil.isNotBlank(dto.getOpenId())) {
            wrapper.like(AppUser::getOpenId, dto.getOpenId());
        }
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            wrapper.ge(AppUser::getCreateTime, dto.getStartDate() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(dto.getEndDate())) {
            wrapper.le(AppUser::getCreateTime, dto.getEndDate() + " 23:59:59");
        }
        wrapper.orderByDesc(AppUser::getCreateTime);

        Page<AppUser> pageParam = new Page<>(dto.getPage(), dto.getPageSize());
        Page<AppUser> pageResult = appUserMapper.selectPage(pageParam, wrapper);

        List<AppUserVO> voList = pageResult.getRecords().stream().map(entity -> {
            AppUserVO vo = new AppUserVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());

        Page<AppUserVO> voPage = new Page<>();
        BeanUtil.copyProperties(pageResult, voPage);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AppUserDetailVO getUserDetail(Long userId) {
        AppUserDetailVO detail = new AppUserDetailVO();

        // 总记账数
        LambdaQueryWrapper<Record> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(Record::getUserId, userId);
        detail.setTotalRecords(recordMapper.selectCount(recordWrapper));

        // 账本数
        LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.eq(Book::getUserId, userId);
        detail.setBookCount(bookMapper.selectCount(bookWrapper));

        // 总支出 (type=1)
        detail.setTotalExpense(sumAmount(userId, 1));

        // 总收入 (type=2)
        detail.setTotalIncome(sumAmount(userId, 2));

        // 最近10条记录
        LambdaQueryWrapper<Record> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(Record::getUserId, userId)
                .orderByDesc(Record::getRecordTime)
                .last("LIMIT 10");
        List<Record> recentRecords = recordMapper.selectList(recentWrapper);

        // 批量查分类
        Set<Long> categoryIds = recentRecords.stream()
                .map(Record::getCategoryId)
                .collect(Collectors.toSet());
        final Map<Long, AppCategory> categoryMap;
        if (!categoryIds.isEmpty()) {
            List<AppCategory> categories = appCategoryMapper.selectBatchIds(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(AppCategory::getId, c -> c));
        } else {
            categoryMap = Collections.emptyMap();
        }

        List<AppUserDetailVO.RecordItemVO> recordItems = recentRecords.stream().map(r -> {
            AppUserDetailVO.RecordItemVO item = new AppUserDetailVO.RecordItemVO();
            item.setId(r.getId());
            item.setType(r.getType());
            item.setAmount(r.getAmount());
            item.setRemark(r.getRemark());
            if (r.getRecordTime() != null) {
                item.setRecordTime(r.getRecordTime().toString().replace("T", " "));
            }
            AppCategory cat = categoryMap.get(r.getCategoryId());
            if (cat != null) {
                item.setCategoryName(cat.getName());
                item.setCategoryIcon(cat.getIcon());
            }
            return item;
        }).collect(Collectors.toList());

        detail.setRecentRecords(recordItems);
        return detail;
    }

    private BigDecimal sumAmount(Long userId, Integer type) {
        QueryWrapper<Record> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(SUM(amount), 0) as total")
                .eq("user_id", userId)
                .eq("type", type);
        List<Map<String, Object>> maps = recordMapper.selectMaps(wrapper);
        if (maps.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Object total = maps.get(0).get("total");
        if (total instanceof BigDecimal) {
            return (BigDecimal) total;
        }
        return new BigDecimal(total.toString());
    }
}
