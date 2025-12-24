package com.ruoyi.project.caseapp.person.controller;

import java.util.ArrayList;
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
import com.ruoyi.project.caseapp.person.domain.AppPerson;
import com.ruoyi.project.caseapp.person.service.IAppPersonService;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.web.page.TableDataInfo;

/**
 * 管理员信息Controller
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
@Controller
@RequestMapping("/caseapp/person")
public class AppPersonController extends BaseController
{
    private String prefix = "caseapp/person";

    @Autowired
    private IAppPersonService appPersonService;

    @GetMapping()
    public String person()
    {
        return prefix + "/person";
    }

    /**
     * 查询管理员信息列表
     */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(AppPerson appPerson)
    {
        startPage();
        List<AppPerson> list = appPersonService.selectAppPersonList(appPerson);
        return getDataTable(list);
    }

    /**
     * 导出管理员信息列表
     */
    @Log(title = "管理员信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(AppPerson appPerson)
    {
        List<AppPerson> list = appPersonService.selectAppPersonList(appPerson);
        ExcelUtil<AppPerson> util = new ExcelUtil<AppPerson>(AppPerson.class);
        return util.exportExcel(list, "管理员信息数据");
    }

    /**
     * 新增管理员信息
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存管理员信息
     */
    @Log(title = "管理员信息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(AppPerson appPerson)
    {
        return toAjax(appPersonService.insertAppPerson(appPerson));
    }

    /**
     * 修改管理员信息
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        AppPerson appPerson = appPersonService.selectAppPersonById(id);
        mmap.put("appPerson", appPerson);
        return prefix + "/edit";
    }

    /**
     * 修改保存管理员信息
     */
    @Log(title = "管理员信息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(AppPerson appPerson)
    {
        return toAjax(appPersonService.updateAppPerson(appPerson));
    }

    /**
     * 删除管理员信息
     */
    @Log(title = "管理员信息", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(appPersonService.deleteAppPersonByIds(ids));
    }

    /**
     * 获取人员列表（用于人员选择）
     */
    @PostMapping("/persons")
    @ResponseBody
    public AjaxResult getPersons()
    {
        AppPerson query = new AppPerson();
        // 只获取未删除的人员
        query.setIsdel("0");
        List<AppPerson> list = appPersonService.selectAppPersonList(query);
        // 提取人员姓名
        Set<String> personSet = new HashSet<String>();
        for (AppPerson item : list)
        {
            String empname = item.getEmpname();
            if (empname != null && !empname.isEmpty())
            {
                personSet.add(empname);
            }
        }
        List<String> persons = new ArrayList<String>(personSet);
        return AjaxResult.success().put("data", persons);
    }
}
