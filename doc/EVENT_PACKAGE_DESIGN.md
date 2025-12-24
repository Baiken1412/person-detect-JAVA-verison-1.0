# 事件可解释性报告 - 设计方案

## 功能概述
为复合事件生成完整的可解释性报告包，包含HTML主页、PDF报告、关键帧图片、视频片段、结构化数据等，形成可查看、可说明、可存档、可复核的标准化材料。

---

## 事件包文件结构

```
事件包_CE_20251224_100000_仓库A/
├── index.html              # 主入口页面（包含时间轴、关键节点、标注信息）
├── report.pdf              # 事件说明文档（PDF格式）
├── README.txt              # 使用说明
├── data/                   # 结构化数据目录
│   ├── event.json          # 事件完整数据（JSON格式）
│   ├── timeline.json       # 时间轴数据
│   └── metadata.json       # 元数据信息
├── images/                 # 关键帧图片目录
│   ├── track_001.jpg       # 第1个轨迹关键帧
│   ├── track_002.jpg       # 第2个轨迹关键帧
│   └── ...
├── videos/                 # 视频片段目录（可选）
│   ├── segment_001.mp4     # 第1段视频
│   ├── segment_002.mp4     # 第2段视频
│   └── ...
└── assets/                 # 静态资源（CSS、JS、图标等）
    ├── style.css           # 样式文件
    ├── timeline.js         # 时间轴交互脚本
    └── logo.png            # 系统Logo
```

---

## 数据来源映射

### 从现有数据提取

| 报告内容 | 数据来源 | 字段 |
|---------|---------|------|
| 事件ID | `app_composite_event.event_id` | 唯一标识 |
| 事件类型 | 根据特征判断 | 单人/多人/异常人员/夜间活动等 |
| 开始时间 | `app_composite_event.start_time` | 事件起始时间 |
| 结束时间 | `app_composite_event.end_time` | 事件结束时间 |
| 时长 | 计算 | end_time - start_time |
| 活动范围 | `app_composite_event.path_areas` | 经过的区域 |
| 主要区域 | `app_composite_event.qymc` | 主要活动区域 |
| 人员信息 | `app_composite_event.ryxm/wlry` | 管理员/外来人员 |
| 最大人数 | `app_composite_event.rysl_max` | 同时出现最多人数 |
| 标注状态 | `app_composite_event.bzzt` | 0=未标注, 1=已标注 |
| 标注原因 | `app_composite_event.xwyy` | 行为原因 |
| 非工作时间 | `app_composite_event.has_nonworktime` | 是否包含非工作时间 |
| 异常人员 | `app_composite_event.has_abnormal_person` | 是否有异常人员 |
| 关键帧图片 | `app_track.pstp` | 轨迹拍摄图片路径 |
| 视频片段 | `app_track.spdz` | 轨迹视频地址 |
| 轨迹时间 | `app_track.pssj/jssj` | 每个轨迹的时间 |
| 轨迹区域 | `app_track.qymc` | 每个轨迹的区域 |

---

## index.html 页面设计

### 页面结构

