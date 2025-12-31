package com.ruoyi.project.caseapp.location.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.caseapp.location.service.ILocationTrajectoryService;

/**
 * 货架位置轨迹查询Controller
 *
 * @author ruoyi
 * @date 2025-12-31
 */
@Controller
@RequestMapping("/caseapp/location")
public class LocationTrajectoryController extends BaseController
{
    private String prefix = "caseapp/location";

    @Autowired
    private ILocationTrajectoryService locationTrajectoryService;

    /**
     * 跳转到货架位置轨迹查询页面
     */
    @GetMapping("/trajectory")
    public String trajectory()
    {
        return prefix + "/trajectory";
    }

    /**
     * 查询轨迹数据
     */
    @PostMapping("/queryTrajectory")
    @ResponseBody
    public AjaxResult queryTrajectory(
            @RequestParam("locationCode") String locationCode,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam(value = "areaName", required = false) String areaName)
    {
        try
        {
            List<Map<String, Object>> trajectories = locationTrajectoryService.queryTrajectoryByLocation(
                    locationCode, startTime, endTime, areaName);

            return AjaxResult.success(trajectories);
        }
        catch (Exception e)
        {
            logger.error("查询位置轨迹失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取货架位置列表
     */
    @GetMapping("/getLocationList")
    @ResponseBody
    public AjaxResult getLocationList(@RequestParam(value = "areaName", required = false) String areaName)
    {
        try
        {
            List<Map<String, Object>> locationList = locationTrajectoryService.getLocationList(areaName);
            return AjaxResult.success(locationList);
        }
        catch (Exception e)
        {
            logger.error("获取位置列表失败", e);
            return AjaxResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取区域列表
     */
    @GetMapping("/getAreaList")
    @ResponseBody
    public AjaxResult getAreaList()
    {
        try
        {
            List<Map<String, Object>> areaList = locationTrajectoryService.getAreaList();
            return AjaxResult.success(areaList);
        }
        catch (Exception e)
        {
            logger.error("获取区域列表失败", e);
            return AjaxResult.error("获取失败: " + e.getMessage());
        }
    }
}
