# Case-Property Video Surveillance System

**🌐 [English](README_PROJECT.md) | [中文](README_PROJECT.zh.md)**

A case-property video surveillance and management system built on Spring Boot + MyBatis + Thymeleaf.

**Version**: 4.7.8
**Stack**: Spring Boot 2.5.15 + MyBatis + Thymeleaf + Shiro + MySQL + Redis

---

## 📚 Documentation

### 🚀 [Deployment Guide (DEPLOYMENT.md)](./DEPLOYMENT.md)

**Audience**: Ops engineers, system administrators

**Covers**:
- ✅ Environment requirements and prerequisites
- ✅ Database deployment steps
- ✅ Detailed application configuration
- ✅ Multiple deployment methods (JAR, systemd service)
- ✅ SSL certificate setup
- ✅ Startup verification and common issues
- ✅ Performance tuning and monitoring recommendations

**Quick start**: [see the deployment guide](./DEPLOYMENT.md)

---

### 🔧 [Maintenance Guide (MAINTENANCE.md)](./MAINTENANCE.md)

**Audience**: Developers, maintainers

**Covers**:
- ✅ System architecture and code structure
- ✅ Detailed walkthrough of core feature modules
- ✅ Code locations and how-to guides for each feature
- ✅ Database design notes
- ✅ Common modification scenarios (with code examples)
- ✅ Development conventions and debugging tips

