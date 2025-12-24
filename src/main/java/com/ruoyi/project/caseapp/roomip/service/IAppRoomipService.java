package com.ruoyi.project.caseapp.roomip.service;

import java.util.List;
import com.ruoyi.project.caseapp.roomip.domain.AppRoomip;

/**
 * 库区与摄像头ipService接口
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
public interface IAppRoomipService 
{
    /**
     * 查询库区与摄像头ip
     * 
     * @param id 库区与摄像头ip主键
     * @return 库区与摄像头ip
     */
    public AppRoomip selectAppRoomipById(Long id);

    /**
     * 查询库区与摄像头ip列表
     * 
     * @param appRoomip 库区与摄像头ip
     * @return 库区与摄像头ip集合
     */
    public List<AppRoomip> selectAppRoomipList(AppRoomip appRoomip);

    /**
     * 新增库区与摄像头ip
     * 
     * @param appRoomip 库区与摄像头ip
     * @return 结果
     */
    public int insertAppRoomip(AppRoomip appRoomip);

    /**
     * 修改库区与摄像头ip
     * 
     * @param appRoomip 库区与摄像头ip
     * @return 结果
     */
    public int updateAppRoomip(AppRoomip appRoomip);

    /**
     * 批量删除库区与摄像头ip
     * 
     * @param ids 需要删除的库区与摄像头ip主键集合
     * @return 结果
     */
    public int deleteAppRoomipByIds(String ids);

    /**
     * 删除库区与摄像头ip信息
     * 
     * @param id 库区与摄像头ip主键
     * @return 结果
     */
    public int deleteAppRoomipById(Long id);
}
