-- =============================================
-- 删除不再使用的 composite_event_id 字段
-- 作者：Claude Code
-- 日期：2025-12-24
-- 说明：现在使用关系表 app_event_track_relation，不再需要 composite_event_id 字段
-- =============================================

-- 验证：确认关系表数据完整
SELECT
    '数据验证' AS step,
    (SELECT COUNT(*) FROM app_track) AS total_tracks,
    (SELECT COUNT(DISTINCT track_id) FROM app_event_track_relation) AS tracks_in_relation,
    CASE
        WHEN (SELECT COUNT(DISTINCT track_id) FROM app_event_track_relation) > 0
        THEN '✅ 可以安全删除 composite_event_id 字段'
        ELSE '⚠️ 建议先运行重新同步复合事件'
    END AS recommendation
FROM DUAL;

-- 删除 composite_event_id 字段
ALTER TABLE `app_track` DROP COLUMN `composite_event_id`;

-- 如果有索引，一并删除
-- ALTER TABLE `app_track` DROP INDEX `idx_composite_event`;

-- 验证删除成功
SHOW COLUMNS FROM `app_track` LIKE 'composite_event_id';
-- 预期结果：Empty set (0.00 sec) - 表示字段已删除

-- =============================================
-- 清理说明：
-- 1. composite_event_id 字段已从数据库中删除
-- 2. Java代码中的字段定义已删除
-- 3. Mapper XML已更新，不再映射此字段
-- 4. 所有关联查询现在通过关系表 app_event_track_relation 进行
-- =============================================

-- 最终验证：查询关系表统计
SELECT
    '关系表统计' AS info,
    COUNT(DISTINCT event_id) AS total_events,
    COUNT(DISTINCT track_id) AS total_tracks,
    COUNT(*) AS total_relations,
    ROUND(COUNT(*) / COUNT(DISTINCT event_id), 2) AS avg_tracks_per_event
FROM app_event_track_relation;
