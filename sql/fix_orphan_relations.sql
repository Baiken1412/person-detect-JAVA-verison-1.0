-- =============================================
-- 修复孤立关系记录
-- 日期：2025-12-24
-- 问题：关系表中存在孤立的event_id（事件已删除但关系未删除）
-- =============================================

-- 步骤1：查看孤立事件的详细信息
SELECT
    '孤立事件诊断' AS 诊断项,
    COUNT(DISTINCT r.event_id) AS 孤立事件数量,
    COUNT(*) AS 孤立关系记录数
FROM app_event_track_relation r
LEFT JOIN app_composite_event e ON r.event_id = e.id
WHERE e.id IS NULL;

-- 查看具体的孤立事件ID
SELECT
    '孤立的事件ID列表' AS 说明,
    GROUP_CONCAT(DISTINCT r.event_id ORDER BY r.event_id) AS 孤立事件IDs
FROM app_event_track_relation r
LEFT JOIN app_composite_event e ON r.event_id = e.id
WHERE e.id IS NULL;

-- 步骤2：备份孤立数据（可选，用于审计）
-- CREATE TABLE IF NOT EXISTS app_event_track_relation_backup_20251224 AS
-- SELECT r.*
-- FROM app_event_track_relation r
-- LEFT JOIN app_composite_event e ON r.event_id = e.id
-- WHERE e.id IS NULL;

-- 步骤3：删除孤立的关系记录
DELETE r
FROM app_event_track_relation r
LEFT JOIN app_composite_event e ON r.event_id = e.id
WHERE e.id IS NULL;

-- 显示删除结果
SELECT
    '清理结果' AS 状态,
    ROW_COUNT() AS 已删除的孤立记录数;

-- 步骤4：验证清理后的状态
SELECT
    '清理后验证' AS 验证项,
    COUNT(*) AS 剩余关系记录数,
    COUNT(DISTINCT event_id) AS 关联的事件数,
    COUNT(DISTINCT track_id) AS 关联的轨迹数
FROM app_event_track_relation;

-- 步骤5：重新运行健康检查
SELECT
    '最终健康检查' AS 检查项,
    CASE
        WHEN (SELECT COUNT(*) FROM app_event_track_relation) = 0
        THEN '⚠️ 关系表为空'
        WHEN EXISTS (
            SELECT 1 FROM app_event_track_relation r
            LEFT JOIN app_composite_event e ON r.event_id = e.id
            WHERE e.id IS NULL
        )
        THEN '❌ 仍存在孤立事件'
        WHEN EXISTS (
            SELECT 1 FROM app_event_track_relation r
            LEFT JOIN app_track h ON r.track_id = h.id
            WHERE h.id IS NULL
        )
        THEN '❌ 存在孤立轨迹'
        WHEN EXISTS (
            SELECT 1 FROM app_composite_event e
            LEFT JOIN app_event_track_relation r ON e.id = r.event_id
            WHERE r.id IS NULL
        )
        THEN '⚠️ 存在没有轨迹的事件'
        ELSE '✅ 关系表健康'
    END AS 健康状态;

-- 步骤6：检查是否有事件缺少轨迹关联
SELECT
    '无轨迹事件检查' AS 检查项,
    COUNT(*) AS 数量
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
WHERE r.id IS NULL;

-- 如果有无轨迹的事件，显示详情
SELECT
    '无轨迹的事件列表' AS 说明,
    e.id,
    e.event_id,
    e.start_time,
    e.qymc
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
WHERE r.id IS NULL
LIMIT 10;
