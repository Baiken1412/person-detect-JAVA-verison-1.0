# 关系表重构 - 功能测试清单

## 📋 测试前准备

1. ✅ 已创建关系表
2. ✅ 已迁移数据
3. ✅ 已编译代码
4. ✅ 已重启应用

## 🧪 测试步骤

### 1. 数据迁移验证

**执行SQL验证：**
```bash
mysql -u root -p caseapp < sql\verify_migration.sql
```

**检查点：**
- [ ] 所有验证项显示 `✅ PASS`
- [ ] 不匹配事件详情为空
- [ ] 样本数据中 track_ids 一致

**预期结果示例：**
```
test_name                     | status
------------------------------|----------
1. 数据量验证                 | ✅ PASS
2. 轨迹数量一致性验证         | ✅ PASS
3. 不匹配事件详情             | (空结果)
4. 顺序号连续性验证           | ✅ PASS
5. 轨迹ID有效性验证           | ✅ PASS
```

---

### 2. 复合事件列表查询

**操作步骤：**
1. 访问：`http://localhost:端口/caseapp/track`
2. 等待页面加载完成
3. 查看复合事件列表

**检查点：**
- [ ] 复合事件列表正常显示
- [ ] 每个事件显示轨迹数量（X 个关联事件）
- [ ] 显示路径信息
- [ ] 显示时间范围
- [ ] 已标注事件显示标注信息（行为原因、管理员）

**测试SQL（手动验证）：**
```sql
-- 查看某个事件的关系表数据
SELECT
    e.id AS event_id,
    e.event_id AS business_id,
    COUNT(r.track_id) AS track_count,
    GROUP_CONCAT(r.track_id ORDER BY r.seq_no) AS track_ids
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id
ORDER BY e.id DESC
LIMIT 5;
```

**预期结果：**
- 列表数据与数据库一致
- 轨迹数量正确

---

### 3. 复合事件详情查看

**操作步骤：**
1. 点击复合事件卡片的"查看详情"按钮
2. 等待详情页加载
3. 查看轨迹时间线

**检查点：**
- [ ] 详情页正常打开
- [ ] 显示正确的事件信息（路径、时长、时间范围）
- [ ] 轨迹时间线显示完整
- [ ] 轨迹顺序正确（按时间排序）
- [ ] 轨迹数量与列表页一致
- [ ] 已标注事件显示标注信息

**测试SQL：**
```sql
-- 验证某个事件的轨迹顺序
SELECT
    r.event_id,
    r.seq_no,
    r.track_id,
    h.pssj AS track_time,
    h.qymc AS area
FROM app_event_track_relation r
LEFT JOIN app_track h ON r.track_id = h.id
WHERE r.event_id = (
    SELECT id FROM app_composite_event ORDER BY id DESC LIMIT 1
)
ORDER BY r.seq_no;
```

**预期结果：**
- seq_no 从 1 开始连续
- track_time 按时间顺序排列

---

### 4. 复合事件标注功能

**操作步骤：**
1. 在复合事件列表页，点击"标注事件"按钮
2. 填写表单：
   - 行为原因：选择"盘库"
   - 管理员姓名：输入"张三,李四"
   - 外来人员：输入"王五"
3. 点击"保存标注"
4. 刷新页面

**检查点：**
- [ ] 标注弹窗正常打开
- [ ] 表单验证正常（行为原因必填）
- [ ] 保存成功提示
- [ ] 列表页显示标注信息
- [ ] 详情页显示标注信息

**测试SQL：**
```sql
-- 验证标注数据
SELECT
    e.id,
    e.event_id,
    e.bzzt AS status,
    e.xwyy AS reason,
    e.ryxm AS persons,
    e.wlry AS outsiders,
    COUNT(h.id) AS annotated_tracks
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
LEFT JOIN app_track h ON r.track_id = h.id AND h.bzzt = '1'
WHERE e.bzzt = '1'
GROUP BY e.id
ORDER BY e.id DESC
LIMIT 5;
```

**预期结果：**
- 复合事件的 bzzt = '1'
- 标注信息正确保存
- 关联的所有轨迹也被标注（bzzt = '1'）

---

### 5. 复合事件导出功能

**操作步骤：**
1. 勾选 2-3 个复合事件
2. 点击"导出选中台账"按钮
3. 确认导出
4. 等待下载完成
5. 打开Excel文件

