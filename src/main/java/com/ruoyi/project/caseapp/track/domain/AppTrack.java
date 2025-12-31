package com.ruoyi.project.caseapp.track.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 轨迹对象 app_track
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
public class AppTrack extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 拍摄时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "拍摄时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date pssj;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date jssj;

    /** 拍摄图片 */
    @Excel(name = "拍摄图片")
    private String pstp;

    /** 区域id */
    @Excel(name = "区域id")
    private Long qyid;

    /** 区域名称（摄像头名称） */
    @Excel(name = "区域名称")
    private String qymc;

    /** 摄像头名称 */
    private String sxtmx;

    /** 视频地址 */
    @Excel(name = "视频地址")
    private String spdz;

    /** 视频时长 */
    @Excel(name = "视频时长")
    private String spsc;

    /** 截取状态 */
    @Excel(name = "截取状态")
    private String jqzt;

    /** 标注状态（biz_annotation_status） */
    @Excel(name = "标注状态", readConverterExp = "biz_annotation_status")
    private String bzzt;

    /** 标注时间 */
    @Excel(name = "标注时间")
    private String bzsj;

    /** 行为原因 */
    @Excel(name = "行为原因")
    private String xwyy;

    /** 画面人员姓名（管理员） */
    @Excel(name = "画面人员姓名", readConverterExp = "库=管员")
    private String ryxm;

    /** 画面人员姓名（外来人员） */
    @Excel(name = "画面人员姓名", readConverterExp = "外=来人员")
    private String wlry;

    /** 人员数量 */
    private int rysl;

    /** 检测次数 */
    @Excel(name = "检测次数")
    private Integer jscs;

    /** 截图列表（不保存到数据库，仅用于传输） */
    private List<AppTrackScreenshot> screenshots;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setPssj(Date pssj)
    {
        this.pssj = pssj;
    }

    public Date getPssj()
    {
        return pssj;
    }
    public void setPstp(String pstp)
    {
        this.pstp = pstp;
    }

    public String getPstp()
    {
        return pstp;
    }
    public void setQyid(Long qyid)
    {
        this.qyid = qyid;
    }

    public Long getQyid()
    {
        return qyid;
    }
    public void setQymc(String qymc)
    {
        this.qymc = qymc;
    }

    public String getQymc()
    {
        return qymc;
    }
    public void setSpdz(String spdz)
    {
        this.spdz = spdz;
    }

    public String getSpdz()
    {
        return spdz;
    }
    public void setSpsc(String spsc)
    {
        this.spsc = spsc;
    }

    public String getSpsc()
    {
        return spsc;
    }
    public void setJqzt(String jqzt)
    {
        this.jqzt = jqzt;
    }

    public String getJqzt()
    {
        return jqzt;
    }
    public void setBzzt(String bzzt)
    {
        this.bzzt = bzzt;
    }

    public String getBzzt()
    {
        return bzzt;
    }
    public void setBzsj(String bzsj)
    {
        this.bzsj = bzsj;
    }

    public String getBzsj()
    {
        return bzsj;
    }
    public void setXwyy(String xwyy)
    {
        this.xwyy = xwyy;
    }

    public String getXwyy()
    {
        return xwyy;
    }
    public void setRyxm(String ryxm)
    {
        this.ryxm = ryxm;
    }

    public String getRyxm()
    {
        return ryxm;
    }
    public void setWlry(String wlry)
    {
        this.wlry = wlry;
    }

    public String getWlry()
    {
        return wlry;
    }
    public void setRysl(int rysl)
    {
        this.rysl = rysl;
    }

    public int getRysl()
    {
        return rysl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("pssj", getPssj())
            .append("pstp", getPstp())
            .append("qyid", getQyid())
            .append("qymc", getQymc())
            .append("spdz", getSpdz())
            .append("spsc", getSpsc())
            .append("jqzt", getJqzt())
            .append("bzzt", getBzzt())
            .append("bzsj", getBzsj())
            .append("xwyy", getXwyy())
            .append("ryxm", getRyxm())
            .append("wlry", getWlry())
            .append("rysl", getRysl())
            .toString();
    }

    public String getSxtmx() {
        return sxtmx;
    }

    public void setSxtmx(String sxtmx) {
        this.sxtmx = sxtmx;
    }

    public Date getJssj() {
        return jssj;
    }

    public void setJssj(Date jssj) {
        this.jssj = jssj;
    }

    public Integer getJscs() {
        return jscs;
    }

    public void setJscs(Integer jscs) {
        this.jscs = jscs;
    }

    public List<AppTrackScreenshot> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<AppTrackScreenshot> screenshots) {
        this.screenshots = screenshots;
    }
}
