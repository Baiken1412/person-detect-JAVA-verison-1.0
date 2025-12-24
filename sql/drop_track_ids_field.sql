-- =============================================
-- 删除不再使用的 track_ids 字段
-- 作者：Claude Code
-- 日期：2025-12-24
-- 说明：现在使用关系表 app_event_track_relation，不再需要 track_ids 字段
-- =============================================

-- 验证：确认关系表数据完整
SELECT
    '数据验证' AS step,
    COUNT(*) AS total_events,
    (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation) AS events_in_relation,
    CASE
        WHEN COUNT(*) = (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation)
        THEN '✅ 可以安全删除 track_ids 字段'
        ELSE '❌ 警告：数据不一致，暂时不要删除'
    END AS recommendation
FROM app_composite_event;

-- 如果上面显示 "✅ 可以安全删除"，执行下面的语句

-- 删除 track_ids 字段
ALTER TABLE `app_composite_event` DROP COLUMN `track_ids`;

-- 验证删除成功
SHOW COLUMNS FROM `app_composite_event` LIKE 'track_ids';
-- 预期结果：Empty set (0.00 sec) - 表示字段已删除

-- =============================================
-- 清理说明：
-- 1. track_ids字段已从数据库中删除
-- 2. Java代码中的trackIds字段保留（仅在内存中使用）
-- 3. Mapper XML已更新，不再持久化此字段
-- 4. 所有查询现在通过关系表 app_event_track_relation 进行
-- =============================================
