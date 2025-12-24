-- =============================================
-- 清空数据并重置测试环境
-- 日期：2025-12-24
-- 说明：提供两种清空方案
-- =============================================

-- ========================================
-- 方案1：只清空事件和关系表（保留轨迹数据）
-- 推荐：如果你的轨迹数据是真实数据，选择此方案
-- ========================================

-- 1.1 备份当前数据（可选）
-- CREATE TABLE app_composite_event_backup_20251224 AS SELECT * FROM app_composite_event;
-- CREATE TABLE app_event_track_relation_backup_20251224 AS SELECT * FROM app_event_track_relation;

-- 1.2 查看当前数据量
SELECT '清空前数据统计' AS 统计项;
SELECT 'app_track' AS 表名, COUNT(*) AS 记录数 FROM app_track
UNION ALL
SELECT 'app_composite_event' AS 表名, COUNT(*) AS 记录数 FROM app_composite_event
UNION ALL
SELECT 'app_event_track_relation' AS 表名, COUNT(*) AS 记录数 FROM app_event_track_relation;

-- 1.3 清空事件和关系表（保留轨迹）
TRUNCATE TABLE app_event_track_relation;
TRUNCATE TABLE app_composite_event;

-- 1.4 验证清空结果
SELECT '清空后数据统计' AS 统计项;
SELECT 'app_track' AS 表名, COUNT(*) AS 记录数 FROM app_track
UNION ALL
SELECT 'app_composite_event' AS 表名, COUNT(*) AS 记录数 FROM app_composite_event
UNION ALL
SELECT 'app_event_track_relation' AS 表名, COUNT(*) AS 记录数 FROM app_event_track_relation;

-- 1.5 重置自增ID（可选，从1开始）
ALTER TABLE app_composite_event AUTO_INCREMENT = 1;
ALTER TABLE app_event_track_relation AUTO_INCREMENT = 1;

SELECT '✅ 方案1完成：事件和关系表已清空，轨迹数据保留' AS 状态;
SELECT '下一步：在前端点击"重新同步复合事件"按钮，系统会根据现有轨迹重新生成事件和关系' AS 提示;


-- ========================================
-- 方案2：完全清空所有数据（包括轨迹）
-- 警告：此方案会删除所有轨迹数据！
-- ========================================

-- 2.1 备份所有数据（强烈建议）
-- CREATE TABLE app_track_backup_20251224 AS SELECT * FROM app_track;
-- CREATE TABLE app_composite_event_backup_20251224 AS SELECT * FROM app_composite_event;
-- CREATE TABLE app_event_track_relation_backup_20251224 AS SELECT * FROM app_event_track_relation;

-- 2.2 查看当前数据量
-- SELECT '完全清空前数据统计' AS 统计项;
-- SELECT 'app_track' AS 表名, COUNT(*) AS 记录数 FROM app_track
-- UNION ALL
-- SELECT 'app_composite_event' AS 表名, COUNT(*) AS 记录数 FROM app_composite_event
-- UNION ALL
-- SELECT 'app_event_track_relation' AS 表名, COUNT(*) AS 记录数 FROM app_event_track_relation;

-- 2.3 清空所有表（按依赖顺序）
-- TRUNCATE TABLE app_event_track_relation;
-- TRUNCATE TABLE app_composite_event;
-- TRUNCATE TABLE app_track;

-- 2.4 重置自增ID
-- ALTER TABLE app_track AUTO_INCREMENT = 1;
-- ALTER TABLE app_composite_event AUTO_INCREMENT = 1;
-- ALTER TABLE app_event_track_relation AUTO_INCREMENT = 1;

-- 2.5 验证清空结果
-- SELECT '完全清空后数据统计' AS 统计项;
-- SELECT 'app_track' AS 表名, COUNT(*) AS 记录数 FROM app_track
-- UNION ALL
-- SELECT 'app_composite_event' AS 表名, COUNT(*) AS 记录数 FROM app_composite_event
-- UNION ALL
-- SELECT 'app_event_track_relation' AS 表名, COUNT(*) AS 记录数 FROM app_event_track_relation;

-- SELECT '✅ 方案2完成：所有数据已清空' AS 状态;
-- SELECT '下一步：导入测试轨迹数据，然后点击"重新同步复合事件"' AS 提示;
