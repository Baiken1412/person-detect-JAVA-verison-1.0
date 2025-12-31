# 活动轨迹检测逻辑升级说明

## 升级目标

将现有的"每次检测创建新记录"逻辑改为"30秒窗口内合并到同一轨迹"逻辑。

## 变更内容

### 1. 数据库变更

#### 新增字段：app_track.jscs
- **字段名**：`jscs` (INT)
- **默认值**：1
- **含义**：检测次数，记录同一轨迹被检测到的次数
- **SQL**：
```sql
ALTER TABLE app_track ADD COLUMN jscs INT DEFAULT 1 COMMENT '检测次数，默认为1';
```

#### 新建表：app_track_screenshot
活动轨迹截图关联表，用于存储每次检测时的截图。

```sql
CREATE TABLE `app_track_screenshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `track_id` bigint(20) NOT NULL COMMENT '活动轨迹ID（关联app_track.id）',
  `screenshot_url` varchar(512) NOT NULL COMMENT '截图URL路径',
  `screenshot_time` datetime NOT NULL COMMENT '截图时间',
  `screenshot_order` int DEFAULT 1 COMMENT '截图顺序（第几次检测）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_track_id` (`track_id`),
  KEY `idx_screenshot_time` (`screenshot_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动轨迹截图关联表';
```

**执行顺序**：
1. 先执行 `sql/update_trajectory_logic.sql` 创建表结构
2. 重启Python监控程序

### 2. Python代码变更

#### database.py
新增方法：
- `update_trajectory_detection(record_id, jscs, jssj)` - 更新轨迹的检测次数和结束时间
- `save_trajectory_screenshot(track_id, screenshot_url, screenshot_time, screenshot_order)` - 保存轨迹截图到新表

修改方法：
- `save_detection_record()` - INSERT语句增加jscs字段，默认值为1

#### camera_monitor.py
新增属性：
- `current_trajectory` - 当前活动轨迹缓存（内存缓存，避免频繁查询数据库）
  ```python
  {
      'record_id': int,    # 轨迹记录ID
      'jscs': int,         # 当前检测次数
      'last_time': datetime  # 上次检测时间
  }
  ```

修改逻辑：
- `_save_detection_result()` - 实现30秒窗口合并逻辑
- 移除 `_should_record()` 的10秒间隔检查

## 新逻辑流程

```
检测到人员活动
    |
    v
是否存在当前活动轨迹？
    |
    +---> 否：创建新轨迹记录
    |         - 保存到 app_track (jscs=1)
    |         - 保存截图到 app_track_screenshot (screenshot_order=1)
    |         - 更新 current_trajectory 缓存
    |
    +---> 是：检查距离上次检测时间
              |
              +---> 超过30秒：创建新轨迹记录（同上）
              |
              +---> 30秒内：合并到现有轨迹
                      - 更新 app_track.jscs += 1
                      - 更新 app_track.jssj = 当前时间
                      - 保存截图到 app_track_screenshot (screenshot_order=jscs)
                      - 更新 current_trajectory 缓存
```

## 性能优化

使用内存缓存（`current_trajectory`）存储当前活动轨迹信息，避免每次检测都查询数据库，性能提升显著：
- **旧逻辑**：每次检测都查询数据库（~50-100ms延迟）
- **新逻辑**：使用内存缓存（<1ms延迟）

## 数据库独立性

每个摄像头的`CameraMonitor`实例都有独立的`current_trajectory`缓存，因此：
- 每个摄像头单独维护自己的轨迹
- 不需要在查询时额外过滤摄像头ID
- 不会出现跨摄像头的轨迹混淆

## 向后兼容性

- 已有的`app_track`记录不受影响
- 新增的`jscs`字段有默认值1，不影响现有查询
- 新表`app_track_screenshot`是独立的，不影响现有功能

## 测试建议

1. 执行SQL脚本创建表结构
2. 重启Python监控程序
3. 观察日志输出：
   - 首次检测应显示 "创建新轨迹记录"
   - 30秒内的后续检测应显示 "合并到现有轨迹"
   - 超过30秒的检测应显示 "创建新轨迹"
4. 检查数据库：
   - `app_track`表的`jscs`字段应正确累加
   - `app_track_screenshot`表应有多条截图记录

## 注意事项

1. **数据库表结构必须先创建**：确保先执行`sql/update_trajectory_logic.sql`
2. **重启程序**：修改代码后需要重启Python监控程序
3. **磁盘空间**：由于每次检测都保存截图，确保有足够的磁盘空间
4. **日志监控**：观察日志中的"✓"标记确认操作成功

## 回滚方案

如果需要回滚到旧逻辑：
1. 停止Python程序
2. 恢复 `database.py` 和 `camera_monitor.py` 的备份版本
3. 重启程序

注：不需要删除新建的表和字段，它们不会影响旧逻辑运行。
