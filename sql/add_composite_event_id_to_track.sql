-- =============================================
-- 为轨迹表添加复合事件ID字段
-- 作者：Claude Code
-- 日期：2025-12-24
-- 说明：建立轨迹与复合事件的双向关联
-- =============================================

-- 1. 添加复合事件ID字段
ALTER TABLE `app_track`
ADD COLUMN `composite_event_id` BIGINT DEFAULT NULL COMMENT '所属复合事件ID' AFTER `rysl`;

-- 2. 添加索引，提高查询效率
ALTER TABLE `app_track`
ADD INDEX `idx_composite_event` (`composite_event_id`);

-- 3. 添加外键约束（可选，建议先跳过，等数据稳定后再添加）
-- 如果需要外键约束，取消下面的注释：
/*
ALTER TABLE `app_track`
ADD CONSTRAINT `fk_track_composite_event`
FOREIGN KEY (`composite_event_id`) REFERENCES `app_composite_event` (`id`)
ON DELETE SET NULL
ON UPDATE CASCADE;
*/

-- =============================================
-- 使用说明：
-- 1. 执行步骤1-2，添加字段和索引
-- 2. 执行步骤3（可选），添加外键约束
-- 3. 重启应用后，点击"重新同步复合事件"按钮
-- 4. 新的同步逻辑会自动填充composite_event_id
-- =============================================
