# 复合事件关系表重构 - 迁移指南

## 📋 概述

本次重构将复合事件与轨迹的关联关系从**逗号分隔字符串**改为**关系表设计**，提升数据库规范性和扩展性。

## 🎯 重构目标

### 旧设计（已废弃）
```
app_composite_event.track_ids = "501,502,503"  // 逗号分隔字符串
app_track.composite_event_id = 100              // 单一引用
```

### 新设计（推荐）
```sql
app_event_track_relation:
  (event_id=100, track_id=501, seq_no=1)
  (event_id=100, track_id=502, seq_no=2)
  (event_id=100, track_id=503, seq_no=3)
```

## ✅ 优势

1. **符合数据库规范**：避免逗号分隔字符串，符合第一范式
2. **支持多对多关系**：未来可支持一个轨迹属于多个事件
3. **查询性能更好**：可建立索引，查询更快
4. **维护更简单**：标准SQL操作，无需字符串解析
5. **可记录元数据**：顺序号、创建时间等

## 🚀 迁移步骤

### 步骤1：备份数据（必须！）

```bash
mysqldump -u root -p caseapp > app_backup_20251224.sql
```

### 步骤2：创建关系表

执行SQL脚本：
```bash
mysql -u root -p caseapp < sql/create_event_track_relation.sql
```

验证表已创建：
```sql
SHOW CREATE TABLE app_event_track_relation;
```

### 步骤3：数据迁移

执行迁移脚本（将track_ids数据迁移到关系表）：
```bash
mysql -u root -p caseapp < sql/migrate_to_relation_table.sql
```

**关键验证点：**
- 检查输出结果，确认 `迁移的关联记录数` 和 `迁移的事件数` 是否合理
- 检查验证结果，确认 `track_count_in_relation` = `track_count_in_event`

示例输出：
```
+------------------------+
| 迁移的关联记录数       |
+------------------------+
|                    150 |
+------------------------+

+------------------+
| 迁移的事件数     |
+------------------+
|               25 |
+------------------+
```

### 步骤4：编译并重启应用

```bash
# 编译Java代码
mvn clean compile

# 重启应用
# （根据你的启动方式执行相应命令）
```

### 步骤5：功能测试

**测试清单：**

1. **复合事件列表查询**
   - 访问复合事件管理页面
   - 检查事件是否正常显示
   - 检查轨迹数量是否正确

2. **复合事件详情**
   - 点击"查看详情"
   - 检查轨迹列表是否完整
   - 检查顺序是否正确

3. **复合事件标注**
   - 标注一个复合事件
   - 检查标注信息是否保存成功
   - 检查关联轨迹是否同步更新

4. **复合事件导出**
   - 选择几个事件导出
   - 检查Excel文件是否包含所有轨迹

5. **复合事件同步**
   - 点击"重新同步复合事件"
   - 检查新生成的事件关系表数据是否正确

### 步骤6：数据一致性验证

执行验证查询：
```sql
-- 验证关系表数据完整性
SELECT
    e.id AS event_id,
    e.event_id AS event_business_id,
    e.track_count AS expected_count,
    COUNT(r.track_id) AS actual_count,
    CASE
        WHEN e.track_count = COUNT(r.track_id) THEN 'OK'
        ELSE 'ERROR'
    END AS status
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id
HAVING status = 'ERROR';
```

**预期结果：** 没有返回任何行（所有事件数据一致）

### 步骤7：清理旧字段（可选，建议等待1-2周）

**警告：执行前请务必确认功能正常！**

```bash
mysql -u root -p caseapp < sql/cleanup_old_design.sql
```

这将删除 `track_ids` 字段（建议保留一段时间，确认无问题后再删除）

## 🔧 代码变更说明

### 新增文件

1. **实体类**
   - `EventTrackRelation.java` - 关系表实体

2. **Mapper**
   - `EventTrackRelationMapper.java` - Mapper接口
   - `EventTrackRelationMapper.xml` - MyBatis XML

3. **SQL脚本**
   - `create_event_track_relation.sql` - 创建关系表
   - `migrate_to_relation_table.sql` - 数据迁移
   - `cleanup_old_design.sql` - 清理旧字段

### 修改文件

1. **CompositeEventServiceImpl.java**
   - `syncAllCompositeEvents()` - 使用关系表保存关联
   - 移除 `composite_event_id` 回填逻辑

2. **AppTrackServiceImpl.java**
   - `selectCompositeEvents()` - 从关系表查询轨迹

3. **AppTrackController.java**
   - `exportCompositeEvents()` - 从关系表查询轨迹

## 🐛 常见问题

### Q1: 迁移后数据不一致怎么办？

**A:** 执行步骤6的验证查询，找出不一致的事件ID，手动修复：
```sql
-- 查看具体不一致的事件
SELECT * FROM app_composite_event WHERE id = xxx;
SELECT * FROM app_event_track_relation WHERE event_id = xxx;
```

### Q2: 可以回滚吗？

**A:** 可以！前提是你执行了步骤1的备份：
```bash
# 停止应用
# 恢复数据库
mysql -u root -p caseapp < app_backup_20251224.sql
# 回滚代码（使用git）
git checkout <previous_commit>
# 重启应用
```

### Q3: 为什么保留track_ids字段？

**A:** 兼容性和安全性考虑：
- 迁移脚本会保留原字段，不会删除
- 建议观察1-2周，确认新设计无问题后再删除
- 删除前可以进行最后一次数据对比验证

### Q4: 性能会受影响吗？

**A:** 不会，反而更好：
- 关系表有索引，查询更快
- 避免字符串分割操作
- MyBatis批量插入性能优秀

## 📞 支持

如有问题，请查看：
- 详细日志：检查应用日志
- SQL错误：检查MySQL错误日志
- 数据验证：使用步骤6的验证查询

## 🎉 完成！

恭喜！你已经成功完成复合事件关系表的重构。现在你的系统拥有更好的数据库设计和扩展性！
