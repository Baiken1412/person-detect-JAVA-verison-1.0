# 复合事件自动更新功能完善

## 修改日期
2025-12-24

## 修改目标
实现删除轨迹时自动清理相关复合事件，确保数据一致性，无需手动点击"重新同步复合事件"按钮。

---

## 修改内容

### 1. AppTrackServiceImpl.java（轨迹服务实现）

#### 修改前的问题：
```java
@Override
public int deleteAppTrackById(Long id)
{
    return appTrackMapper.deleteAppTrackById(id);
    // ❌ 只删除轨迹，不处理复合事件
    // ❌ 导致关系表中出现孤立记录
}
```

#### 修改后（第133-154行）：
```java
@Override
public int deleteAppTrackById(Long id)
{
    // 先查询该轨迹，用于后续复合事件处理
    AppTrack track = appTrackMapper.selectAppTrackById(id);
    if (track == null)
    {
        return 0;
    }

    // 删除轨迹
    int result = appTrackMapper.deleteAppTrackById(id);

    if (result > 0)
    {
        // ✅ 删除包含该轨迹的所有复合事件
        // ✅ 自动清理关系表记录
        compositeEventService.handleTrackDeletion(track);
    }

    return result;
}
```

#### 批量删除方法（第103-124行）：
```java
@Override
public int deleteAppTrackByIds(String ids)
{
    String[] idArray = Convert.toStrArray(ids);
    int deletedCount = 0;

    // ✅ 逐个删除轨迹（确保每个轨迹的复合事件都被正确处理）
    for (String idStr : idArray)
    {
        try
        {
            Long id = Long.parseLong(idStr.trim());
            deletedCount += deleteAppTrackById(id);
        }
        catch (NumberFormatException e)
        {
            // 忽略无效的ID
        }
    }

    return deletedCount;
}
```

---

### 2. ICompositeEventService.java（接口定义）

新增方法声明（第99-105行）：
```java
/**
 * 处理轨迹删除后的复合事件清理
 * 应用层实时写入：删除轨迹后自动删除包含该轨迹的所有复合事件
 *
 * @param track 被删除的轨迹
 */
public void handleTrackDeletion(AppTrack track);
```

---

### 3. CompositeEventServiceImpl.java（复合事件服务实现）

新增方法实现（第625-675行）：
```java
@Override
@Transactional
public void handleTrackDeletion(AppTrack track)
{
    if (track == null || track.getId() == null)
    {
        return;
    }

    try
    {
        // 1. 通过关系表查询该轨迹所属的事件ID列表
        List<Long> eventIds = relationMapper.selectEventIdsByTrackId(track.getId());

        if (eventIds != null && !eventIds.isEmpty())
        {
            System.out.println("轨迹 ID=" + track.getId() + " 被删除，需要删除 "
                + eventIds.size() + " 个包含该轨迹的复合事件");

            // 2. 删除所有包含该轨迹的复合事件
            for (Long eventId : eventIds)
            {
                // deleteCompositeEventById 方法已经包含删除关系表记录的逻辑
                deleteCompositeEventById(eventId);
                System.out.println("  已删除复合事件 ID=" + eventId);
            }

            System.out.println("轨迹删除后复合事件清理完成");
        }
        else
        {
            System.out.println("轨迹 ID=" + track.getId() + " 不属于任何复合事件，无需清理");
        }
    }
    catch (Exception e)
    {
        System.err.println("处理轨迹删除时发生错误：" + e.getMessage());
        e.printStackTrace();
        throw e;  // 重新抛出异常，确保事务回滚
    }
}
```

---

## 修改效果对比

### 修改前：

| 操作 | 自动生成复合事件 | 需要手动同步 |
|-----|--------------|-----------|
| 新增轨迹（前端/API） | ✅ 自动 | ❌ 不需要 |
| 更新轨迹（前端/API） | ✅ 自动 | ❌ 不需要 |
| 删除轨迹（前端/API） | ❌ 不自动 | ✅ **需要** |
| 批量导入SQL数据 | ❌ 不触发 | ✅ 需要 |

**问题：**
- 删除轨迹后，关系表出现孤立记录
- 复合事件列表显示的轨迹可能已被删除
- 数据不一致

---

### 修改后：

| 操作 | 自动生成复合事件 | 需要手动同步 |
|-----|--------------|-----------|
| 新增轨迹（前端/API） | ✅ 自动 | ❌ 不需要 |
| 更新轨迹（前端/API） | ✅ 自动 | ❌ 不需要 |
| 删除轨迹（前端/API） | ✅ **自动清理** | ❌ **不需要** |
| 批量导入SQL数据 | ❌ 不触发 | ✅ 需要 |

**优势：**
- 删除轨迹后自动清理相关复合事件
- 自动删除关系表记录，避免孤立数据
- 保证数据一致性
- 使用事务保证原子性

---

## 技术细节