```html
<!DOCTYPE html>
<html>
<head>
    <title>事件可解释性报告 - CE_20251224_100000</title>
    <style>
        /* 响应式布局、时间轴样式 */
    </style>
</head>
<body>
    <!-- 1. 顶部导航 -->
    <header>
        <h1>事件可解释性报告</h1>
        <p>事件ID: CE_20251224_100000</p>
    </header>

    <!-- 2. 事件基本信息卡片 -->
    <section id="basic-info">
        <h2>基本信息</h2>
        <table>
            <tr><td>事件类型</td><td>单人活动</td></tr>
            <tr><td>开始时间</td><td>2025-12-24 10:00:00</td></tr>
            <tr><td>结束时间</td><td>2025-12-24 10:05:30</td></tr>
            <tr><td>持续时长</td><td>5分30秒</td></tr>
            <tr><td>活动范围</td><td>仓库A → 仓库B → 仓库C</td></tr>
            <tr><td>涉及人员</td><td>张三（管理员）</td></tr>
        </table>
    </section>

    <!-- 3. 标注信息（如果已标注）-->
    <section id="annotation" class="highlight">
        <h2>标注信息</h2>
        <p><strong>标注原因：</strong>正常巡检</p>
        <p><strong>标注人员：</strong>张三</p>
        <p><strong>标注时间：</strong>2025-12-24 15:00:00</p>
    </section>

    <!-- 4. 事件时间轴 -->
    <section id="timeline">
        <h2>事件时间轴</h2>
        <div class="timeline-container">
            <!-- 时间轴节点 -->
            <div class="timeline-item">
                <div class="time">10:00:00</div>
                <div class="content">
                    <h3>进入仓库A</h3>
                    <p>管理员张三进入仓库A区域</p>
                    <img src="images/track_001.jpg" width="200">
                    <a href="videos/segment_001.mp4">查看视频</a>
                </div>
            </div>
            <!-- 更多节点... -->
        </div>
    </section>

    <!-- 5. 关键节点说明 -->
    <section id="key-events">
        <h2>关键事件节点</h2>
        <ul>
            <li><strong>10:00:00</strong> - 开始活动，进入仓库A</li>
            <li><strong>10:02:15</strong> - 移动至仓库B</li>
            <li><strong>10:05:30</strong> - 活动结束，离开监控范围</li>
        </ul>
    </section>

    <!-- 6. 视频证据索引 -->
    <section id="evidence">
        <h2>证据材料索引</h2>
        <table>
            <thead>
                <tr>
                    <th>序号</th>
                    <th>时间</th>
                    <th>区域</th>
                    <th>关键帧</th>
                    <th>视频</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>1</td>
                    <td>10:00:00</td>
                    <td>仓库A</td>
                    <td><a href="images/track_001.jpg">查看图片</a></td>
                    <td><a href="videos/segment_001.mp4">查看视频</a></td>
                </tr>
                <!-- 更多记录... -->
            </tbody>
        </table>
    </section>

    <!-- 7. 关联材料快速访问 -->
    <section id="quick-access">
        <h2>关联材料</h2>
        <ul>
            <li><a href="report.pdf" target="_blank">📄 下载PDF报告</a></li>
            <li><a href="data/event.json" target="_blank">📊 查看结构化数据（JSON）</a></li>
            <li><a href="data/timeline.json" target="_blank">⏱️ 查看时间轴数据</a></li>
        </ul>
    </section>

    <!-- 8. 页脚 -->
    <footer>
        <p>报告生成时间: 2025-12-24 16:00:00</p>
        <p>系统版本: CASEAPP v1.0</p>
    </footer>

    <script src="assets/timeline.js"></script>
</body>
</html>
```

---

## PDF报告设计

### PDF内容结构

1. **封面**
   - 标题："事件可解释性报告"
   - 事件ID
   - 生成时间
   - 系统Logo

2. **第一部分：事件概览**
   - 事件基本信息表格
   - 事件统计图表（可选）

3. **第二部分：事件时间线**
   - 时间轴图示
   - 每个节点的详细说明

4. **第三部分：关键帧截图**
   - 嵌入每个轨迹的关键帧图片
   - 图片说明（时间、地点、人员）

5. **第四部分：标注信息**（如果已标注）
   - 标注原因
   - 标注人员
   - 标注时间

6. **第五部分：附录**
   - 视频索引列表
   - 结构化数据说明

---

## 结构化数据格式

### event.json 示例

```json
{
  "eventId": "CE_20251224_100000",
  "eventType": "单人活动",
  "startTime": "2025-12-24 10:00:00",
  "endTime": "2025-12-24 10:05:30",
  "duration": 330,
  "mainArea": "仓库A",
  "pathAreas": ["仓库A", "仓库B", "仓库C"],
  "personnel": {
    "type": "管理员",
    "name": "张三",
    "count": 1
  },
  "annotation": {
    "status": "已标注",
    "reason": "正常巡检",
    "time": "2025-12-24 15:00:00"
  },
  "flags": {
    "hasNonworktime": false,
    "hasAbnormalPerson": false
  },
  "tracks": [
    {
      "trackId": 1001,
      "time": "2025-12-24 10:00:00",
      "area": "仓库A",
      "image": "images/track_001.jpg",
      "video": "videos/segment_001.mp4"
    }
  ],
  "metadata": {
    "generatedAt": "2025-12-24 16:00:00",
    "version": "1.0"
  }
}
```

### timeline.json 示例

```json
{
  "eventId": "CE_20251224_100000",
  "timeline": [
    {
      "time": "2025-12-24 10:00:00",
      "type": "开始",
      "title": "进入仓库A",
      "description": "管理员张三进入仓库A区域",
      "area": "仓库A",
      "image": "images/track_001.jpg",
      "video": "videos/segment_001.mp4"
    },
    {
      "time": "2025-12-24 10:02:15",
      "type": "移动",
      "title": "移动至仓库B",
      "description": "从仓库A移动到仓库B",
      "area": "仓库B",
      "image": "images/track_002.jpg",
      "video": "videos/segment_002.mp4"
    },
    {
      "time": "2025-12-24 10:05:30",
      "type": "结束",
      "title": "离开监控范围",
      "description": "活动结束",
      "area": "仓库C",
      "image": "images/track_003.jpg",
      "video": "videos/segment_003.mp4"
    }
  ]
}
```