**检查点：**
- [ ] 复选框可以正常选择
- [ ] 全选/反选按钮正常工作
- [ ] 导出成功提示
- [ ] Excel文件正常下载
- [ ] Excel包含所有选中的事件
- [ ] 每个事件的轨迹数据完整

**测试SQL：**
```sql
-- 验证某些事件的导出数据
SELECT
    e.id,
    e.event_id,
    e.start_time,
    e.end_time,
    e.track_count,
    e.xwyy,
    e.ryxm,
    COUNT(r.track_id) AS actual_tracks
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
WHERE e.event_id IN (选中的事件ID列表)
GROUP BY e.id;
```

**预期结果：**
- Excel数据与数据库一致
- track_count = actual_tracks

---

### 6. 复合事件同步功能

**操作步骤：**
1. 点击"重新同步复合事件"按钮
2. 确认同步
3. 等待同步完成
4. 查看同步结果

**检查点：**
- [ ] 同步成功提示（显示处理的事件数量）
- [ ] 关系表数据更新
- [ ] 新生成的事件关系正确

**测试SQL（同步前后对比）：**
```sql
-- 同步前：记录当前关系表数据量
SELECT COUNT(*) AS before_count FROM app_event_track_relation;

-- 执行同步操作（点击按钮）

-- 同步后：验证关系表数据
SELECT
    COUNT(*) AS after_count,
    COUNT(DISTINCT event_id) AS unique_events,
    MAX(created_at) AS last_sync_time
FROM app_event_track_relation;

-- 验证最新事件的关系
SELECT
    e.id,
    e.event_id,
    e.track_count,
    COUNT(r.track_id) AS relation_count
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
WHERE e.created_at > NOW() - INTERVAL 1 HOUR
GROUP BY e.id
ORDER BY e.id DESC
LIMIT 5;
```

**预期结果：**
- 关系表数据量增加（如果有新事件）
- track_count = relation_count
- 最新事件的 created_at 是最近时间

---

### 7. 筛选功能测试

**操作步骤：**
1. 选择日期范围
2. 选择区域
3. 选择标注状态
4. 点击"筛选"按钮

**检查点：**
- [ ] 筛选结果正确
- [ ] 筛选后的事件数量正确
- [ ] 事件详情仍然正常显示

---

### 8. 性能测试（可选）

**测试目标：验证关系表查询性能**

**测试SQL：**
```sql
-- 旧方式模拟（字符串分割）
SELECT BENCHMARK(1000, (
    SELECT track_ids FROM app_composite_event WHERE id = 1
));

-- 新方式（关系表查询）
SELECT BENCHMARK(1000, (
    SELECT GROUP_CONCAT(track_id) FROM app_event_track_relation WHERE event_id = 1
));

-- 查看索引使用情况
EXPLAIN SELECT * FROM app_event_track_relation WHERE event_id = 123;
```

**预期结果：**
- 关系表查询使用了索引（key = idx_event_id）
- 查询速度应该更快或持平

---

## ✅ 测试完成检查

全部测试通过后，确认以下内容：

- [ ] 所有功能正常运行
- [ ] 数据迁移完整无误
- [ ] 关系表正确使用
- [ ] 性能没有下降
- [ ] 用户体验没有变化

## 📊 测试结果记录

| 测试项 | 结果 | 备注 |
|--------|------|------|
| 1. 数据迁移验证 | ⬜ PASS / ❌ FAIL | |
| 2. 复合事件列表查询 | ⬜ PASS / ❌ FAIL | |
| 3. 复合事件详情查看 | ⬜ PASS / ❌ FAIL | |
| 4. 复合事件标注功能 | ⬜ PASS / ❌ FAIL | |
| 5. 复合事件导出功能 | ⬜ PASS / ❌ FAIL | |
| 6. 复合事件同步功能 | ⬜ PASS / ❌ FAIL | |
| 7. 筛选功能测试 | ⬜ PASS / ❌ FAIL | |
| 8. 性能测试 | ⬜ PASS / ❌ FAIL | |

## 🐛 问题记录

如果发现问题，请记录：

**问题1：**
- **现象：**
- **重现步骤：**
- **错误信息：**
- **影响范围：**

---

## 📞 需要帮助？

如果测试中遇到问题，请提供：
1. 具体的错误信息
2. 浏览器控制台日志
3. 应用服务器日志
4. 相关SQL查询结果

我会帮你诊断和解决！
