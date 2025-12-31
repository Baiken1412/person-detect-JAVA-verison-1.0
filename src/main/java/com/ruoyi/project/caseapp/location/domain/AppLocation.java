package com.ruoyi.project.caseapp.location.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 货架位置对象 app_location
 *
 * @author ruoyi
 * @date 2025-12-31
 */
public class AppLocation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 位置主键ID */
    private Long locationId;

    /** 位置编码，如 A01-03-02 */
    private String locationCode;

    /** 位置名称 */
    private String locationName;

    /** 所属区域，如 A区、B区 */
    private String areaName;

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
