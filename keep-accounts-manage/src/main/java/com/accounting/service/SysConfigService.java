package com.accounting.service;

/**
 * 系统配置服务接口
 */
public interface SysConfigService {

    /**
     * 根据 key 获取配置值
     *
     * @param key 配置键
     * @return 配置值，不存在返回 null
     */
    String getByKey(String key);

    /**
     * 保存配置（存在则更新，不存在则新增）
     *
     * @param key   配置键
     * @param value 配置值
     */
    void save(String key, String value);
}
