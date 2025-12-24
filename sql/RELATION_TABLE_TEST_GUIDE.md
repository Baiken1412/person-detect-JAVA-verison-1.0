# 事件轨迹关系表测试指南

## 测试目标
验证 `app_event_track_relation` 表在以下场景下能够正常工作：
1. ✅ 新增复合事件时创建关系
2. ✅ 删除复合事件时清理关系
3. ✅ 删除轨迹时更新关系并重组事件
4. ✅ 查询事件的轨迹列表
5. ✅ 查询轨迹所属的事件

---

## 测试方案

### 第一步：数据库层面验证

#### 1.1 运行基础验证脚本
```bash
# 在MySQL客户端中运行
mysql -u your_username -p your_database < sql/test_relation_table.sql
```

**预期结果：**
- 关系表有数据（total_relations > 0）
- 无孤立事件（orphan_event_count = 0）
- 无孤立轨迹（orphan_track_count = 0）
- 序号连续（无不连续的序号）

#### 1.2 手动验证数据完整性
```sql
-- 查看最近创建的事件及其轨迹
SELECT
    e.event_id,
    e.start_time,
    GROUP_CONCAT(r.track_id ORDER BY r.seq_no) AS track_ids,
    COUNT(r.track_id) AS track_count
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id
ORDER BY e.create_time DESC
LIMIT 10;
```

---

### 第二步：应用层面功能测试

#### 测试场景1：新增复合事件 ✅

**操作步骤：**
1. 登录系统，进入"活动轨迹"页面
2. 筛选一些轨迹（建议筛选今天的数据）
3. 点击"重新同步复合事件"按钮
4. 等待同步完成

**验证步骤：**
```sql
-- 查看刚才创建的事件（假设刚才同步的事件start_time是今天）
SELECT
    '新增事件验证' AS test_type,
    e.id AS event_id,
    e.event_id AS composite_event_id,
    e.start_time,
    COUNT(r.track_id) AS track_count_in_relation,
    GROUP_CONCAT(r.track_id ORDER BY r.seq_no) AS track_ids
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
WHERE DATE(e.start_time) = CURDATE()
GROUP BY e.id
ORDER BY e.create_time DESC
LIMIT 5;
```

**预期结果：**
- 每个新事件都有对应的关系记录（track_count_in_relation > 0）
- track_ids 字段有值且按顺序排列

---

#### 测试场景2：查看复合事件详情 ✅

**操作步骤：**
1. 在"复合事件管理"页面，点击某个事件的"查看"按钮
2. 查看详情页面显示的轨迹列表

**验证步骤：**
```sql
-- 记录前端显示的事件ID（event_id），然后查询数据库
SET @frontend_event_id = 'CE_xxxxx'; -- 替换为前端显示的event_id

-- 查询该事件的轨迹（模拟前端查询逻辑）
SELECT
    h.id,
    h.pssj,
    h.qymc,
    h.ryxm,
    h.wlry,
    h.rysl
FROM app_composite_event e
JOIN app_event_track_relation r ON e.id = r.event_id
JOIN app_track h ON r.track_id = h.id
WHERE e.event_id = @frontend_event_id
ORDER BY r.seq_no;
```

**预期结果：**
- 数据库查询的轨迹与前端显示的轨迹一致
- 顺序相同（按 seq_no 排序）

---

#### 测试场景3：标注复合事件 ✅

**操作步骤：**
1. 在"复合事件管理"页面，选择一个未标注的事件
2. 点击"标注"按钮
3. 填写标注信息（行为原因、管理员、外来人员）
4. 提交

**验证步骤：**
```sql
-- 查询刚才标注的事件（假设event_id是'CE_xxxxx'）
SET @annotated_event_id = 'CE_xxxxx';

-- 验证复合事件表的标注信息
SELECT
    '复合事件标注信息' AS check_type,
    event_id,
    bzzt,
    xwyy,
    ryxm,
    wlry
FROM app_composite_event
WHERE event_id = @annotated_event_id;

-- 验证关联轨迹的标注信息是否同步更新
SELECT
    '关联轨迹标注信息' AS check_type,
    h.id AS track_id,
    h.bzzt,
    h.xwyy,
    h.ryxm,
    h.wlry
FROM app_composite_event e
JOIN app_event_track_relation r ON e.id = r.event_id
JOIN app_track h ON r.track_id = h.id
WHERE e.event_id = @annotated_event_id;
```

