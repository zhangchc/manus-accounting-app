package com.accounting.service.impl;

import com.accounting.entity.SysConfig;
import com.accounting.mapper.SysConfigMapper;
import com.accounting.service.SysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统配置服务实现
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper mapper;

    @Override
    public String getByKey(String key) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, key);
        SysConfig config = mapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(String key, String value) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, key);
        SysConfig config = mapper.selectOne(wrapper);
        if (config != null) {
            config.setConfigValue(value);
            mapper.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            mapper.insert(config);
        }
    }
}
