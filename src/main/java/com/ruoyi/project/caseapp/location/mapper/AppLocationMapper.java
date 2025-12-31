package com.ruoyi.project.caseapp.location.mapper;

import java.util.List;
import com.ruoyi.project.caseapp.location.domain.AppLocation;

/**
 * 货架位置Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-31
 */
public interface AppLocationMapper
{
    /**
     * 查询货架位置列表
     *
     * @param appLocation 货架位置
     * @return 货架位置集合
     */
    public List<AppLocation> selectAppLocationList(AppLocation appLocation);

    /**
     * 根据位置编码查询
     *
     * @param locationCode 位置编码
     * @return 货架位置
     */
    public AppLocation selectByLocationCode(String locationCode);

    /**
     * 根据位置ID查询
     *
     * @param locationId 位置ID
     * @return 货架位置
     */
    public AppLocation selectByLocationId(Long locationId);
}