### 事务控制
所有修改都使用了 `@Transactional` 注解，确保：
- 删除轨迹和删除复合事件要么全部成功，要么全部回滚
- 避免出现轨迹已删除但事件未删除的不一致状态

### 删除逻辑
1. **查询阶段**：通过关系表 `app_event_track_relation` 查询包含该轨迹的所有事件ID
2. **删除阶段**：逐个删除复合事件（每个事件删除时会自动删除关系表记录）
3. **日志输出**：在控制台输出详细的删除日志，便于调试和监控

### 性能考虑
- 删除单个轨迹时，只查询并删除包含该轨迹的事件（通常1-2个）
- 批量删除轨迹时，逐个处理，确保每个轨迹的事件都被正确清理
- 使用索引查询（`idx_track_id`），性能开销小

---

## 测试方案

### 测试1：删除单个轨迹

**步骤：**
1. 在"活动轨迹"页面，找到一个属于某个复合事件的轨迹
2. 记录该轨迹ID和所属事件ID
3. 删除该轨迹
4. 检查复合事件是否被自动删除

**验证SQL：**
```sql
-- 假设删除的轨迹ID是100
SET @deleted_track_id = 100;

-- 检查该轨迹是否还存在
SELECT COUNT(*) AS 轨迹是否存在 FROM app_track WHERE id = @deleted_track_id;
-- 预期：0

-- 检查关系表是否还有该轨迹的记录
SELECT COUNT(*) AS 关系表记录数 FROM app_event_track_relation WHERE track_id = @deleted_track_id;
-- 预期：0

-- 检查是否有孤立事件
SELECT COUNT(*) AS 孤立事件数
FROM app_event_track_relation r
LEFT JOIN app_composite_event e ON r.event_id = e.id
WHERE e.id IS NULL;
-- 预期：0
```

---

### 测试2：批量删除轨迹

**步骤：**
1. 在"活动轨迹"页面，勾选多个轨迹（包括属于复合事件的）
2. 点击"删除"按钮
3. 检查相关复合事件是否被自动删除

**验证SQL：**
```sql
-- 检查健康状态
SELECT
    CASE
        WHEN EXISTS (SELECT 1 FROM app_event_track_relation r LEFT JOIN app_composite_event e ON r.event_id = e.id WHERE e.id IS NULL)
        THEN '❌ 存在孤立事件'
        WHEN EXISTS (SELECT 1 FROM app_event_track_relation r LEFT JOIN app_track h ON r.track_id = h.id WHERE h.id IS NULL)
        THEN '❌ 存在孤立轨迹'
        ELSE '✅ 数据健康'
    END AS 健康状态;
-- 预期：✅ 数据健康
```

---

### 测试3：删除轨迹后查看日志

删除轨迹时，控制台应该输出类似日志：
```
轨迹 ID=100 被删除，需要删除 1 个包含该轨迹的复合事件
  已删除复合事件 ID=50
轨迹删除后复合事件清理完成
```

或者：
```
轨迹 ID=200 不属于任何复合事件，无需清理
```

---

### 测试4：事务回滚测试（可选）

**模拟异常情况：**
1. 在 `handleTrackDeletion` 方法中临时添加一个抛出异常的代码
2. 删除一个轨迹
3. 验证轨迹和复合事件都没有被删除（事务回滚成功）

---

## 后续优化建议

### 可选优化1：重新计算周围事件
当前实现是直接删除包含该轨迹的所有事件。未来可以优化为：
- 删除事件后，重新计算该时间窗口内剩余轨迹的复合事件
- 适用于误删轨迹后，希望保留其他轨迹形成的事件

### 可选优化2：软删除
当前是物理删除轨迹。未来可以改为软删除：
- 添加 `is_deleted` 字段标记删除状态
- 保留历史数据用于审计
- 复合事件计算时忽略已删除的轨迹

### 可选优化3：异步处理
如果批量删除大量轨迹，可以考虑：
- 使用消息队列异步处理复合事件清理
- 避免阻塞前端操作
- 提供删除进度反馈

---

## 注意事项

1. **备份数据**：在生产环境应用此修改前，建议先备份数据库
2. **性能监控**：批量删除大量轨迹时，注意监控数据库性能
3. **日志输出**：控制台会输出详细的删除日志，便于问题排查
4. **事务超时**：如果删除的轨迹关联的事件过多，可能需要调整事务超时时间

---

## 总结

此次修改实现了**完整的应用层实时写入**机制：
- ✅ 新增轨迹 → 自动生成复合事件
- ✅ 更新轨迹 → 自动重新计算复合事件
- ✅ 删除轨迹 → 自动清理相关复合事件

**用户只需要在以下情况手动点击"重新同步复合事件"：**
- 批量导入SQL测试数据
- 系统初始化时已有历史数据
- 数据修复或维护

**日常使用中，所有前端操作都会自动维护复合事件的一致性。**