**预期结果：**
- 复合事件的 bzzt = '1'，标注信息已保存
- 所有关联轨迹的标注信息与复合事件一致

---

#### 测试场景4：删除复合事件 ✅

**操作步骤：**
1. 在"复合事件管理"页面，选择一个事件
2. 点击"删除"按钮
3. 确认删除

**验证步骤（在删除前先记录event_id）：**
```sql
-- 假设要删除的事件event_id是'CE_xxxxx'
SET @deleted_event_id = 'CE_xxxxx';

-- 删除前：记录关系数量
SELECT
    '删除前关系数量' AS status,
    COUNT(*) AS relation_count
FROM app_event_track_relation r
JOIN app_composite_event e ON r.event_id = e.id
WHERE e.event_id = @deleted_event_id;

-- 执行删除操作...

-- 删除后：验证关系表已清理
SELECT
    '删除后关系数量' AS status,
    COUNT(*) AS relation_count
FROM app_event_track_relation r
JOIN app_composite_event e ON r.event_id = e.id
WHERE e.event_id = @deleted_event_id;
```

**预期结果：**
- 删除前：relation_count > 0
- 删除后：relation_count = 0（关系记录已被级联删除）

---

#### 测试场景5：删除轨迹（触发事件重组） ✅

**操作步骤：**
1. 在"活动轨迹"页面，找到一个已经属于某个复合事件的轨迹
2. 记录该轨迹所属的事件ID
3. 删除该轨迹
4. 查看对应的复合事件是否被删除

**验证步骤：**
```sql
-- 先查找一个属于复合事件的轨迹
SELECT
    h.id AS track_id,
    e.event_id AS belongs_to_event,
    COUNT(r2.track_id) AS total_tracks_in_event
FROM app_track h
JOIN app_event_track_relation r ON h.id = r.track_id
JOIN app_composite_event e ON r.event_id = e.id
LEFT JOIN app_event_track_relation r2 ON r2.event_id = r.event_id
GROUP BY h.id, e.event_id
LIMIT 1;

-- 记录上面查询的track_id和event_id
SET @track_to_delete = xxxx;
SET @event_to_check = 'CE_xxxxx';

-- 删除前：检查事件状态
SELECT '删除前' AS status, COUNT(*) FROM app_composite_event WHERE event_id = @event_to_check;
SELECT '删除前关系' AS status, COUNT(*) FROM app_event_track_relation r
JOIN app_composite_event e ON r.event_id = e.id
WHERE e.event_id = @event_to_check;

-- 在前端执行删除轨迹操作...

-- 删除后：检查事件是否被删除
SELECT '删除后' AS status, COUNT(*) FROM app_composite_event WHERE event_id = @event_to_check;
SELECT '删除后关系' AS status, COUNT(*) FROM app_event_track_relation r
JOIN app_composite_event e ON r.event_id = e.id
WHERE e.event_id = @event_to_check;
```

**预期结果：**
- 删除轨迹后，包含该轨迹的所有复合事件都被删除
- 关系表中相关的记录也被清理

---

#### 测试场景6：导出复合事件 ✅

**操作步骤：**
1. 在"复合事件管理"页面，勾选几个事件
2. 点击"导出选中"按钮
3. 下载Excel文件
4. 打开Excel检查数据

**验证步骤：**
- 检查Excel中的轨迹数据是否与页面显示一致
- 检查时间、区域、人员信息是否正确
- 检查标注信息是否包含在导出文件中

**预期结果：**
- Excel中每个事件的轨迹数量与页面显示一致
- 数据完整，无缺失

---

### 第三步：边界情况测试

