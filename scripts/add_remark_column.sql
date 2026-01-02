-- 为 app_track 表添加 remark（备注）字段
-- 执行时间：2026-01-02
-- 说明：添加备注字段用于存储轨迹标注时的备注信息

-- 检查字段是否已存在，如果不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'app_track';
SET @columnname = 'remark';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(500) DEFAULT NULL COMMENT ''备注'' AFTER wlry')
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
            AND TABLE_NAME = 'app_track'
            AND COLUMN_NAME = 'remark'
        )
        THEN '✅ remark 字段已成功添加到 app_track 表'
        ELSE '❌ remark 字段添加失败，请检查数据库权限'
    END AS 执行结果;
