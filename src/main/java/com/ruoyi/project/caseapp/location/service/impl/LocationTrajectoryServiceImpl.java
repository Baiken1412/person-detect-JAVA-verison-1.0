package com.ruoyi.project.caseapp.location.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.project.caseapp.track.domain.AppTrack;
import com.ruoyi.project.caseapp.track.mapper.AppTrackMapper;
import com.ruoyi.project.caseapp.location.domain.AppLocation;
import com.ruoyi.project.caseapp.location.mapper.AppLocationMapper;
import com.ruoyi.project.caseapp.location.mapper.AppCameraLocationRelMapper;
import com.ruoyi.project.caseapp.location.service.ILocationTrajectoryService;

/**
 * 基于位置的轨迹查询Service实现
 *
 * @author ruoyi
 * @date 2025-12-31
 */
@Service
public class LocationTrajectoryServiceImpl implements ILocationTrajectoryService
{
    private static final Logger log = LoggerFactory.getLogger(LocationTrajectoryServiceImpl.class);

    @Autowired
    private AppLocationMapper appLocationMapper;

    @Autowired
    private AppCameraLocationRelMapper cameraLocationRelMapper;

    @Autowired
    private AppTrackMapper appTrackMapper;

    /**
     * 根据货架位置查询人员活动轨迹
     */
    @Override
    public List<Map<String, Object>> queryTrajectoryByLocation(String locationCode, String startTime, String endTime, String areaName)
    {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try
        {
            // 1. 根据位置编码查询位置信息
            AppLocation location = appLocationMapper.selectByLocationCode(locationCode);
            if (location == null)
            {
                log.warn("未找到位置编码: {}", locationCode);
                return resultList;
            }

            // 2. 查询该位置关联的所有摄像头区域名称（gnslx）
            List<String> qymcList = cameraLocationRelMapper.selectCameraQymcByLocationId(location.getLocationId());
            if (qymcList == null || qymcList.isEmpty())
            {
                log.warn("位置 {} 未关联任何摄像头", locationCode);
                return resultList;
            }

            log.info("位置编码: {}, 关联的摄像头区域: {}", locationCode, qymcList);

            // 3. 查询这些区域在指定时间段内的轨迹数据
            for (String qymc : qymcList)
            {
                AppTrack query = new AppTrack();
                query.setQymc(qymc);

                // 设置时间范围查询参数
                Map<String, Object> params = new HashMap<>();
                params.put("beginPssj", startTime);
                params.put("endPssj", endTime);
                query.setParams(params);

                List<AppTrack> trajectories = appTrackMapper.selectAppTrackList(query);

                log.info("区域: {}, 时间范围: {} ~ {}, 查询到轨迹数: {}",
                        qymc, startTime, endTime, trajectories.size());

                // 4. 转换为前端需要的格式
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (AppTrack track : trajectories)
                {
                    Map<String, Object> item = new HashMap<>();

                    // 轨迹基本信息
                    item.put("id", "TJ" + track.getId());
                    item.put("locationCode", locationCode);
                    item.put("locationName", location.getLocationName());
                    item.put("areaName", location.getAreaName());
                    item.put("cameraName", qymc);

                    // 时间信息
                    String start = track.getPssj() != null ? sdf.format(track.getPssj()) : "";
                    String end = track.getJssj() != null ? sdf.format(track.getJssj()) : start;
                    item.put("startTime", start);
                    item.put("endTime", end);

                    // 计算持续时长
                    if (track.getPssj() != null && track.getJssj() != null)
                    {
                        long duration = (track.getJssj().getTime() - track.getPssj().getTime()) / 1000;
                        item.put("duration", formatDuration(duration));
                    }
                    else
                    {
                        item.put("duration", "未知");
                    }

                    // 关键帧图片
                    item.put("keyframeUrl", track.getPstp() != null ? track.getPstp() : "");

                    // 视频信息
                    item.put("hasVideo", track.getSpdz() != null && !track.getSpdz().isEmpty());
                    item.put("videoUrl", track.getSpdz() != null ? track.getSpdz() : "");
                    item.put("videoCount", track.getSpdz() != null && !track.getSpdz().isEmpty() ? 1 : 0);

                    // 截图列表（目前只有一张关键帧，可以扩展）
                    List<Map<String, Object>> screenshots = new ArrayList<>();
                    if (track.getPstp() != null && !track.getPstp().isEmpty())
                    {
                        Map<String, Object> screenshot = new HashMap<>();
                        screenshot.put("url", track.getPstp());
                        screenshot.put("time", start);
                        screenshot.put("index", 1);
                        screenshots.add(screenshot);
                    }
                    item.put("screenshots", screenshots);

                    // 其他信息
                    Integer rysl = track.getRysl();
                    item.put("rysl", rysl != null ? rysl.intValue() : 0);
                    item.put("bzzt", track.getBzzt() != null ? track.getBzzt() : "0");

                    resultList.add(item);
                }
            }
        }
        catch (Exception e)
        {
            log.error("查询位置轨迹失败", e);
        }

        return resultList;
    }

    /**
     * 获取所有货架位置列表
     */
    @Override
    public List<Map<String, Object>> getLocationList(String areaName)
    {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try
        {
            AppLocation query = new AppLocation();
            if (areaName != null && !areaName.isEmpty())
            {
                query.setAreaName(areaName);
            }

            List<AppLocation> locations = appLocationMapper.selectAppLocationList(query);

            for (AppLocation location : locations)
            {
                Map<String, Object> item = new HashMap<>();
                item.put("value", location.getLocationCode());
                item.put("label", location.getLocationCode() + " - " + location.getLocationName());
                item.put("locationCode", location.getLocationCode());
                item.put("locationName", location.getLocationName());
                item.put("areaName", location.getAreaName());
                resultList.add(item);
            }
        }
        catch (Exception e)
        {
            log.error("获取位置列表失败", e);
        }

        return resultList;
    }

    /**
     * 获取所有区域列表
     */
    @Override
    public List<Map<String, Object>> getAreaList()
    {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try
        {
            List<AppLocation> locations = appLocationMapper.selectAppLocationList(new AppLocation());

            // 去重区域
            Map<String, String> areaMap = new HashMap<>();
            for (AppLocation location : locations)
            {
                if (location.getAreaName() != null)
                {
                    areaMap.put(location.getAreaName(), location.getAreaName());
                }
            }

            for (Map.Entry<String, String> entry : areaMap.entrySet())
            {
                Map<String, Object> item = new HashMap<>();
                item.put("value", entry.getKey());
                item.put("label", entry.getValue());
                resultList.add(item);
            }
        }
        catch (Exception e)
        {
            log.error("获取区域列表失败", e);
        }

        return resultList;
    }

    /**
     * 格式化时长
     */
    private String formatDuration(long seconds)
    {
        if (seconds < 60)
        {
            return seconds + "秒";
        }
        else if (seconds < 3600)
        {
            long minutes = seconds / 60;
            long remainSeconds = seconds % 60;
            return minutes + "分" + (remainSeconds > 0 ? remainSeconds + "秒" : "");
        }
        else
        {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分" : "");
        }
    }
}