#### 边界测试1：事件包含大量轨迹
```sql
-- 查找包含最多轨迹的事件
SELECT
    e.event_id,
    COUNT(r.track_id) AS track_count
FROM app_composite_event e
JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id
ORDER BY track_count DESC
LIMIT 1;

-- 在前端查看这个事件，验证是否正常显示
```

#### 边界测试2：一个轨迹属于多个事件（理论上不应该出现）
```sql
-- 检查是否有轨迹属于多个事件
SELECT
    track_id,
    COUNT(DISTINCT event_id) AS event_count,
    GROUP_CONCAT(DISTINCT event_id) AS event_ids
FROM app_event_track_relation
GROUP BY track_id
HAVING event_count > 1;
```

**预期结果：** Empty set（每个轨迹只应该属于一个事件）

#### 边界测试3：事件没有关联轨迹（孤立事件）
```sql
-- 检查是否有没有轨迹的事件
SELECT
    e.id,
    e.event_id,
    e.start_time
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
WHERE r.id IS NULL;
```

**预期结果：** Empty set（所有事件都应该有轨迹）

---

## 快速测试脚本

如果想快速验证关系表工作正常，可以运行以下脚本：

```sql
-- 快速健康检查
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM app_event_track_relation) = 0
        THEN '⚠️ 关系表为空'
        WHEN EXISTS (
            SELECT 1 FROM app_event_track_relation r
            LEFT JOIN app_composite_event e ON r.event_id = e.id
            WHERE e.id IS NULL
        )
        THEN '❌ 存在孤立事件引用'
        WHEN EXISTS (
            SELECT 1 FROM app_event_track_relation r
            LEFT JOIN app_track h ON r.track_id = h.id
            WHERE h.id IS NULL
        )
        THEN '❌ 存在孤立轨迹引用'
        WHEN EXISTS (
            SELECT 1 FROM app_composite_event e
            LEFT JOIN app_event_track_relation r ON e.id = r.event_id
            WHERE r.id IS NULL
        )
        THEN '⚠️ 存在没有轨迹的事件'
        ELSE '✅ 关系表健康'
    END AS health_check;
```

---

## 常见问题排查

### 问题1：关系表为空
**检查：**
```sql
SELECT COUNT(*) FROM app_event_track_relation;
```
**解决：** 运行数据迁移脚本 `sql/migrate_to_relation_table.sql`

### 问题2：前端显示的轨迹与数据库不一致
**检查：**
- 清除浏览器缓存
- 检查是否有JS报错
- 验证API返回的数据：在浏览器控制台查看Network请求

### 问题3：删除轨迹后事件没有被删除
**检查：**
```sql
-- 检查CompositeEventServiceImpl.java中的删除逻辑
-- 应该使用 relationMapper.selectEventIdsByTrackId() 而不是已废弃的方法
```

---

## 测试清单

- [ ] 运行 `sql/test_relation_table.sql` 所有测试通过
- [ ] 新增复合事件能正常创建关系记录
- [ ] 查看复合事件详情显示正确的轨迹列表
- [ ] 标注复合事件能同步更新所有关联轨迹
- [ ] 删除复合事件能清理关系表记录
- [ ] 删除轨迹能删除关联的事件
- [ ] 导出功能能正确导出轨迹数据
- [ ] 快速健康检查返回 "✅ 关系表健康"
- [ ] 无孤立事件引用
- [ ] 无孤立轨迹引用
- [ ] 所有事件都有轨迹关联

---

## 性能测试（可选）

如果数据量较大，可以测试查询性能：

```sql
-- 测试通过事件ID查询轨迹的性能
EXPLAIN SELECT track_id
FROM app_event_track_relation
WHERE event_id = 1
ORDER BY seq_no;
-- 应该使用 idx_event_id 索引

-- 测试通过轨迹ID查询事件的性能
EXPLAIN SELECT event_id
FROM app_event_track_relation
WHERE track_id = 1;
-- 应该使用 idx_track_id 索引
```

**预期结果：** 查询应该使用索引，type应该是 ref 或 const
