package com.ruoyi.project.caseapp.roomip.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project.caseapp.roomip.mapper.AppRoomipMapper;
import com.ruoyi.project.caseapp.roomip.domain.AppRoomip;
import com.ruoyi.project.caseapp.roomip.service.IAppRoomipService;
import com.ruoyi.common.utils.text.Convert;

/**
 * 库区与摄像头ipService业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
@Service
public class AppRoomipServiceImpl implements IAppRoomipService 
{
    @Autowired
    private AppRoomipMapper appRoomipMapper;

    /**
     * 查询库区与摄像头ip
     * 
     * @param id 库区与摄像头ip主键
     * @return 库区与摄像头ip
     */
    @Override
    public AppRoomip selectAppRoomipById(Long id)
    {
        return appRoomipMapper.selectAppRoomipById(id);
    }

    /**
     * 查询库区与摄像头ip列表
     * 
     * @param appRoomip 库区与摄像头ip
     * @return 库区与摄像头ip
     */
    @Override
    public List<AppRoomip> selectAppRoomipList(AppRoomip appRoomip)
    {
        return appRoomipMapper.selectAppRoomipList(appRoomip);
    }

    /**
     * 新增库区与摄像头ip
     * 
     * @param appRoomip 库区与摄像头ip
     * @return 结果
     */
    @Override
    public int insertAppRoomip(AppRoomip appRoomip)
    {
        return appRoomipMapper.insertAppRoomip(appRoomip);
    }

    /**
     * 修改库区与摄像头ip
     * 
     * @param appRoomip 库区与摄像头ip
     * @return 结果
     */
    @Override
    public int updateAppRoomip(AppRoomip appRoomip)
    {
        return appRoomipMapper.updateAppRoomip(appRoomip);
    }

    /**
     * 批量删除库区与摄像头ip
     * 
     * @param ids 需要删除的库区与摄像头ip主键
     * @return 结果
     */
    @Override
    public int deleteAppRoomipByIds(String ids)
    {
        return appRoomipMapper.deleteAppRoomipByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除库区与摄像头ip信息
     * 
     * @param id 库区与摄像头ip主键
     * @return 结果
     */
    @Override
    public int deleteAppRoomipById(Long id)
    {
        return appRoomipMapper.deleteAppRoomipById(id);
    }
}
