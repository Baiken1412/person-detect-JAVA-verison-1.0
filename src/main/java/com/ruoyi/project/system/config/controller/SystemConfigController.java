package com.ruoyi.project.system.config.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.config.SystemConfig;
import com.ruoyi.framework.web.domain.AjaxResult;

/**
 * 系统配置Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/config")
public class SystemConfigController
{
    @Autowired
    private SystemConfig systemConfig;

    /**
     * 获取系统配置
     */
    @GetMapping("/info")
    public AjaxResult getSystemConfig()
    {
        Map<String, String> config = new HashMap<>();
        config.put("title", systemConfig.getTitle());
        config.put("subtitle", systemConfig.getSubtitle());
        return AjaxResult.success(config);
    }
}
