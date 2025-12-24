package com.ruoyi.project.caseapp.person.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project.caseapp.person.mapper.AppPersonMapper;
import com.ruoyi.project.caseapp.person.domain.AppPerson;
import com.ruoyi.project.caseapp.person.service.IAppPersonService;
import com.ruoyi.common.utils.text.Convert;

/**
 * 管理员信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
@Service
public class AppPersonServiceImpl implements IAppPersonService 
{
    @Autowired
    private AppPersonMapper appPersonMapper;

    /**
     * 查询管理员信息
     * 
     * @param id 管理员信息主键
     * @return 管理员信息
     */
    @Override
    public AppPerson selectAppPersonById(Long id)
    {
        return appPersonMapper.selectAppPersonById(id);
    }

    /**
     * 查询管理员信息列表
     * 
     * @param appPerson 管理员信息
     * @return 管理员信息
     */
    @Override
    public List<AppPerson> selectAppPersonList(AppPerson appPerson)
    {
        return appPersonMapper.selectAppPersonList(appPerson);
    }

    /**
     * 新增管理员信息
     * 
     * @param appPerson 管理员信息
     * @return 结果
     */
    @Override
    public int insertAppPerson(AppPerson appPerson)
    {
        return appPersonMapper.insertAppPerson(appPerson);
    }

    /**
     * 修改管理员信息
     * 
     * @param appPerson 管理员信息
     * @return 结果
     */
    @Override
    public int updateAppPerson(AppPerson appPerson)
    {
        return appPersonMapper.updateAppPerson(appPerson);
    }

    /**
     * 批量删除管理员信息
     * 
     * @param ids 需要删除的管理员信息主键
     * @return 结果
     */
    @Override
    public int deleteAppPersonByIds(String ids)
    {
        return appPersonMapper.deleteAppPersonByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除管理员信息信息
     * 
     * @param id 管理员信息主键
     * @return 结果
     */
    @Override
    public int deleteAppPersonById(Long id)
    {
        return appPersonMapper.deleteAppPersonById(id);
    }
}
