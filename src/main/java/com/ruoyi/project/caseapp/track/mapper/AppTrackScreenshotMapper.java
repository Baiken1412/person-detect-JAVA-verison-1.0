package com.ruoyi.project.caseapp.track.mapper;

import java.util.List;
import com.ruoyi.project.caseapp.track.domain.AppTrackScreenshot;

/**
 * 活动轨迹截图Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-31
 */
public interface AppTrackScreenshotMapper
{
    /**
     * 查询活动轨迹截图
     *
     * @param id 活动轨迹截图主键
     * @return 活动轨迹截图
     */
    public AppTrackScreenshot selectAppTrackScreenshotById(Long id);

    /**
     * 根据轨迹ID查询所有截图列表
     *
     * @param trackId 活动轨迹ID
     * @return 活动轨迹截图集合
     */
    public List<AppTrackScreenshot> selectScreenshotsByTrackId(Long trackId);

    /**
     * 查询活动轨迹截图列表
     *
     * @param appTrackScreenshot 活动轨迹截图
     * @return 活动轨迹截图集合
     */
    public List<AppTrackScreenshot> selectAppTrackScreenshotList(AppTrackScreenshot appTrackScreenshot);

    /**
     * 新增活动轨迹截图
     *
     * @param appTrackScreenshot 活动轨迹截图
     * @return 结果
     */
    public int insertAppTrackScreenshot(AppTrackScreenshot appTrackScreenshot);

    /**
     * 修改活动轨迹截图
     *
     * @param appTrackScreenshot 活动轨迹截图
     * @return 结果
     */
    public int updateAppTrackScreenshot(AppTrackScreenshot appTrackScreenshot);

    /**
     * 删除活动轨迹截图
     *
     * @param id 活动轨迹截图主键
     * @return 结果
     */
    public int deleteAppTrackScreenshotById(Long id);

    /**
     * 批量删除活动轨迹截图
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAppTrackScreenshotByIds(Long[] ids);
}
