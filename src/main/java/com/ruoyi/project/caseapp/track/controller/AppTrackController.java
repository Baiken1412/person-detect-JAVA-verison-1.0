package com.ruoyi.project.caseapp.track.controller;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.project.caseapp.roomip.domain.AppRoomip;
import com.ruoyi.project.caseapp.roomip.service.IAppRoomipService;
import com.ruoyi.project.caseapp.core.util.HikVedioUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.project.system.dict.service.IDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.project.caseapp.track.domain.AppTrack;
import com.ruoyi.project.caseapp.track.domain.AppTrackScreenshot;
import com.ruoyi.project.caseapp.track.domain.CompositeEvent;
import com.ruoyi.project.caseapp.track.domain.DailyReport;
import com.ruoyi.project.caseapp.track.service.IAppTrackService;
import com.ruoyi.project.caseapp.track.service.IAppTrackScreenshotService;
import com.ruoyi.project.caseapp.track.service.ICompositeEventService;
import com.ruoyi.project.caseapp.track.service.IDailyReportService;
import com.ruoyi.project.caseapp.track.mapper.EventTrackRelationMapper;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.framework.web.page.TableSupport;

/**
 * 轨迹Controller
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
@Controller
@RequestMapping("/caseapp/track")
public class AppTrackController extends BaseController
{
    private String prefix = "track";

    @Value("${server.port}")
    private String serverPort;
    @Value("${hkpt.ip}")
    private String ip;
    @Value("${hkpt.port}")
    private String port;
    @Value("${hkpt.appKey}")
    private String appKey;
    @Value("${hkpt.appSecret}")
    private String appSecret;

    @Autowired
    private IAppTrackService appTrackService;

    @Autowired
    private ICompositeEventService compositeEventService;

    @Autowired
    private IDictTypeService dictTypeService;

    @Autowired
    private IAppRoomipService appRoomipService;

    @Autowired
    private EventTrackRelationMapper relationMapper;

    @Autowired
    private IDailyReportService dailyReportService;

    @Autowired
    private IAppTrackScreenshotService screenshotService;

    @Autowired
    private com.ruoyi.project.caseapp.location.mapper.AppLocationMapper locationMapper;

    @Autowired
    private com.ruoyi.project.caseapp.location.mapper.AppCameraLocationRelMapper cameraLocationRelMapper;

    //首页 - 复合事件页面
    @GetMapping()
    public String track()
    {
        return "sy";
    }

    //事件列表页面 - 事件标注
    @GetMapping("/eventList")
    public String eventList()
    {
        return "track/event-list";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(AppTrack appTrack)
    {
        startPage();
        List<AppTrack> list = appTrackService.selectAppTrackList(appTrack);
        return getDataTable(list);
    }

    /**
     * rtsp实时流，根据splx是否为1启用
     */
    @PostMapping("/rtspStream")
    @ResponseBody
    public AjaxResult rtspStream()
    {
        AppRoomip appRoomip = new AppRoomip();
        appRoomip.setSblx("1");
        List<AppRoomip> appRoomips = appRoomipService.selectAppRoomipList(appRoomip);
        for (AppRoomip roomip : appRoomips) {
            if(StringUtils.isEmpty(roomip.getRtspssl())){
                HikVedioUtil hikVedioUtil = new HikVedioUtil();
                hikVedioUtil.fz(ip,"443",appKey,appSecret);
                String url = "/api/video/v1/cameras/previewURLs";
                JSONObject jsonObject = new JSONObject();
                // 将日期转换为ISO 8601 格式进行传参
                jsonObject.put("cameraIndexCode",roomip.getIp());  // 摄像头的唯一标识，应该是从数据库里获取
                jsonObject.put("streamType",0);
                jsonObject.put("protocol","rtsp");    // 取流协议
                jsonObject.put("transmode",1);     // 传输协议 1是TCP，0是udp
                // 调用接口 （视频截取的话应该是返回一个rtsp的视频流）
                String result = hikVedioUtil.vedioCut(url, jsonObject);
                JSONObject jsonObject1 = JSON.parseObject(result);
                String rspturl = jsonObject1.getJSONObject("data").getString("url");
                roomip.setRtspssl(rspturl);
            }
        }
        return AjaxResult.success(appRoomips);
    }

    /**
     * 导出轨迹列表
     */
    @Log(title = "轨迹", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(AppTrack appTrack)
    {
        List<AppTrack> list = appTrackService.selectAppTrackList(appTrack);
        ExcelUtil<AppTrack> util = new ExcelUtil<AppTrack>(AppTrack.class);
        return util.exportExcel(list, "轨迹数据");
    }

    /**
     * 新增轨迹
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存轨迹
     */
    @Log(title = "轨迹", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(AppTrack appTrack, javax.servlet.http.HttpServletRequest request)
    {
        // 记录调用者信息（用于排查USB轨迹来源）
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        logger.info("========== 轨迹插入请求 ==========");
        logger.info("来源IP: {}", remoteAddr);
        logger.info("User-Agent: {}", userAgent);
        logger.info("Referer: {}", referer);
        logger.info("区域ID: {}, 区域名称: {}, 摄像头: {}",
            appTrack.getQyid(), appTrack.getQymc(), appTrack.getSxtmx());
        logger.info("拍摄时间: {}", appTrack.getPssj());
        logger.info("图片路径: {}", appTrack.getPstp());
        logger.info("====================================");

        // 临时方案：拦截USB轨迹（qyid = -1）
        if (appTrack.getQyid() != null && appTrack.getQyid() == -1)
        {
            logger.warn("!!! 拦截USB轨迹插入请求 !!!");
            logger.warn("调用者信息 - IP: {}, User-Agent: {}", remoteAddr, userAgent);
            return AjaxResult.error("USB轨迹插入已被系统禁用，请联系管理员");
        }

        return toAjax(appTrackService.insertAppTrack(appTrack));
    }

    /**
     * 修改轨迹
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        AppTrack appTrack = appTrackService.selectAppTrackById(id);
        mmap.put("appTrack", appTrack);
        return prefix + "/edit";
    }

    /**
     * 修改保存轨迹（用于标注）
     */
    @Log(title = "轨迹标注", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(AppTrack appTrack)
    {
        AppTrack event = appTrackService.selectAppTrackById(appTrack.getId());
        if (event == null) {
            return AjaxResult.error("事件不存在");
        }
        
        // 设置标注信息
        event.setBzzt("1"); // 已标注
        event.setXwyy(appTrack.getXwyy());
        // 如果 xwyy 包含 "："，说明是"其他"原因，已经包含了详情
        event.setRyxm(appTrack.getRyxm());
        event.setWlry(appTrack.getWlry());
        event.setBzsj(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        int result = appTrackService.updateAppTrack(event);
        return result > 0 ? AjaxResult.success("标注成功") : AjaxResult.error("标注失败");
    }

    /**
     * 删除轨迹
     */
    @Log(title = "轨迹", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(appTrackService.deleteAppTrackByIds(ids));
    }

    /**
     * 获取统计数据（复合事件统计）
     */
    @GetMapping("/stats")
    @ResponseBody
    public AjaxResult getStats()
    {
        int todayTotal = compositeEventService.countTodayEvents();
        int unlabeledCount = compositeEventService.countUnlabeledEvents();
        int labeledCount = compositeEventService.countLabeledEvents();
        int monthTotal = compositeEventService.countMonthEvents();

        return AjaxResult.success()
            .put("todayTotal", todayTotal)
            .put("unlabeledCount", unlabeledCount)
            .put("labeledCount", labeledCount)
            .put("monthTotal", monthTotal);
    }

    /**
     * 查询事件列表（用于首页展示，支持筛选和分页）
     */
    @PostMapping("/events")
    @ResponseBody
    public TableDataInfo getEvents(AppTrack appTrack)
    {
        startPage();
        List<AppTrack> list = appTrackService.selectAppTrackList(appTrack);
        return getDataTable(list);
    }

    /**
     * 获取事件详情
     */
    @GetMapping("/event/{id}")
    @ResponseBody
    public AjaxResult getEventDetail(@PathVariable("id") Long id)
    {
        AppTrack event = appTrackService.selectAppTrackById(id);
        if (event == null) {
            return AjaxResult.error("事件不存在");
        }
        return AjaxResult.success().put("data", event);
    }


    /**
     * 获取字典数据（用于状态筛选）
     */
    @GetMapping("/dict/{dictType}")
    @ResponseBody
    public AjaxResult getDictData(@PathVariable("dictType") String dictType)
    {
        List<com.ruoyi.project.system.dict.domain.DictData> list = dictTypeService.selectDictDataByType(dictType);
        return AjaxResult.success().put("data", list);
    }

    /**
     * 查询复合事件列表
     * qyid = 1 表示复合事件的标记点，两个标记点之间的所有数据组成一个复合事件
     * 支持时间筛选
     * 
     * @param appTrack 轨迹（用于时间筛选，通过 params.beginPssj 和 params.endPssj 进行时间筛选）
     * @return 复合事件列表
     */
    @PostMapping("/compositeEvents")
    @ResponseBody
    public TableDataInfo getCompositeEvents(AppTrack appTrack)
    {
        startPage();
        List<CompositeEvent> list = appTrackService.selectCompositeEvents(appTrack);
        return getDataTable(list);
    }

    /**
     * 查询复合事件列表（不分页，用于导出等场景）
     * 
     * @param appTrack 轨迹（用于时间筛选）
     * @return 复合事件列表
     */
    @PostMapping("/compositeEvents/list")
    @ResponseBody
    public AjaxResult getCompositeEventsList(AppTrack appTrack)
    {
        List<CompositeEvent> list = appTrackService.selectCompositeEvents(appTrack);

        // 为每个复合事件的轨迹添加截图信息
        for (CompositeEvent event : list) {
            if (event.getEvents() != null) {
                for (AppTrack track : event.getEvents()) {
                    // 查询该轨迹的所有截图
                    List<AppTrackScreenshot> screenshots = screenshotService.selectScreenshotsByTrackId(track.getId());
                    track.setScreenshots(screenshots);
                }
            }
        }

        return AjaxResult.success().put("data", list);
    }

    /**
     * 跳转到事件经过还原（复合事件）页面
     * 
     * @return 页面路径
     */
    @GetMapping("/compositeTrace")
    public String compositeTrace()
    {
        return prefix + "/composite-trace";
    }

    /**
     * 跳转到事件标注页面
     *
     * @return 页面路径
     */
    @GetMapping("/eventAnnotation")
    public String eventAnnotation()
    {
        return prefix + "/event-annotation";
    }

    /**
     * 跳转到视频测试页面（用于调试视频路径）
     *
     * @return 页面路径
     */
    @GetMapping("/videoTest")
    public String videoTest()
    {
        return prefix + "/video-test";
    }

    /**
     * 跳转到每日报告页面
     *
     * @return 页面路径
     */
    @GetMapping("/dailyReport")
    public String dailyReport()
    {
        return prefix + "/daily-report";
    }

    /**
     * 获取每日报告数据
     *
     * @param dateStr 报告日期（格式：yyyy-MM-dd，如果不传则为今天）
     * @return 每日报告数据
     */
    @PostMapping("/dailyReport/getData")
    @ResponseBody
    public AjaxResult getDailyReportData(String dateStr)
    {
        try
        {
            Date reportDate = null;
            if (dateStr != null && !dateStr.isEmpty())
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                reportDate = sdf.parse(dateStr);
            }

            DailyReport report = dailyReportService.generateDailyReport(reportDate);
            return AjaxResult.success().put("data", report);
        }
        catch (ParseException e)
        {
            return AjaxResult.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
        catch (Exception e)
        {
            return AjaxResult.error("生成每日报告失败：" + e.getMessage());
        }
    }

    /**
     * 同步所有复合事件（初始化/数据修复）
     * 根据现有轨迹数据重新计算并同步所有复合事件
     *
     * @param appTrack 查询条件（时间范围等，可选）
     * @return 同步结果
     */
    @Log(title = "复合事件同步", businessType = BusinessType.OTHER)
    @PostMapping("/syncCompositeEvents")
    @ResponseBody
    public AjaxResult syncCompositeEvents(AppTrack appTrack)
    {
        try
        {
            logger.info("开始同步复合事件，参数：{}", appTrack != null ? appTrack.getParams() : "null");

            int count = compositeEventService.syncAllCompositeEvents(appTrack);

            logger.info("同步复合事件完成，共处理 {} 个复合事件", count);
            return AjaxResult.success("同步成功，共处理 " + count + " 个复合事件");
        }
        catch (Exception e)
        {
            logger.error("同步复合事件失败", e);
            return AjaxResult.error("同步失败：" + e.getMessage());
        }
    }

    /**
     * 为指定轨迹生成/更新复合事件（Python系统调用）
     * 轻量级接口：只处理该轨迹及其周围的事件
     *
     * @param trackId 轨迹ID
     * @return 处理结果
     */
    @Log(title = "轨迹事件同步", businessType = BusinessType.OTHER)
    @PostMapping("/syncEventForTrack/{trackId}")
    @ResponseBody
    public AjaxResult syncEventForTrack(@PathVariable("trackId") Long trackId)
    {
        try
        {
            if (trackId == null)
            {
                return AjaxResult.error("轨迹ID不能为空");
            }

            // 查询轨迹
            AppTrack track = appTrackService.selectAppTrackById(trackId);
            if (track == null)
            {
                return AjaxResult.error("轨迹不存在：ID=" + trackId);
            }

            // 为该轨迹生成/更新复合事件
            compositeEventService.updateOrCreateCompositeEventByTrack(track);

            return AjaxResult.success("已为轨迹 ID=" + trackId + " 生成复合事件");
        }
        catch (Exception e)
        {
            logger.error("为轨迹生成复合事件失败：trackId=" + trackId, e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 标注复合事件
     * 标注复合事件时，会同时更新该事件下的所有轨迹
     *
     * @param eventId 复合事件的event_id（第一条轨迹的ID）
     * @param xwyy 行为原因
     * @param ryxm 人员姓名
     * @param wlry 外来人员
     * @param remark 备注
     * @return 标注结果
     */
    @Log(title = "复合事件标注", businessType = BusinessType.UPDATE)
    @PostMapping("/annotateCompositeEvent")
    @ResponseBody
    public AjaxResult annotateCompositeEvent(Long eventId, String xwyy, String ryxm, String wlry, String remark)
    {
        try
        {
            if (eventId == null)
            {
                return AjaxResult.error("事件ID不能为空");
            }

            if (xwyy == null || xwyy.trim().isEmpty())
            {
                return AjaxResult.error("行为原因不能为空");
            }

            // 调用服务层标注方法
            compositeEventService.annotateCompositeEvent(eventId, xwyy, ryxm, wlry, remark);

            return AjaxResult.success("标注成功");
        }
        catch (Exception e)
        {
            logger.error("标注复合事件失败", e);
            return AjaxResult.error("标注失败：" + e.getMessage());
        }
    }

    /**
     * 导出复合事件台账
     * 根据选中的事件ID列表导出Excel文件
     *
     * @param eventIds 事件ID列表（逗号分隔）
     * @return 导出结果
     */
    @Log(title = "复合事件台账导出", businessType = BusinessType.EXPORT)
    @PostMapping("/exportCompositeEvents")
    @ResponseBody
    public AjaxResult exportCompositeEvents(String eventIds)
    {
        try
        {
            if (eventIds == null || eventIds.trim().isEmpty())
            {
                return AjaxResult.error("请选择要导出的复合事件");
            }

            // 解析事件ID列表
            String[] eventIdArray = eventIds.split(",");
            List<Long> eventIdList = new ArrayList<>();
            for (String idStr : eventIdArray)
            {
                try
                {
                    eventIdList.add(Long.parseLong(idStr.trim()));
                }
                catch (NumberFormatException e)
                {
                    logger.warn("无效的事件ID: " + idStr);
                }
            }

            if (eventIdList.isEmpty())
            {
                return AjaxResult.error("没有有效的事件ID");
            }

            // 查询复合事件列表
            List<CompositeEvent> compositeEvents = new ArrayList<>();
            for (Long eventId : eventIdList)
            {
                // 注意：前端传递的是复合事件的主键ID，而不是eventId字段
                CompositeEvent event = compositeEventService.selectCompositeEventById(eventId);
                if (event != null)
                {
                    // 从关系表查询轨迹详情
                    List<AppTrack> tracks = new ArrayList<>();
                    List<Long> trackIds = relationMapper.selectTrackIdsByEventId(event.getId());

                    for (Long trackId : trackIds)
                    {
                        AppTrack track = appTrackService.selectAppTrackById(trackId);
                        if (track != null)
                        {
                            tracks.add(track);
                        }
                    }

                    event.setEvents(tracks);
                    compositeEvents.add(event);
                }
                else
                {
                    logger.warn("未找到eventId为 " + eventId + " 的复合事件");
                }
            }

            logger.info("准备导出 " + compositeEvents.size() + " 条复合事件数据");

            if (compositeEvents.isEmpty())
            {
                return AjaxResult.error("未查询到有效的复合事件数据，请检查是否已生成复合事件");
            }

            // 使用ExcelUtil导出
            ExcelUtil<CompositeEvent> util = new ExcelUtil<>(CompositeEvent.class);
            return util.exportExcel(compositeEvents, "复合事件台账");
        }
        catch (Exception e)
        {
            logger.error("导出复合事件台账失败", e);
            return AjaxResult.error("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出事件包（HTML报告 + 图片 + 视频 + JSON数据）
     * 导出为ZIP文件，通过浏览器下载
     */
    @Log(title = "导出事件包", businessType = BusinessType.EXPORT)
    @PostMapping("/compositeEvents/export/{eventId}")
    @ResponseBody
    public AjaxResult exportEventPackage(@PathVariable("eventId") Long eventId)
    {
        try
        {
            // 使用RuoYi下载目录
            String exportBasePath = com.ruoyi.framework.config.RuoYiConfig.getDownloadPath();
            File exportDir = new File(exportBasePath);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            // 导出事件包（返回ZIP文件名）
            String zipFileName = compositeEventService.exportEventPackage(eventId, exportBasePath);

            return AjaxResult.success(zipFileName);
        }
        catch (Exception e)
        {
            logger.error("导出事件包失败", e);
            return AjaxResult.error("导出失败：" + e.getMessage());
        }
    }

    /**
     * 批量导出事件包（支持多个事件ID，导出为ZIP文件）
     * 导出为ZIP文件，通过浏览器下载
     *
     * @param eventIds 事件ID列表（逗号分隔）
     * @return 导出结果
     */
    @Log(title = "批量导出事件包", businessType = BusinessType.EXPORT)
    @PostMapping("/compositeEvents/batchExport")
    @ResponseBody
    public AjaxResult batchExportEventPackages(String eventIds)
    {
        try
        {
            if (eventIds == null || eventIds.trim().isEmpty())
            {
                return AjaxResult.error("请选择要导出的复合事件");
            }

            // 使用RuoYi下载目录
            String exportBasePath = com.ruoyi.framework.config.RuoYiConfig.getDownloadPath();
            File exportDir = new File(exportBasePath);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            // 解析事件ID列表
            String[] idArray = eventIds.split(",");
            List<Long> eventIdList = new ArrayList<>();
            for (String idStr : idArray)
            {
                try
                {
                    eventIdList.add(Long.parseLong(idStr.trim()));
                }
                catch (NumberFormatException e)
                {
                    logger.warn("无效的事件ID: " + idStr);
                }
            }

            if (eventIdList.isEmpty())
            {
                return AjaxResult.error("没有有效的事件ID");
            }

            // 批量导出事件包（返回ZIP文件名）
            String zipFileName = compositeEventService.batchExportEventPackages(eventIdList, exportBasePath);

            return AjaxResult.success(zipFileName);
        }
        catch (Exception e)
        {
            logger.error("批量导出事件包失败", e);
            return AjaxResult.error("导出失败：" + e.getMessage());
        }
    }

    /**
     * 跳转到轨迹详情页面
     *
     * @param id 轨迹ID
     * @param mmap ModelMap
     * @return 页面路径
     */
    @GetMapping("/detail/{id}")
    public String trajectoryDetail(@PathVariable("id") Long id, ModelMap mmap)
    {
        AppTrack trajectory = appTrackService.selectAppTrackById(id);
        if (trajectory == null)
        {
            return "error/404";
        }
        mmap.put("trajectory", trajectory);
        return prefix + "/trajectory-detail";
    }

    /**
     * 获取轨迹详情数据（包括所有截图）
     *
     * @param id 轨迹ID
     * @return 轨迹详情（包括基本信息和截图列表）
     */
    @GetMapping("/detailData/{id}")
    @ResponseBody
    public AjaxResult getTrajectoryDetail(@PathVariable("id") Long id)
    {
        try
        {
            // 查询轨迹基本信息
            AppTrack trajectory = appTrackService.selectAppTrackById(id);
            if (trajectory == null)
            {
                return AjaxResult.error("轨迹不存在");
            }

            // 查询该轨迹的所有截图
            List<com.ruoyi.project.caseapp.track.domain.AppTrackScreenshot> screenshots =
                screenshotService.selectScreenshotsByTrackId(id);

            return AjaxResult.success()
                .put("trajectory", trajectory)
                .put("screenshots", screenshots);
        }
        catch (Exception e)
        {
            logger.error("获取轨迹详情失败", e);
            return AjaxResult.error("获取轨迹详情失败：" + e.getMessage());
        }
    }

    /**
     * 跳转到货架轨迹查询页面
     */
    @GetMapping("/shelfTrajectory")
    public String shelfTrajectoryPage()
    {
        return "track/shelf-trajectory";
    }

    /**
     * 根据货架位置和时间范围查询轨迹
     *
     * @param locationCode 货架位置编码（如 A01-03-02）
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 轨迹列表（包含截图）
     */
    @PostMapping("/queryShelfTrajectory")
    @ResponseBody
    public AjaxResult queryShelfTrajectory(String locationCode, String beginTime, String endTime)
    {
        try
        {
            logger.info("查询货架轨迹：货架位置={}, 开始时间={}, 结束时间={}", locationCode, beginTime, endTime);

            List<AppTrack> trajectories = new java.util.ArrayList<>();

            if (StringUtils.isNotEmpty(locationCode))
            {
                // 步骤1：根据货架位置编码查询位置ID
                com.ruoyi.project.caseapp.location.domain.AppLocation location = locationMapper.selectByLocationCode(locationCode);

                if (location != null)
                {
                    logger.info("找到货架位置：locationId={}, locationName={}", location.getLocationId(), location.getLocationName());

                    // 步骤2：根据位置ID查询关联的摄像头ID列表（即qyid）
                    List<Long> cameraIds = cameraLocationRelMapper.selectCameraIdsByLocationId(location.getLocationId());
                    logger.info("找到关联的摄像头ID列表：{}", cameraIds);

                    if (cameraIds != null && !cameraIds.isEmpty())
                    {
                        // 步骤3：根据摄像头ID（qyid）查询轨迹
                        for (Long cameraId : cameraIds)
                        {
                            AppTrack queryParam = new AppTrack();
                            queryParam.setQyid(cameraId);

                            // 设置时间范围
                            if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime))
                            {
                                queryParam.getParams().put("beginPssj", beginTime);
                                queryParam.getParams().put("endPssj", endTime);
                            }

                            // 查询该摄像头的轨迹
                            List<AppTrack> cameraTrajectories = appTrackService.selectAppTrackList(queryParam);
                            trajectories.addAll(cameraTrajectories);
                        }
                    }
                    else
                    {
                        logger.warn("该货架位置未关联任何摄像头");
                    }
                }
                else
                {
                    logger.warn("未找到货架位置：{}", locationCode);
                }
            }

            // 为每个轨迹查询截图
            for (AppTrack trajectory : trajectories)
            {
                List<AppTrackScreenshot> screenshots = screenshotService.selectScreenshotsByTrackId(trajectory.getId());
                trajectory.setScreenshots(screenshots);
            }

            logger.info("查询到 {} 条轨迹记录", trajectories.size());

            return AjaxResult.success()
                .put("data", trajectories)
                .put("total", trajectories.size());
        }
        catch (Exception e)
        {
            logger.error("查询货架轨迹失败", e);
            return AjaxResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取区域列表（用于前端下拉框）
     *
     * @return 区域列表
     */
    @GetMapping("/getAreaList")
    @ResponseBody
    public AjaxResult getAreaList()
    {
        try
        {
            // 查询所有区域
            List<AppRoomip> areas = appRoomipService.selectAppRoomipList(new AppRoomip());

            return AjaxResult.success().put("data", areas);
        }
        catch (Exception e)
        {
            logger.error("获取区域列表失败", e);
            return AjaxResult.error("获取区域列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取货架位置列表（用于前端下拉框）
     *
     * @param areaName 区域名称（可选，用于筛选）
     * @return 货架位置列表
     */
    @GetMapping("/getLocationList")
    @ResponseBody
    public AjaxResult getLocationList(String areaName)
    {
        try
        {
            com.ruoyi.project.caseapp.location.domain.AppLocation queryParam =
                new com.ruoyi.project.caseapp.location.domain.AppLocation();

            if (StringUtils.isNotEmpty(areaName))
            {
                queryParam.setAreaName(areaName);
            }

            List<com.ruoyi.project.caseapp.location.domain.AppLocation> locations =
                locationMapper.selectAppLocationList(queryParam);

            return AjaxResult.success().put("data", locations);
        }
        catch (Exception e)
        {
            logger.error("获取货架位置列表失败", e);
            return AjaxResult.error("获取货架位置列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有区域（去重）
     *
     * @return 区域名称列表
     */
    @GetMapping("/getAreaNames")
    @ResponseBody
    public AjaxResult getAreaNames()
    {
        try
        {
            List<com.ruoyi.project.caseapp.location.domain.AppLocation> locations =
                locationMapper.selectAppLocationList(new com.ruoyi.project.caseapp.location.domain.AppLocation());

            // 去重并提取区域名称
            List<String> areaNames = locations.stream()
                .map(com.ruoyi.project.caseapp.location.domain.AppLocation::getAreaName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());

            return AjaxResult.success().put("data", areaNames);
        }
        catch (Exception e)
        {
            logger.error("获取区域名称列表失败", e);
            return AjaxResult.error("获取区域名称列表失败：" + e.getMessage());
        }
    }

}