**Quick links**:
- [Event-aggregation algorithm changes](./MAINTENANCE.md#一复合事件聚合算法修改)
- [Customizing the event-list page](./MAINTENANCE.md#二事件列表页面定制)
- [Modifying the event-package export feature](./MAINTENANCE.md#三事件包导出功能修改)
- [Database table structure](./MAINTENANCE.md#数据库设计)

---

## Core features

### 1. Automatic composite-event aggregation ⭐
- Automatically aggregates continuous tracks into events based on a 30-second idle-detection algorithm
- Supports both real-time aggregation and batch sync
- Automatically computes event duration, activity path, peak headcount, etc.

### 2. Batch event-package export 📦
- Supports multi-select batch export of events
- Export contents: HTML report, images, video, JSON data, PDF summary document
- Export location: Desktop/Event Package Export/

### 3. Event annotation management 🏷️
- Supports annotating composite events (reason for behavior, person's name, etc.)
- Annotation data automatically syncs to all associated tracks
- Annotation-status statistics (pending / annotated)

### 4. Real-time statistics dashboard 📊
- Today's / this month's event counts
- Pending / annotated event counts
- Anomaly alerts (off-hours activity, abnormal headcount)

### 5. Video track viewer 🎥
- Timeline view of activity tracks
- Supports viewing both images and video
- Filter by zone and time range

---

## System access

### Default access URL
- **HTTPS**: https://localhost:8090
- **HTTP**: http://localhost:8090 (if SSL is disabled)

### Default account
```
username: admin
password: admin123
```

### Main pages
- **Composite event list**: https://localhost:8090/caseapp/track
- **Activity track list**: https://localhost:8090/caseapp/track/eventList
- **System monitoring**: https://localhost:8090/druid/

---

## Quick start

### 1. Deploy the system

If this is your first deployment, see the **[Deployment Guide](./DEPLOYMENT.md)**

```bash
# 1. Set up the environment (JDK 1.8, MySQL 8.0, Redis 5.0)
# 2. Import the database
mysql -u root -p caseappdb < sql/caseappdb.sql

# 3. Edit configuration
# Edit src/main/resources/application-druid.yml (database)
# Edit src/main/resources/application.yml (Redis, file paths)

# 4. Build the package
mvn clean package -DskipTests

# 5. Start the application
java -jar target/ruoyi.jar

# 6. Access the system
# Open in a browser: https://localhost:8090
```

### 2. Development / customization

If you need to modify functionality, see the **[Maintenance Guide](./MAINTENANCE.md)**

**Common modification scenarios**:
- [Change the event-aggregation time threshold](./MAINTENANCE.md#一复合事件聚合算法修改)
- [Add a new statistics dimension](./MAINTENANCE.md#五统计功能修改)
- [Change the PDF export format](./MAINTENANCE.md#32-修改pdf格式)
- [Add a new annotation field](./MAINTENANCE.md#41-添加标注字段)

---

## Project structure

```
caseapp/
├── src/main/
│   ├── java/com/ruoyi/project/caseapp/
│   │   └── track/                              # activity-track & composite-event module (core)
│   │       ├── controller/                    # HTTP layer
│   │       ├── service/                       # business logic layer
│   │       ├── mapper/                        # data access layer
│   │       └── domain/                        # entity classes
│   │
│   └── resources/
│       ├── application.yml                    # main config file
│       ├── application-druid.yml              # database config
│       ├── mybatis/                           # MyBatis SQL mappings
│       └── templates/
│           ├── sy.html                        # composite-event list page (core)
│           └── caseapp/track/                     # other pages
│
├── pom.xml                                    # Maven dependencies
├── README.md                                  # original RuoYi framework readme
├── README_PROJECT.md                          # project readme (this file) ⭐
├── DEPLOYMENT.md                              # deployment guide ⭐⭐⭐
└── MAINTENANCE.md                             # maintenance guide ⭐⭐⭐
```

---

## Technical highlights

### 1. Smart event-aggregation algorithm
Uses a time-gap detection algorithm to automatically aggregate continuous tracks within a 30-second window into a single event, greatly reducing data redundancy.

### 2. Real-time + batch dual-write mechanism
- **Real-time write**: composite events are updated automatically as tracks are added/modified
- **Batch sync**: supports batch syncing of historical data, useful for data repair

### 3. Offline event-package export
Supports packaging a full event (HTML, images, video, data) for offline viewing and archiving.

### 4. Smart PDF generation
Uses Apache PDFBox to automatically generate structured PDF documents, with Chinese font support, including event details and track breakdowns.

---

## Core dependencies

| Dependency | Version | Purpose |
|------|------|------|
| Spring Boot | 2.5.15 | core framework |
| MyBatis | official release | ORM framework |
| Druid | 1.2.20 | DB connection pool |
| Shiro | 1.13.0 | authorization/permissions |
| Thymeleaf | official release | template engine |
| Fastjson | 1.2.83 | JSON processing |
| Apache PDFBox | 2.0.29 | PDF generation |
| Redis | 5.0+ | cache |

---

## FAQ

**Q: How do I change the event-aggregation time interval?**
A: See [Maintenance Guide – event-aggregation algorithm changes](./MAINTENANCE.md#一复合事件聚合算法修改)

**Q: How do I add a new statistics metric?**
A: See [Maintenance Guide – statistics feature changes](./MAINTENANCE.md#五统计功能修改)

**Q: Where do exported event packages go?**
A: Default location: `Desktop/Event Package Export/batch_events_[timestamp]/`

**Q: How do I change the exported PDF format?**
A: See [Maintenance Guide – PDF format changes](./MAINTENANCE.md#32-修改pdf格式)

**Q: What if the system won't start?**
A: See [Deployment Guide – FAQ](./DEPLOYMENT.md#常见问题)

---

## System requirements

- **JDK**: 1.8 (required)
- **MySQL**: 5.7+ or 8.0+
- **Redis**: 5.0+
- **Memory**: 8GB+ (16GB recommended)
- **Disk**: 500GB+ (for video storage)
- **OS**: Windows Server 2016+ or CentOS 7+

---

## Development team

**Project name**: Case-Property Video Surveillance System
**Version**: 4.7.8
**Based on**: RuoYi framework v4.7.8
**Last updated**: 2025-12-27

---

## License

This project is built on the RuoYi framework and follows its open-source license.

---

## Getting help

- 📖 See the [Deployment Guide](./DEPLOYMENT.md)
- 🔧 See the [Maintenance Guide](./MAINTENANCE.md)
- 💬 Contact the technical support team

---

**Enjoy!** 🎉
