package com.ruoyi.project.caseapp.person.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 管理员信息对象 app_person
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
public class AppPerson extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 人员工号 */
    @Excel(name = "人员工号")
    private String empno;

    /** 人员姓名 */
    @Excel(name = "人员姓名")
    private String empname;

    /** 人员性别 */
    @Excel(name = "人员性别")
    private String empsex;

    /** 组织机构代码 */
    @Excel(name = "组织机构代码")
    private String empDept;

    /** 是否删除 */
    @Excel(name = "是否删除")
    private String isdel;

    /** 人员身份证号 */
    @Excel(name = "人员身份证号")
    private String idno;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setEmpno(String empno)
    {
        this.empno = empno;
    }

    public String getEmpno()
    {
        return empno;
    }
    public void setEmpname(String empname)
    {
        this.empname = empname;
    }

    public String getEmpname()
    {
        return empname;
    }
    public void setEmpsex(String empsex)
    {
        this.empsex = empsex;
    }

    public String getEmpsex()
    {
        return empsex;
    }
    public void setEmpDept(String empDept)
    {
        this.empDept = empDept;
    }

    public String getEmpDept()
    {
        return empDept;
    }
    public void setIsdel(String isdel)
    {
        this.isdel = isdel;
    }

    public String getIsdel()
    {
        return isdel;
    }
    public void setIdno(String idno)
    {
        this.idno = idno;
    }

    public String getIdno()
    {
        return idno;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPhone()
    {
        return phone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("empno", getEmpno())
            .append("empname", getEmpname())
            .append("empsex", getEmpsex())
            .append("empDept", getEmpDept())
            .append("isdel", getIsdel())
            .append("idno", getIdno())
            .append("phone", getPhone())
            .toString();
    }
}
