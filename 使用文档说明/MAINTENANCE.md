# 资产视频监控系统 - 维护手册

**版本**: 4.7.8
**最后更新**: 2025-12-27
**技术栈**: Spring Boot 2.5.15 + MyBatis + Thymeleaf + Shiro

---

## 📋 目录

1. [系统架构](#系统架构)
2. [核心功能模块](#核心功能模块)
3. [代码结构说明](#代码结构说明)
4. [功能修改指南](#功能修改指南)
5. [数据库设计](#数据库设计)
6. [常见修改场景](#常见修改场景)
7. [开发规范](#开发规范)

---

## 系统架构

### 技术架构

```
┌─────────────────────────────────────────────────┐
│              前端 (Thymeleaf + jQuery)           │
├─────────────────────────────────────────────────┤
│              Controller 层 (Spring MVC)          │
├─────────────────────────────────────────────────┤
│              Service 层 (业务逻辑)                │
├─────────────────────────────────────────────────┤
│              Mapper 层 (MyBatis)                 │
├─────────────────────────────────────────────────┤
│              Database (MySQL + Redis)            │
└─────────────────────────────────────────────────┘
```

### 核心模块

- **track**: 活动轨迹和复合事件模块（核心）
- **person**: 门禁信息模块
- **roomip**: 房间IP管理模块
- **caseapp**: 视频巡查模块

---

## 核心功能模块

### 1. 复合事件管理（最核心）

**功能说明**: 基于30秒空闲检测算法，将连续的活动轨迹自动聚合为复合事件

**核心文件**:
```
后端:
├── controller/AppTrackController.java          # HTTP接口
├── service/ICompositeEventService.java         # 服务接口
├── service/impl/CompositeEventServiceImpl.java # 核心业务逻辑 ⭐⭐⭐
├── mapper/CompositeEventMapper.java            # 数据访问接口
├── domain/CompositeEvent.java                  # 事件实体类
└── domain/AppTrack.java                        # 轨迹实体类

前端:
├── templates/sy.html                           # 复合事件列表页面 ⭐⭐⭐
└── templates/caseapp/track/event-list.html        # 轨迹列表页面

数据库:
├── app_composite_event                        # 复合事件表
├── app_composite_event_track_relation         # 事件-轨迹关联表
└── app_track                                   # 活动轨迹表
```

---

## 代码结构说明

### 完整目录树

```
caseapp/
├── src/main/
│   ├── java/com/ruoyi/project/caseapp/
│   │   └── track/                              # 活动轨迹模块
│   │       ├── controller/
│   │       │   └── AppTrackController.java    # 控制器（HTTP接口）
│   │       ├── domain/
│   │       │   ├── AppTrack.java              # 轨迹实体
│   │       │   └── CompositeEvent.java        # 复合事件实体
│   │       ├── mapper/
│   │       │   ├── AppTrackMapper.java        # 轨迹数据访问
│   │       │   ├── CompositeEventMapper.java  # 事件数据访问
│   │       │   └── CompositeEventTrackRelationMapper.java
│   │       └── service/
│   │           ├── IAppTrackService.java
│   │           ├── ICompositeEventService.java
│   │           └── impl/
│   │               ├── AppTrackServiceImpl.java
│   │               └── CompositeEventServiceImpl.java  # ⭐核心业务逻辑
│   │
│   └── resources/
│       ├── application.yml                    # 主配置文件
│       ├── application-druid.yml              # 数据库配置
│       ├── mybatis/
│       │   └── caseapp/
│       │       └── AppTrackMapper.xml         # SQL映射文件
│       └── templates/
│           ├── sy.html                        # 复合事件列表页 ⭐
│           └── caseapp/track/
│               └── event-list.html            # 轨迹列表页
│
├── pom.xml                                    # Maven依赖配置
├── DEPLOYMENT.md                              # 部署手册
└── MAINTENANCE.md                             # 维护手册（本文件）
```

---

## 功能修改指南

### 一、复合事件聚合算法修改

**场景**: 需要修改事件聚合的时间阈值（默认30秒）

**修改位置**: `CompositeEventServiceImpl.java`

**方法**: `updateOrCreateCompositeEventByTrack()`

**代码位置**: 第168-300行

```java
// 当前逻辑：时间间隔超过30秒则认为是新事件
private static final long IDLE_THRESHOLD_MS = 30 * 1000; // 30秒

// 修改示例：改为60秒
private static final long IDLE_THRESHOLD_MS = 60 * 1000; // 60秒
```

**关键代码**:
```java
long timeDiff = Math.abs(track.getPssj().getTime() - lastTrack.getPssj().getTime());
if (timeDiff > IDLE_THRESHOLD_MS) {
    // 时间间隔超过阈值，创建新事件
}
```

**影响范围**:
- 实时轨迹写入时的事件聚合
- 批量同步事件时的聚合逻辑
- 建议同时修改常量定义和使用的地方

---

### 二、事件列表页面定制

**场景**: 修改复合事件列表的显示字段、筛选条件、按钮等

**修改位置**: `templates/sy.html`

#### 2.1 修改显示列

**代码位置**: 第500-700行（事件卡片区域）

```html
<!-- 当前显示字段 -->
<div class="event-card">
    <div class="event-time">⏱️ ${event.startTime} - ${event.endTime}</div>
    <div class="event-info">📍 ${event.qymc} | 👥 最多${event.ryslMax}人</div>
    <!-- 添加新字段示例 -->
    <div class="event-custom">🔖 ${event.customField}</div>
</div>
```

#### 2.2 修改筛选条件

**代码位置**: 第100-200行（筛选表单）

```html
<!-- 添加新的筛选项 -->
<select id="customFilter" onchange="loadCompositeEvents()">
    <option value="">全部</option>
    <option value="1">自定义选项1</option>
</select>
```

**JavaScript修改**: 第1200-1400行
```javascript
function loadCompositeEvents() {
    const params = {
        bzzt: document.getElementById('filterBzzt').value,
        customField: document.getElementById('customFilter').value  // 新增
    };
    // ...
}
```

#### 2.3 添加新按钮

**代码位置**: 第580-600行（按钮区域）

```html
<!-- 在导出事件包按钮旁边添加 -->
<button onclick="customFunction()" style="background: #4CAF50;">
    🔧 自定义功能
</button>

<script>
function customFunction() {
    // 自定义功能逻辑
    alert('自定义功能');
}
</script>
```

---

### 三、事件包导出功能修改

**场景**: 修改导出内容、格式、路径等

**修改位置**: `CompositeEventServiceImpl.java`

#### 3.1 修改导出路径

**方法**: `batchExportEventPackages()`

**代码位置**: 第1550-1588行

```java
// 当前导出到桌面
String exportBasePath = System.getProperty("user.home") +
                       File.separator + "Desktop" +
                       File.separator + "事件包导出";

// 修改示例：导出到指定目录
String exportBasePath = "D:/exports/event_packages";
```

#### 3.2 修改导出内容

**方法**: `exportEventPackage()`

**代码位置**: 第806-876行

```java
// 当前导出内容：
// 1. HTML报告
// 2. 图片文件
// 3. 视频文件
// 4. JSON数据
// 5. README说明
// 6. PDF文档

// 添加新内容示例：导出Excel
private void generateExcelReport(CompositeEvent event, List<AppTrack> tracks, Path packagePath) {
    // 使用Apache POI生成Excel
    // ...
}

// 在exportEventPackage中调用
// 9. 生成Excel报告
generateExcelReport(event, tracks, packagePath);
```

#### 3.3 修改PDF格式

**方法**: `generatePdfReport()`

**代码位置**: 第1180-1433行

```java
// 修改PDF标题
String title = "资产视频监控事件说明书";  // 改为你的标题

// 修改表格字段
String[][] basicInfo = {
    {"事件编号", "COMP-" + event.getEventId()},
    {"自定义字段", event.getCustomField()},  // 添加新字段
    // ...
};

// 修改字体大小
float fontSize = 12;  // 改为你想要的大小
float titleFontSize = 16;
```

#### 3.4 修改导出文件夹命名

**方法**: `exportEventPackage()`

**代码位置**: 第844-848行

```java
// 当前命名格式
String folderName = String.format("事件包_COMP%d_%s", event.getEventId(), timestamp);

// 修改示例：添加区域信息
String folderName = String.format("事件包_%s_COMP%d_%s",
                                 event.getQymc(),
                                 event.getEventId(),
                                 timestamp);
```

---

### 四、标注功能修改

**场景**: 修改标注字段、标注逻辑、标注界面等

**修改位置**: 多个文件

#### 4.1 添加标注字段

**步骤1**: 修改数据库表

```sql
-- 在 app_composite_event 表中添加新字段
ALTER TABLE app_composite_event
ADD COLUMN custom_label VARCHAR(200) COMMENT '自定义标注';
```

**步骤2**: 修改实体类 `CompositeEvent.java`

```java
/** 自定义标注 */
private String customLabel;

// 生成getter和setter
public String getCustomLabel() {
    return customLabel;
}

public void setCustomLabel(String customLabel) {
    this.customLabel = customLabel;
}
```

**步骤3**: 修改标注方法 `CompositeEventServiceImpl.java`

**方法**: `annotateCompositeEvent()`

**代码位置**: 第734-797行

```java
public void annotateCompositeEvent(Long eventId, String xwyy, String ryxm,
                                   String wlry, String remark,
                                   String customLabel) {  // 新增参数
    // ...
    event.setCustomLabel(customLabel);  // 设置新字段
    compositeEventMapper.updateCompositeEvent(event);
}
```

**步骤4**: 修改前端标注表单 `sy.html`

**代码位置**: 第900-1100行（标注弹窗）

```html
<!-- 添加新的标注字段 -->
<div class="form-group">
    <label>自定义标注:</label>
    <input type="text" id="annotateCustomLabel" class="form-control">
</div>

<!-- 修改提交函数 -->
<script>
function submitAnnotation(eventId) {
    const data = {
        xwyy: document.getElementById('annotateXwyy').value,
        ryxm: document.getElementById('annotateRyxm').value,
        wlry: document.getElementById('annotateWlry').value,
        remark: document.getElementById('annotateRemark').value,
        customLabel: document.getElementById('annotateCustomLabel').value  // 新增
    };
    // 发送AJAX请求
}
</script>
```

#### 4.2 修改标注状态逻辑

**方法**: `annotateCompositeEvent()`

**代码位置**: 第760行

```java
// 当前逻辑：标注后自动设为"已标注"
event.setBzzt("1");

// 修改示例：根据标注内容决定状态
if (StringUtils.isNotEmpty(xwyy) && StringUtils.isNotEmpty(ryxm)) {
    event.setBzzt("1");  // 完整标注
} else {
    event.setBzzt("2");  // 部分标注
}
```

---

### 五、统计功能修改

**场景**: 修改首页统计卡片、添加新的统计维度

**修改位置**: `CompositeEventServiceImpl.java` + `sy.html`

#### 5.1 添加新的统计方法

**文件**: `ICompositeEventService.java`

```java
/**
 * 统计自定义维度
 */
public int countCustomDimension(String dimension);
```

**文件**: `CompositeEventServiceImpl.java`

```java
@Override
public int countCustomDimension(String dimension) {
    CompositeEvent queryParam = new CompositeEvent();
    queryParam.setCustomField(dimension);

    List<CompositeEvent> events = compositeEventMapper.selectCompositeEventList(queryParam);
    return events != null ? events.size() : 0;
}
```

#### 5.2 修改前端统计展示

**文件**: `sy.html`

**代码位置**: 第200-400行（统计卡片区域）

```html
<!-- 添加新的统计卡片 -->
<div class="stat-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
    <div class="stat-value" id="customStat">0</div>
    <div class="stat-label">自定义统计</div>
</div>

<!-- 修改加载统计的函数 -->
<script>
function loadStats() {
    // 原有统计
    fetch('/caseapp/track/compositeEvents/stats/today').then(...);

    // 新增统计
    fetch('/caseapp/track/compositeEvents/stats/custom')
        .then(response => response.json())
        .then(data => {
            document.getElementById('customStat').textContent = data;
        });
}
</script>
```

#### 5.3 添加统计接口

**文件**: `AppTrackController.java`

**代码位置**: 在其他统计接口附近添加

```java
/**
 * 统计自定义维度
 */
@GetMapping("/compositeEvents/stats/custom")
@ResponseBody
public int getCustomStats(@RequestParam(required = false) String dimension) {
    return compositeEventService.countCustomDimension(dimension);
}
```

---

### 六、数据同步功能修改

**场景**: 修改自动同步逻辑、同步时间范围等

**修改位置**: `CompositeEventServiceImpl.java`

#### 6.1 修改同步算法

**方法**: `syncAllCompositeEvents()`

**代码位置**: 第419-680行

```java
// 当前逻辑：删除所有旧事件，重新聚合
// 修改为增量同步：只处理变化的数据

public int syncAllCompositeEvents(AppTrack appTrack) {
    // 获取上次同步时间
    Date lastSyncTime = getLastSyncTime();

    // 只查询增量数据
    appTrack.setStartTime(lastSyncTime);
    List<AppTrack> newTracks = appTrackMapper.selectAppTrackList(appTrack);

    // 处理增量数据
    for (AppTrack track : newTracks) {
        updateOrCreateCompositeEventByTrack(track);
    }

    // 更新同步时间
    saveLastSyncTime(new Date());

    return newTracks.size();
}
```

#### 6.2 添加定时同步

**创建定时任务类**: `ScheduledTasks.java`

```java
package com.ruoyi.project.caseapp.track.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CompositeEventSyncTask {

    @Autowired
    private ICompositeEventService compositeEventService;

    /**
     * 每小时同步一次（cron表达式）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncEvents() {
        try {
            AppTrack queryParam = new AppTrack();
            int count = compositeEventService.syncAllCompositeEvents(queryParam);
            System.out.println("定时同步完成，处理事件数：" + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**启用定时任务**: `Application.java`

```java
@SpringBootApplication
@EnableScheduling  // 添加这个注解
public class RuoYiApplication {
    // ...
}
```

---

### 七、视频文件处理修改

**场景**: 修改视频存储路径、格式支持等

**修改位置**: `application.yml` + `CompositeEventServiceImpl.java`

#### 7.1 修改视频存储路径

**配置文件**: `application.yml`

```yaml
ruoyi:
  # 修改文件上传路径
  profile: D:/video_storage/uploadPath

hkpt:
  # 修改视频下载路径
  xzwjjmc: E:/video_download
```

#### 7.2 修改视频复制逻辑

**方法**: `copyMediaFiles()`

**代码位置**: 第881-937行

```java
// 添加视频格式验证
private boolean isValidVideoFormat(String fileName) {
    String[] validFormats = {".mp4", ".avi", ".flv", ".mkv"};
    for (String format : validFormats) {
        if (fileName.toLowerCase().endsWith(format)) {
            return true;
        }
    }
    return false;
}

// 在复制视频时调用
if (StringUtils.isNotEmpty(track.getSpdz())) {
    String videoPath = track.getSpdz();

    if (!isValidVideoFormat(videoPath)) {
        logger.warn("不支持的视频格式: " + videoPath);
        continue;
    }

    // 继续复制逻辑...
}
```

---

### 八、数据库查询优化

**场景**: 修改查询条件、添加索引、优化性能

#### 8.1 修改MyBatis SQL

**文件**: `mybatis/caseapp/AppTrackMapper.xml`

**位置**: 查找 `selectCompositeEventList`

```xml
<!-- 添加新的查询条件 -->
<select id="selectCompositeEventList" resultMap="CompositeEventResult">
    SELECT * FROM app_composite_event
    <where>
        <if test="bzzt != null and bzzt != ''">
            AND bzzt = #{bzzt}
        </if>

        <!-- 添加新条件 -->
        <if test="customField != null and customField != ''">
            AND custom_field = #{customField}
        </if>

        <!-- 时间范围查询 -->
        <if test="params.beginTime != null and params.beginTime != ''">
            AND start_time &gt;= #{params.beginTime}
        </if>
        <if test="params.endTime != null and params.endTime != ''">
            AND start_time &lt;= #{params.endTime}
        </if>
    </where>
    ORDER BY start_time DESC
</select>
```

#### 8.2 添加数据库索引

```sql
-- 常用查询字段添加索引
CREATE INDEX idx_composite_event_start_time ON app_composite_event(start_time);
CREATE INDEX idx_composite_event_bzzt ON app_composite_event(bzzt);
CREATE INDEX idx_composite_event_qymc ON app_composite_event(qymc);

-- 联合索引（用于多条件查询）
CREATE INDEX idx_composite_event_bzzt_time ON app_composite_event(bzzt, start_time);

-- 轨迹表索引
CREATE INDEX idx_track_pssj ON app_track(pssj);
CREATE INDEX idx_track_qymc_pssj ON app_track(qymc, pssj);
```

---

## 数据库设计

### 核心表结构

#### 1. app_composite_event（复合事件表）

```sql
CREATE TABLE app_composite_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_id BIGINT NOT NULL COMMENT '事件ID（第一条轨迹的ID）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration INT COMMENT '持续时长（分钟）',
    qymc VARCHAR(100) COMMENT '主要区域名称',
    path_areas VARCHAR(500) COMMENT '行为路径（多个区域）',
    track_count INT COMMENT '轨迹数量',
    rysl_max INT COMMENT '最大人数',

    -- 异常标记
    has_nonworktime TINYINT DEFAULT 0 COMMENT '是否有非工作时间进入（0否1是）',
    has_abnormal_person TINYINT DEFAULT 0 COMMENT '是否有人员数量异常（0否1是）',

    -- 标注信息
    bzzt CHAR(1) DEFAULT '0' COMMENT '标注状态（0未标注1已标注）',
    xwyy VARCHAR(200) COMMENT '行为原因',
    ryxm VARCHAR(100) COMMENT '人员姓名',
    wlry VARCHAR(100) COMMENT '外来人员',
    bz VARCHAR(500) COMMENT '备注',

    -- 时间戳
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_event_id (event_id),
    INDEX idx_start_time (start_time),
    INDEX idx_bzzt (bzzt)
) COMMENT '复合事件表';
```

#### 2. app_composite_event_track_relation（事件-轨迹关联表）

```sql
CREATE TABLE app_composite_event_track_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL COMMENT '复合事件ID（app_composite_event.id）',
    track_id BIGINT NOT NULL COMMENT '轨迹ID（app_track.id）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_event_track (event_id, track_id),
    INDEX idx_event_id (event_id),
    INDEX idx_track_id (track_id)
) COMMENT '复合事件与轨迹关联表';
```

#### 3. app_track（活动轨迹表）

```sql
CREATE TABLE app_track (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pssj DATETIME COMMENT '拍摄时间',
    qymc VARCHAR(100) COMMENT '区域名称',
    rysl INT COMMENT '人员数量',
    pstp VARCHAR(500) COMMENT '拍摄图片路径',
    spdz VARCHAR(500) COMMENT '视频地址',

    -- 标注信息（从复合事件继承）
    bzzt CHAR(1) DEFAULT '0' COMMENT '标注状态',
    xwyy VARCHAR(200) COMMENT '行为原因',
    ryxm VARCHAR(100) COMMENT '人员姓名',
    wlry VARCHAR(100) COMMENT '外来人员',
    bz VARCHAR(500) COMMENT '备注',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_pssj (pssj),
    INDEX idx_qymc (qymc)
) COMMENT '活动轨迹表';
```

### 表关系图

```
app_composite_event (复合事件)
    ↓ 1:N
app_composite_event_track_relation (关联表)
    ↓ N:1
app_track (活动轨迹)
```

### 重要字段说明

| 字段 | 用途 | 注意事项 |
|------|------|----------|
| event_id | 事件唯一标识 | 等于该事件第一条轨迹的ID |
| track_count | 轨迹数量 | 冗余字段，方便查询 |
| has_nonworktime | 非工作时间标记 | 用于快速筛选异常事件 |
| bzzt | 标注状态 | 0未标注/1已标注 |
| duration | 持续时长 | 单位：分钟，自动计算 |

---

## 常见修改场景

### 场景1: 添加新的事件属性

**需求**: 给复合事件添加"风险等级"字段

**步骤**:

1. **修改数据库**:
```sql
ALTER TABLE app_composite_event
ADD COLUMN risk_level VARCHAR(20) DEFAULT 'low' COMMENT '风险等级（low/medium/high）';
```

2. **修改实体类** `CompositeEvent.java`:
```java
/** 风险等级 */
private String riskLevel;

public String getRiskLevel() {
    return riskLevel;
}

public void setRiskLevel(String riskLevel) {
    this.riskLevel = riskLevel;
}
```

3. **修改前端显示** `sy.html`:
```html
<div class="event-risk">
    风险等级: <span class="risk-${event.riskLevel}">${event.riskLevel}</span>
</div>
```

4. **添加自动计算逻辑** `CompositeEventServiceImpl.java`:
```java
private String calculateRiskLevel(CompositeEvent event) {
    if (event.getHasNonworktime() == 1 && event.getRyslMax() > 5) {
        return "high";
    } else if (event.getHasNonworktime() == 1 || event.getRyslMax() > 3) {
        return "medium";
    } else {
        return "low";
    }
}

// 在创建/更新事件时调用
event.setRiskLevel(calculateRiskLevel(event));
```

---

### 场景2: 修改事件聚合规则

**需求**: 除了时间间隔，还要考虑区域变化

**修改位置**: `CompositeEventServiceImpl.java`

**方法**: `updateOrCreateCompositeEventByTrack()`

```java
// 原逻辑：只看时间间隔
long timeDiff = Math.abs(track.getPssj().getTime() - lastTrack.getPssj().getTime());
if (timeDiff > IDLE_THRESHOLD_MS) {
    // 创建新事件
}

// 新逻辑：时间间隔 + 区域变化
long timeDiff = Math.abs(track.getPssj().getTime() - lastTrack.getPssj().getTime());
boolean areaChanged = !track.getQymc().equals(lastTrack.getQymc());

if (timeDiff > IDLE_THRESHOLD_MS || areaChanged) {
    // 时间间隔超过阈值 或 区域发生变化，创建新事件
    createNewEvent = true;
}
```

---

### 场景3: 批量导出增加进度显示

**需求**: 批量导出时显示实时进度

**修改位置**: `AppTrackController.java` + `sy.html`

**后端**: 使用WebSocket或SSE推送进度

```java
@Autowired
private SimpMessagingTemplate messagingTemplate;

@PostMapping("/compositeEvents/batchExport")
@ResponseBody
public AjaxResult batchExportEventPackages(String eventIds) {
    List<Long> eventIdList = parseEventIds(eventIds);

    // 异步执行导出
    CompletableFuture.runAsync(() -> {
        for (int i = 0; i < eventIdList.size(); i++) {
            Long eventId = eventIdList.get(i);

            try {
                exportEventPackage(eventId, exportBasePath);

                // 推送进度
                int progress = (i + 1) * 100 / eventIdList.size();
                messagingTemplate.convertAndSend("/topic/export-progress", progress);

            } catch (Exception e) {
                logger.error("导出失败", e);
            }
        }
    });

    return AjaxResult.success("导出任务已启动");
}
```

**前端**: `sy.html`

```javascript
// 连接WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function() {
    stompClient.subscribe('/topic/export-progress', function(message) {
        const progress = JSON.parse(message.body);
        document.getElementById('progressBar').style.width = progress + '%';
    });
});
```

---

### 场景4: 添加事件审批流程

**需求**: 标注后需要审批才能生效

**步骤**:

1. **添加审批状态字段**:
```sql
ALTER TABLE app_composite_event
ADD COLUMN approval_status VARCHAR(20) DEFAULT 'pending' COMMENT '审批状态（pending/approved/rejected）',
ADD COLUMN approver VARCHAR(50) COMMENT '审批人',
ADD COLUMN approval_time DATETIME COMMENT '审批时间';
```

2. **修改标注逻辑**:
```java
public void annotateCompositeEvent(...) {
    event.setBzzt("0");  // 暂不标注
    event.setApprovalStatus("pending");  // 待审批
    compositeEventMapper.updateCompositeEvent(event);
}
```

3. **添加审批方法**:
```java
public void approveEvent(Long eventId, String approver, boolean approved) {
    CompositeEvent event = selectCompositeEventById(eventId);

    if (approved) {
        event.setApprovalStatus("approved");
        event.setBzzt("1");  // 审批通过后才标注
    } else {
        event.setApprovalStatus("rejected");
    }

    event.setApprover(approver);
    event.setApprovalTime(new Date());

    compositeEventMapper.updateCompositeEvent(event);
}
```

4. **添加审批页面**: 创建新的HTML页面或在现有页面添加审批按钮

---

## 开发规范

### 代码规范

1. **命名规范**:
   - 类名: 大驼峰 `CompositeEventService`
   - 方法名: 小驼峰 `selectCompositeEventById`
   - 常量: 全大写下划线 `IDLE_THRESHOLD_MS`
   - 数据库表: 小写下划线 `app_composite_event`

2. **注释规范**:
```java
/**
 * 根据轨迹更新或创建复合事件（核心方法）
 * 应用层实时写入：当轨迹数据变化时，自动计算并更新复合事件
 *
 * @param track 新增或修改的轨迹
 * @return 无返回值
 * @throws Exception 处理异常
 */
public void updateOrCreateCompositeEventByTrack(AppTrack track) {
    // 方法实现
}
```

3. **异常处理**:
```java
try {
    // 业务逻辑
} catch (Exception e) {
    logger.error("操作失败：" + e.getMessage(), e);
    throw new RuntimeException("操作失败", e);
}
```

### 日志规范

```java
// 使用slf4j日志
private static final Logger logger = LoggerFactory.getLogger(CompositeEventServiceImpl.class);

// 不同级别的日志
logger.debug("调试信息：变量值 = {}", value);
logger.info("操作信息：用户{}执行了{}", user, action);
logger.warn("警告信息：发现异常数据 {}", data);
logger.error("错误信息：操作失败", exception);
```

### SQL规范

```xml
<!-- MyBatis XML规范 -->
<select id="selectCompositeEventList" resultMap="CompositeEventResult">
    SELECT
        id, event_id, start_time, end_time,
        duration, qymc, bzzt
    FROM app_composite_event
    <where>
        <if test="bzzt != null and bzzt != ''">
            AND bzzt = #{bzzt}
        </if>
        <if test="params.beginTime != null">
            AND start_time >= #{params.beginTime}
        </if>
    </where>
    ORDER BY start_time DESC
    LIMIT #{pageNum}, #{pageSize}
</select>
```

### 前端规范

```javascript
// 函数命名：小驼峰
function loadCompositeEvents() {
    // 实现
}

// 常量：全大写
const MAX_RETRY_COUNT = 3;

// AJAX请求错误处理
fetch(url)
    .then(response => response.json())
    .then(data => {
        // 成功处理
    })
    .catch(error => {
        console.error('请求失败:', error);
        alert('操作失败，请重试');
    });
```

---

## 调试技巧

### 1. 查看SQL执行

**方法1**: 开启MyBatis日志

`application.yml`:
```yaml
logging:
  level:
    com.ruoyi.project.caseapp.track.mapper: debug
```

**方法2**: 使用Druid监控

访问: `https://localhost:8090/druid/sql.html`

### 2. 前端调试

```javascript
// 在浏览器控制台查看请求
console.log('发送请求:', url, params);

// 查看响应数据
console.log('响应数据:', data);

// 断点调试
debugger;
```

### 3. 后端调试

在IDE中设置断点，然后启动调试模式:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

---

## 性能优化建议

### 1. 数据库优化

- 添加必要索引
- 定期清理历史数据
- 使用分页查询
- 避免N+1查询

### 2. 缓存优化

```java
@Cacheable(value = "compositeEvents", key = "#eventId")
public CompositeEvent selectCompositeEventById(Long eventId) {
    return compositeEventMapper.selectCompositeEventById(eventId);
}
```

### 3. 异步处理

```java
@Async
public void asyncExportEventPackage(Long eventId) {
    // 异步导出，不阻塞主线程
}
```

---

## 常见错误处理

### 错误1: 空指针异常

```java
// 错误写法
String qymc = event.getQymc().trim();

// 正确写法
String qymc = event.getQymc() != null ? event.getQymc().trim() : "";
```

### 错误2: 并发问题

```java
// 使用同步或数据库锁避免并发问题
@Transactional
public synchronized void updateOrCreateCompositeEventByTrack(AppTrack track) {
    // 处理逻辑
}
```

### 错误3: 内存溢出

```java
// 大量数据导出时分批处理
int batchSize = 100;
for (int i = 0; i < totalCount; i += batchSize) {
    List<CompositeEvent> batch = selectBatch(i, batchSize);
    processBatch(batch);
    batch.clear();  // 及时清理
}
```

---

## 联系方式

**技术支持**: 开发团队
**维护手册版本**: v1.0
**最后更新**: 2025-12-27

---

## 附录：快速查找代码位置

| 功能 | 文件位置 | 关键方法/行号 |
|------|----------|--------------|
| 事件聚合算法 | CompositeEventServiceImpl.java | updateOrCreateCompositeEventByTrack() 第168行 |
| 批量导出 | CompositeEventServiceImpl.java | batchExportEventPackages() 第1550行 |
| PDF生成 | CompositeEventServiceImpl.java | generatePdfReport() 第1180行 |
| 事件列表页 | sy.html | 全文件 |
| 标注功能 | CompositeEventServiceImpl.java | annotateCompositeEvent() 第734行 |
| 数据同步 | CompositeEventServiceImpl.java | syncAllCompositeEvents() 第419行 |
| 统计功能 | CompositeEventServiceImpl.java | countTodayEvents() 第1465行 |
| 数据库配置 | application-druid.yml | 第8-11行 |
| 文件上传路径 | application.yml | 第12行 |
| HTTP接口 | AppTrackController.java | 全文件 |
