-- =============================================
-- 清理旧设计的字段（可选）
-- 作者：Claude Code
-- 日期：2025-12-24
-- 说明：验证关系表数据正确后，删除不再使用的track_ids字段
-- 警告：执行前请务必备份数据！
-- =============================================

-- 验证：确认关系表数据完整
SELECT
    '数据验证' AS step,
    COUNT(*) AS total_events,
    SUM(CASE WHEN track_ids IS NOT NULL AND track_ids != '' THEN 1 ELSE 0 END) AS events_with_tracks,
    (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation) AS events_in_relation
FROM app_composite_event;

-- 如果上面的验证结果显示：events_with_tracks = events_in_relation
-- 说明迁移成功，可以安全删除track_ids字段

-- 删除track_ids字段（谨慎操作！）
-- ALTER TABLE `app_composite_event` DROP COLUMN `track_ids`;

-- =============================================
-- 使用说明：
-- 1. 首先运行SELECT验证语句，检查数据是否完整
-- 2. 如果验证通过，取消ALTER TABLE的注释并执行
-- 3. 建议保留track_ids字段一段时间（比如1-2周），确认无问题后再删除
-- =============================================
