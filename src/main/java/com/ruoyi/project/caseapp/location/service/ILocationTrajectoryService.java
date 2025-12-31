package com.ruoyi.project.caseapp.location.service;

import java.util.List;
import java.util.Map;

/**
 * 基于位置的轨迹查询Service接口
 *
 * @author ruoyi
 * @date 2025-12-31
 */
public interface ILocationTrajectoryService
{
    /**
     * 根据货架位置查询人员活动轨迹
     *
     * @param locationCode 位置编码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param areaName 区域名称（可选）
     * @return 轨迹列表
     */
    public List<Map<String, Object>> queryTrajectoryByLocation(String locationCode, String startTime, String endTime, String areaName);

    /**
     * 获取所有货架位置列表
     *
     * @param areaName 区域名称（可选，用于筛选）
     * @return 货架位置列表
     */
    public List<Map<String, Object>> getLocationList(String areaName);

    /**
     * 获取所有区域列表
     *
     * @return 区域列表
     */
    public List<Map<String, Object>> getAreaList();
}
