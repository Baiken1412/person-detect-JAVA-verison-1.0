-- 为 app_composite_event 表添加报警相关字段
-- 执行时间：2026-01-11
-- 说明：添加轨迹时间过长和事件时间过长报警字段

-- 1. 添加 has_long_track 字段（是否包含轨迹时间过长：0=否，1=是）
SET @dbname = DATABASE();
SET @tablename = 'app_composite_event';
SET @columnname = 'has_long_track';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT DEFAULT 0 COMMENT ''是否包含轨迹时间过长：0=否，1=是'' AFTER has_abnormal_person')
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 添加 has_long_event 字段（是否事件时间过长：0=否，1=是）
SET @columnname = 'has_long_event';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT DEFAULT 0 COMMENT ''是否事件时间过长：0=否，1=是'' AFTER has_long_track')
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 显示确认信息
SELECT
    CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'app_composite_event'
            AND COLUMN_NAME = 'has_long_track'
        )
        THEN '✅ has_long_track 字段已成功添加到 app_composite_event 表'
        ELSE '❌ has_long_track 字段添加失败，请检查数据库权限'
    END AS has_long_track_状态;

SELECT
    CASE
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'app_composite_event'
            AND COLUMN_NAME = 'has_long_event'
        )
        THEN '✅ has_long_event 字段已成功添加到 app_composite_event 表'
        ELSE '❌ has_long_event 字段添加失败，请检查数据库权限'
    END AS has_long_event_状态;
