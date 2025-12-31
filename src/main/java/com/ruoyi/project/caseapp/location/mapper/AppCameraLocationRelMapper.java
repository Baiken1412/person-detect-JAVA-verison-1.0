package com.ruoyi.project.caseapp.location.mapper;

import java.util.List;
import com.ruoyi.project.caseapp.location.domain.AppCameraLocationRel;

/**
 * 摄像头与位置关联Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-31
 */
public interface AppCameraLocationRelMapper
{
    /**
     * 根据位置ID查询关联的摄像头ID列表
     *
     * @param locationId 位置ID
     * @return 摄像头ID列表
     */
    public List<Long> selectCameraIdsByLocationId(Long locationId);

    /**
     * 根据摄像头ID查询关联的位置ID列表
     *
     * @param cameraId 摄像头ID
     * @return 位置ID列表
     */
    public List<Long> selectLocationIdsByCameraId(Long cameraId);

    /**
     * 根据位置ID查询关联的摄像头区域名称列表（用于轨迹查询）
     *
     * @param locationId 位置ID
     * @return 区域名称列表（app_roomip.gnslx，对应轨迹表的qymc字段）
     */
    public List<String> selectCameraQymcByLocationId(Long locationId);
}
