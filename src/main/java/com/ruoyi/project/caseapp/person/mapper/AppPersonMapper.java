package com.ruoyi.project.caseapp.person.mapper;

import java.util.List;
import com.ruoyi.project.caseapp.person.domain.AppPerson;

/**
 * 管理员信息Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
public interface AppPersonMapper 
{
    /**
     * 查询管理员信息
     * 
     * @param id 管理员信息主键
     * @return 管理员信息
     */
    public AppPerson selectAppPersonById(Long id);

    /**
     * 查询管理员信息列表
     * 
     * @param appPerson 管理员信息
     * @return 管理员信息集合
     */
    public List<AppPerson> selectAppPersonList(AppPerson appPerson);

    /**
     * 新增管理员信息
     * 
     * @param appPerson 管理员信息
     * @return 结果
     */
    public int insertAppPerson(AppPerson appPerson);

    /**
     * 修改管理员信息
     * 
     * @param appPerson 管理员信息
     * @return 结果
     */
    public int updateAppPerson(AppPerson appPerson);

    /**
     * 删除管理员信息
     * 
     * @param id 管理员信息主键
     * @return 结果
     */
    public int deleteAppPersonById(Long id);

    /**
     * 批量删除管理员信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAppPersonByIds(String[] ids);
}
