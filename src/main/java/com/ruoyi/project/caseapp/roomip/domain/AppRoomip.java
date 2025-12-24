package com.ruoyi.project.caseapp.roomip.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 库区与摄像头ip对象 app_roomip
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
public class AppRoomip extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 摄像头名称 */
    @Excel(name = "摄像头名称")
    private String fjmc;

    /** 摄像头ip */
    @Excel(name = "摄像头ip")
    private String ip;

    /** 端口 */
    @Excel(name = "端口")
    private String dk;

    /** 通道号 */
    @Excel(name = "通道号")
    private String tdh;

    /** 序号 */
    @Excel(name = "序号")
    private String xh;

    /** 账号 */
    @Excel(name = "账号")
    private String zh;

    /** 密码 */
    @Excel(name = "密码")
    private String mm;

    /** 设备类型 */
    @Excel(name = "设备类型")
    private String sblx;

    /** 区域类型 */
    @Excel(name = "区域类型")
    private String gnslx;

    /** rtsp流 */
    @Excel(name = "rtsp流")
    private String rtspssl;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setFjmc(String fjmc)
    {
        this.fjmc = fjmc;
    }

    public String getFjmc()
    {
        return fjmc;
    }
    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getIp()
    {
        return ip;
    }
    public void setDk(String dk)
    {
        this.dk = dk;
    }

    public String getDk()
    {
        return dk;
    }
    public void setTdh(String tdh)
    {
        this.tdh = tdh;
    }

    public String getTdh()
    {
        return tdh;
    }
    public void setXh(String xh)
    {
        this.xh = xh;
    }

    public String getXh()
    {
        return xh;
    }
    public void setZh(String zh)
    {
        this.zh = zh;
    }

    public String getZh()
    {
        return zh;
    }
    public void setMm(String mm)
    {
        this.mm = mm;
    }

    public String getMm()
    {
        return mm;
    }
    public void setSblx(String sblx)
    {
        this.sblx = sblx;
    }

    public String getSblx()
    {
        return sblx;
    }
    public void setGnslx(String gnslx)
    {
        this.gnslx = gnslx;
    }

    public String getGnslx()
    {
        return gnslx;
    }
    public void setRtspssl(String rtspssl)
    {
        this.rtspssl = rtspssl;
    }

    public String getRtspssl()
    {
        return rtspssl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("fjmc", getFjmc())
            .append("ip", getIp())
            .append("dk", getDk())
            .append("tdh", getTdh())
            .append("xh", getXh())
            .append("zh", getZh())
            .append("mm", getMm())
            .append("sblx", getSblx())
            .append("gnslx", getGnslx())
            .append("rtspssl", getRtspssl())
            .toString();
    }
}
