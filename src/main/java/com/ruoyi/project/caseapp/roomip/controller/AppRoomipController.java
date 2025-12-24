package com.ruoyi.project.caseapp.roomip.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.project.caseapp.roomip.domain.AppRoomip;
import com.ruoyi.project.caseapp.roomip.service.IAppRoomipService;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.web.page.TableDataInfo;

/**
 * 库区与摄像头ipController
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
@Controller
@RequestMapping("/caseapp/roomip")
public class AppRoomipController extends BaseController
{
    private String prefix = "caseapp/roomip";

    @Autowired
    private IAppRoomipService appRoomipService;

    @GetMapping()
    public String roomip()
    {
        return prefix + "/roomip";
    }

    /**
     * 查询库区与摄像头ip列表
     */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(AppRoomip appRoomip)
    {
        startPage();
        List<AppRoomip> list = appRoomipService.selectAppRoomipList(appRoomip);
        return getDataTable(list);
    }

    /**
     * 导出库区与摄像头ip列表
     */
    @Log(title = "库区与摄像头ip", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(AppRoomip appRoomip)
    {
        List<AppRoomip> list = appRoomipService.selectAppRoomipList(appRoomip);
        ExcelUtil<AppRoomip> util = new ExcelUtil<AppRoomip>(AppRoomip.class);
        return util.exportExcel(list, "库区与摄像头ip数据");
    }

    /**
     * 新增库区与摄像头ip
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存库区与摄像头ip
     */
    @Log(title = "库区与摄像头ip", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(AppRoomip appRoomip)
    {
        return toAjax(appRoomipService.insertAppRoomip(appRoomip));
    }

    /**
     * 修改库区与摄像头ip
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        AppRoomip appRoomip = appRoomipService.selectAppRoomipById(id);
        mmap.put("appRoomip", appRoomip);
        return prefix + "/edit";
    }

    /**
     * 修改保存库区与摄像头ip
     */
    @Log(title = "库区与摄像头ip", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(AppRoomip appRoomip)
    {
        return toAjax(appRoomipService.updateAppRoomip(appRoomip));
    }

    /**
     * 删除库区与摄像头ip
     */
    @Log(title = "库区与摄像头ip", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(appRoomipService.deleteAppRoomipByIds(ids));
    }

    /**
     * 获取区域列表（用于下拉框，按gnslx分组）
     */
    @PostMapping("/areas")
    @ResponseBody
    public AjaxResult getAreas()
    {
        List<AppRoomip> list = appRoomipService.selectAppRoomipList(new AppRoomip());
        // 提取不重复的区域类型（gnslx）
        Set<String> areaSet = new HashSet<String>();
        for (AppRoomip item : list)
        {
            String gnslx = item.getGnslx();
            if (gnslx != null && !gnslx.isEmpty())
            {
                areaSet.add(gnslx);
            }
        }
        List<String> areas = new ArrayList<String>(areaSet);
        Collections.sort(areas);
        return AjaxResult.success().put("data", areas);
    }
}
