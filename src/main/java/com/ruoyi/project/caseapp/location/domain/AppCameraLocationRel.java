package com.ruoyi.project.caseapp.location.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 摄像头与位置关联对象 app_camera_location_rel
 *
 * @author ruoyi
 * @date 2025-12-31
 */
public class AppCameraLocationRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 摄像头ID（关联app_roomip.id） */
    private Long cameraId;

    /** 位置ID（关联app_location.location_id） */
    private Long locationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
