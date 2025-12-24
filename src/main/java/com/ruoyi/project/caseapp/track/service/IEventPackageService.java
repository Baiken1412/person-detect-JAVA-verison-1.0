package com.ruoyi.project.caseapp.track.service;

/**
 * 事件包生成服务接口
 *
 * @author ruoyi
 * @date 2025-12-24
 */
public interface IEventPackageService
{
    /**
     * 生成事件可解释性报告包
     * 生成包含HTML报告、JSON数据、关键帧图片等的ZIP文件
     *
     * @param eventId 复合事件ID（event_id字段，如CE_20251224_100000）
     * @return ZIP文件相对路径（相对于upload目录）
     */
    public String generateEventPackage(Long eventId) throws Exception;

    /**
     * 批量生成事件包
     *
     * @param eventIds 多个事件ID，逗号分隔
     * @return ZIP文件相对路径
     */
    public String generateBatchEventPackages(String eventIds) throws Exception;
}
