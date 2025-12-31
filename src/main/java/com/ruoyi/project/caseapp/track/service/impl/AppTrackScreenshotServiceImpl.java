package com.ruoyi.project.caseapp.track.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project.caseapp.track.mapper.AppTrackScreenshotMapper;
import com.ruoyi.project.caseapp.track.domain.AppTrackScreenshot;
import com.ruoyi.project.caseapp.track.service.IAppTrackScreenshotService;

/**
 * 活动轨迹截图Service业务层处理
 *
 * @author ruoyi
 * @date 2025-12-31
 */
@Service
public class AppTrackScreenshotServiceImpl implements IAppTrackScreenshotService
{
    @Autowired
    private AppTrackScreenshotMapper appTrackScreenshotMapper;

    /**
     * 查询活动轨迹截图
     *
     * @param id 活动轨迹截图主键
     * @return 活动轨迹截图
     */
    @Override
    public AppTrackScreenshot selectAppTrackScreenshotById(Long id)
    {
        return appTrackScreenshotMapper.selectAppTrackScreenshotById(id);
    }

    /**
     * 根据轨迹ID查询所有截图列表
     *
     * @param trackId 活动轨迹ID
     * @return 活动轨迹截图集合
     */
    @Override
    public List<AppTrackScreenshot> selectScreenshotsByTrackId(Long trackId)
    {
        return appTrackScreenshotMapper.selectScreenshotsByTrackId(trackId);
    }

    /**
     * 查询活动轨迹截图列表
     *
     * @param appTrackScreenshot 活动轨迹截图
     * @return 活动轨迹截图
     */
    @Override
    public List<AppTrackScreenshot> selectAppTrackScreenshotList(AppTrackScreenshot appTrackScreenshot)
    {
        return appTrackScreenshotMapper.selectAppTrackScreenshotList(appTrackScreenshot);
    }

    /**
     * 新增活动轨迹截图
     *
     * @param appTrackScreenshot 活动轨迹截图
     * @return 结果
     */
    @Override
    public int insertAppTrackScreenshot(AppTrackScreenshot appTrackScreenshot)
    {
        return appTrackScreenshotMapper.insertAppTrackScreenshot(appTrackScreenshot);
    }

    /**
     * 修改活动轨迹截图
     *
     * @param appTrackScreenshot 活动轨迹截图
     * @return 结果
     */
    @Override
    public int updateAppTrackScreenshot(AppTrackScreenshot appTrackScreenshot)
    {
        return appTrackScreenshotMapper.updateAppTrackScreenshot(appTrackScreenshot);
    }

    /**
     * 批量删除活动轨迹截图
     *
     * @param ids 需要删除的活动轨迹截图主键
     * @return 结果
     */
    @Override
    public int deleteAppTrackScreenshotByIds(Long[] ids)
    {
        return appTrackScreenshotMapper.deleteAppTrackScreenshotByIds(ids);
    }

    /**
     * 删除活动轨迹截图信息
     *
     * @param id 活动轨迹截图主键
     * @return 结果
     */
    @Override
    public int deleteAppTrackScreenshotById(Long id)
    {
        return appTrackScreenshotMapper.deleteAppTrackScreenshotById(id);
    }
}
