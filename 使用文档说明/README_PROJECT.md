# 资产视频监控系统

基于Spring Boot + MyBatis + Thymeleaf开发的资产视频监控管理系统

**版本**: 4.7.8
**技术栈**: Spring Boot 2.5.15 + MyBatis + Thymeleaf + Shiro + MySQL + Redis

---

## 📚 文档导航

### 🚀 [部署手册 (DEPLOYMENT.md)](./DEPLOYMENT.md)

**适用人员**: 运维人员、系统管理员

**内容包括**:
- ✅ 环境要求和准备工作
- ✅ 数据库部署步骤
- ✅ 应用配置详解
- ✅ 多种部署方式（JAR包、systemd服务）
- ✅ SSL证书配置
- ✅ 启动验证和常见问题
- ✅ 性能优化和监控建议

**快速开始**: [点击查看部署手册](./DEPLOYMENT.md)

---

### 🔧 [维护手册 (MAINTENANCE.md)](./MAINTENANCE.md)

**适用人员**: 开发人员、维护人员

**内容包括**:
- ✅ 系统架构和代码结构
- ✅ 核心功能模块详解
- ✅ 各功能代码位置和修改指南
- ✅ 数据库设计说明
- ✅ 常见修改场景（含代码示例）
- ✅ 开发规范和调试技巧