---

## 技术实现方案

### 后端实现

**技术栈：**
- Java 8+
- Spring Boot
- iText 7（PDF生成）
- Apache Commons Compress（ZIP打包）
- Freemarker/Thymeleaf（HTML模板）

**核心类设计：**

```java
// 事件包生成服务
@Service
public class EventPackageService {

    /**
     * 生成事件包（ZIP文件）
     * @param eventId 事件ID
     * @return ZIP文件路径
     */
    public String generateEventPackage(Long eventId) {
        // 1. 查询事件数据
        // 2. 创建临时目录
        // 3. 生成index.html
        // 4. 生成report.pdf
        // 5. 复制图片和视频
        // 6. 生成JSON数据文件
        // 7. 打包成ZIP
        // 8. 返回下载路径
    }

    /**
     * 生成HTML报告
     */
    private void generateHtmlReport(CompositeEvent event, String outputPath) {
        // 使用Freemarker模板生成HTML
    }

    /**
     * 生成PDF报告
     */
    private void generatePdfReport(CompositeEvent event, String outputPath) {
        // 使用iText生成PDF
    }

    /**
     * 复制媒体文件
     */
    private void copyMediaFiles(List<AppTrack> tracks, String outputDir) {
        // 复制图片和视频到对应目录
    }
}
```

### 前端实现

**在复合事件管理页面添加"生成报告包"按钮：**

```javascript
function generateEventPackage(eventId) {
    // 发送请求到后端
    $.ajax({
        url: '/caseapp/track/generateEventPackage',
        type: 'POST',
        data: { eventId: eventId },
        success: function(response) {
            if (response.code === 200) {
                // 下载ZIP文件
                window.location.href = response.data.downloadUrl;
            } else {
                alert('生成失败: ' + response.msg);
            }
        }
    });
}
```

---

## 视频处理方案（测试环境）

由于无法获取真实海康视频，我们采用以下方案：

### 方案1：使用静态测试视频
- 准备一些测试视频文件（可以是任意MP4文件）
- 在数据库中将 `spdz` 字段指向这些测试视频
- 生成事件包时复制这些测试视频

### 方案2：只包含关键帧图片
- 只导出 `pstp`（拍摄图片）字段的图片
- 不包含视频部分，或标注"视频暂不可用"

### 方案3：生成视频占位符
- 创建一个简单的占位视频（显示"测试视频"文字）
- 标注说明这是测试环境

**推荐方案2**，因为：
- 关键帧图片已经足够说明事件过程
- 不依赖视频文件，生成速度快
- 文件包体积小，便于存档和传输

---

## 生成流程

```
用户点击"生成报告包"
        ↓
后端查询事件和轨迹数据
        ↓
创建临时目录结构
        ↓
并行执行：
  - 生成index.html
  - 生成report.pdf
  - 生成JSON数据文件
  - 复制关键帧图片
  - （可选）复制视频文件
        ↓
打包成ZIP文件
        ↓
保存到临时目录
        ↓
返回下载URL给前端
        ↓
用户下载ZIP文件
        ↓
定时任务清理临时文件
```

---

## 优化建议

### 性能优化
1. **异步生成**：使用线程池异步生成，避免阻塞用户操作
2. **缓存机制**：对于相同事件的重复请求，直接返回已生成的包
3. **增量复制**：只复制需要的图片和视频，避免重复拷贝

### 功能扩展
1. **批量生成**：支持一次生成多个事件的报告包
2. **自定义模板**：允许管理员自定义HTML和PDF模板
3. **导出格式选择**：支持只导出HTML、只导出PDF等选项
4. **邮件发送**：生成后自动发送到指定邮箱

---

## 测试数据准备

为了测试这个功能，需要准备：

1. **测试图片**
   - 在 `D:\work\caseapp\src\main\resources\static\upload\` 目录下放置测试图片
   - 更新数据库中 `app_track.pstp` 字段指向这些图片

2. **测试视频**（可选）
   - 在 `D:\work\caseapp\src\main\resources\static\video\` 目录下放置测试视频
   - 更新数据库中 `app_track.spdz` 字段

3. **完整事件数据**
   - 确保有完整的复合事件和轨迹关联数据
   - 确保关系表数据正确

---

## 下一步实现计划

1. ✅ 设计文件结构和数据格式（已完成）
2. ⏳ 创建EventPackageService服务类
3. ⏳ 实现HTML模板生成
4. ⏳ 实现PDF报告生成（使用iText）
5. ⏳ 实现ZIP打包功能
6. ⏳ 添加Controller接口
7. ⏳ 前端添加"生成报告包"按钮
8. ⏳ 测试完整流程
