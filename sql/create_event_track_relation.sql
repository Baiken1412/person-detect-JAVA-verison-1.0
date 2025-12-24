-- =============================================
-- 创建复合事件与轨迹关系表
-- 作者：Claude Code
-- 日期：2025-12-24
-- 说明：使用关系表替代track_ids字段，符合数据库设计规范
-- =============================================

-- 1. 创建关系表
CREATE TABLE IF NOT EXISTS `app_event_track_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `event_id` BIGINT NOT NULL COMMENT '复合事件ID，关联app_composite_event.id',
  `track_id` BIGINT NOT NULL COMMENT '轨迹ID，关联app_track.id',
  `seq_no` INT DEFAULT 0 COMMENT '轨迹在事件中的顺序号（从1开始）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关联建立时间',
  PRIMARY KEY (`id`),
  KEY `idx_event_id` (`event_id`),
  KEY `idx_track_id` (`track_id`),
  UNIQUE KEY `uk_event_track` (`event_id`, `track_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复合事件与轨迹关系表';

-- 2. 添加外键约束（可选，建议先跳过，数据稳定后再添加）
/*
ALTER TABLE `app_event_track_relation`
ADD CONSTRAINT `fk_relation_event`
  FOREIGN KEY (`event_id`) REFERENCES `app_composite_event` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `app_event_track_relation`
ADD CONSTRAINT `fk_relation_track`
  FOREIGN KEY (`track_id`) REFERENCES `app_track` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
*/

-- =============================================
-- 使用说明：
-- 1. 执行步骤1，创建关系表
-- 2. 执行数据迁移脚本（migrate_to_relation_table.sql）
-- 3. 验证数据迁移正确后，删除track_ids字段（见cleanup_old_design.sql）
-- 4. 如需外键约束，取消步骤2的注释
-- =============================================