**快速查找**:
- [事件聚合算法修改](./MAINTENANCE.md#一复合事件聚合算法修改)
- [事件列表页面定制](./MAINTENANCE.md#二事件列表页面定制)
- [事件包导出功能修改](./MAINTENANCE.md#三事件包导出功能修改)
- [数据库表结构](./MAINTENANCE.md#数据库设计)

---

## 核心功能

### 1. 复合事件自动聚合 ⭐
- 基于30秒空闲检测算法自动将连续轨迹聚合为事件
- 支持实时聚合和批量同步
- 自动计算事件持续时长、行为路径、最大人数等

### 2. 事件包批量导出 📦
- 支持多选事件批量导出
- 导出内容包括：HTML报告、图片、视频、JSON数据、PDF说明文档
- 导出位置：桌面/事件包导出/

### 3. 事件标注管理 🏷️
- 支持对复合事件进行标注（行为原因、人员姓名等）
- 标注信息自动同步到所有关联轨迹
- 标注状态统计（待标注/已标注）

### 4. 实时统计看板 📊
- 今日事件数、本月事件数
- 待标注/已标注事件数
- 异常事件提示（非工作时间、人数异常）

### 5. 视频轨迹查看 🎥
- 时间线方式展示活动轨迹
- 支持图片和视频查看
- 区域筛选和时间范围查询

---

## 系统访问

### 默认访问地址
- **HTTPS**: https://localhost:8090
- **HTTP**: http://localhost:8090（如禁用SSL）

### 默认账号
```
账号: admin
密码: admin123
```

### 主要功能页面
- **复合事件列表**: https://localhost:8090/caseapp/track
- **活动轨迹列表**: https://localhost:8090/caseapp/track/eventList
- **系统监控**: https://localhost:8090/druid/

---

## 快速开始

### 1. 部署系统

如果你是首次部署，请查看 **[部署手册](./DEPLOYMENT.md)**

```bash
# 1. 安装环境（JDK 1.8, MySQL 8.0, Redis 5.0）
# 2. 导入数据库
mysql -u root -p caseappdb < sql/caseappdb.sql

# 3. 修改配置
# 编辑 src/main/resources/application-druid.yml（数据库）
# 编辑 src/main/resources/application.yml（Redis、文件路径）

# 4. 编译打包
mvn clean package -DskipTests

# 5. 启动应用
java -jar target/ruoyi.jar

# 6. 访问系统
# 浏览器打开: https://localhost:8090
```

### 2. 开发修改

如果你需要修改功能，请查看 **[维护手册](./MAINTENANCE.md)**

**常见修改场景**:
- [修改事件聚合时间阈值](./MAINTENANCE.md#一复合事件聚合算法修改)
- [添加新的统计维度](./MAINTENANCE.md#五统计功能修改)
- [修改PDF导出格式](./MAINTENANCE.md#32-修改pdf格式)
- [添加新的标注字段](./MAINTENANCE.md#41-添加标注字段)

---

## 项目结构

```
caseapp/
├── src/main/
│   ├── java/com/ruoyi/project/caseapp/
│   │   └── track/                              # 活动轨迹和复合事件模块（核心）
│   │       ├── controller/                    # HTTP接口层
│   │       ├── service/                       # 业务逻辑层
│   │       ├── mapper/                        # 数据访问层
│   │       └── domain/                        # 实体类
│   │
│   └── resources/
│       ├── application.yml                    # 主配置文件
│       ├── application-druid.yml              # 数据库配置
│       ├── mybatis/                           # MyBatis SQL映射
│       └── templates/
│           ├── sy.html                        # 复合事件列表页（核心）
│           └── caseapp/track/                     # 其他页面
│
├── pom.xml                                    # Maven依赖
├── README.md                                  # RuoYi框架原始说明
├── README_PROJECT.md                          # 项目说明文档（本文件）⭐
├── DEPLOYMENT.md                              # 部署手册 ⭐⭐⭐
└── MAINTENANCE.md                             # 维护手册 ⭐⭐⭐
```

---

## 技术亮点

### 1. 智能事件聚合算法
采用时间间隔检测算法，自动将30秒内的连续轨迹聚合为一个事件，大幅减少数据冗余。

### 2. 实时 + 批量双写机制
- **实时写入**: 轨迹新增/修改时自动更新复合事件
- **批量同步**: 支持历史数据批量同步，可用于数据修复

### 3. 事件包离线导出
支持将事件完整信息（HTML、图片、视频、数据）打包导出，方便离线查看和归档。

### 4. PDF智能生成
使用Apache PDFBox自动生成结构化PDF说明文档，支持中文字体，包含事件详情和轨迹明细。

---

## 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.5.15 | 核心框架 |
| MyBatis | 官方版本 | ORM框架 |
| Druid | 1.2.20 | 数据库连接池 |
| Shiro | 1.13.0 | 权限管理 |
| Thymeleaf | 官方版本 | 模板引擎 |
| Fastjson | 1.2.83 | JSON处理 |
| Apache PDFBox | 2.0.29 | PDF生成 |
| Redis | 5.0+ | 缓存 |

---

## 常见问题

### Q: 如何修改事件聚合的时间间隔？
A: 查看 [维护手册 - 事件聚合算法修改](./MAINTENANCE.md#一复合事件聚合算法修改)

### Q: 如何添加新的统计指标？
A: 查看 [维护手册 - 统计功能修改](./MAINTENANCE.md#五统计功能修改)

### Q: 导出的事件包在哪里？
A: 默认位置: `桌面/事件包导出/batch_events_[时间]/`

### Q: 如何修改导出PDF的格式？
A: 查看 [维护手册 - PDF格式修改](./MAINTENANCE.md#32-修改pdf格式)

### Q: 系统无法启动怎么办？
A: 查看 [部署手册 - 常见问题](./DEPLOYMENT.md#常见问题)

---

## 系统要求

- **JDK**: 1.8（必须）
- **MySQL**: 5.7+ 或 8.0+
- **Redis**: 5.0+
- **内存**: 8GB+ （推荐16GB）
- **磁盘**: 500GB+ （用于存储视频）
- **操作系统**: Windows Server 2016+ 或 CentOS 7+

---

## 开发团队

**项目名称**: 资产视频监控系统
**版本**: 4.7.8
**基于**: RuoYi框架 v4.7.8
**最后更新**: 2025-12-27

---

## 许可证

本项目基于RuoYi框架开发，遵循相关开源协议。

---

## 获取帮助

- 📖 查看 [部署手册](./DEPLOYMENT.md)
- 🔧 查看 [维护手册](./MAINTENANCE.md)
- 💬 联系技术支持团队

---

**祝你使用愉快！** 🎉
