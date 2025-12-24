-- =============================================
-- 数据迁移脚本：从track_ids迁移到关系表
-- 作者：Claude Code
-- 日期：2025-12-24
-- 说明：将app_composite_event.track_ids的数据迁移到app_event_track_relation表
-- =============================================

-- 清空关系表（如果之前有测试数据）
TRUNCATE TABLE `app_event_track_relation`;

-- 迁移数据（使用存储过程处理逗号分隔字符串）
DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_event_track_data$$

CREATE PROCEDURE migrate_event_track_data()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_event_id BIGINT;
    DECLARE v_track_ids TEXT;
    DECLARE v_track_id VARCHAR(20);
    DECLARE v_seq_no INT;
    DECLARE v_pos INT;
    DECLARE v_remaining TEXT;

    -- 游标：遍历所有复合事件
    DECLARE event_cursor CURSOR FOR
        SELECT id, track_ids
        FROM app_composite_event
        WHERE track_ids IS NOT NULL AND track_ids != '';

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN event_cursor;

    read_loop: LOOP
        FETCH event_cursor INTO v_event_id, v_track_ids;

        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 处理逗号分隔的track_ids
        SET v_remaining = v_track_ids;
        SET v_seq_no = 1;

        WHILE LENGTH(v_remaining) > 0 DO
            -- 查找逗号位置
            SET v_pos = LOCATE(',', v_remaining);

            IF v_pos > 0 THEN
                -- 提取当前track_id
                SET v_track_id = TRIM(SUBSTRING(v_remaining, 1, v_pos - 1));
                SET v_remaining = SUBSTRING(v_remaining, v_pos + 1);
            ELSE
                -- 最后一个track_id
                SET v_track_id = TRIM(v_remaining);
                SET v_remaining = '';
            END IF;

            -- 插入关系表
            IF LENGTH(v_track_id) > 0 THEN
                INSERT INTO app_event_track_relation (event_id, track_id, seq_no)
                VALUES (v_event_id, CAST(v_track_id AS UNSIGNED), v_seq_no)
                ON DUPLICATE KEY UPDATE seq_no = v_seq_no;

                SET v_seq_no = v_seq_no + 1;
            END IF;
        END WHILE;
    END LOOP;

    CLOSE event_cursor;

    -- 输出迁移结果
    SELECT COUNT(*) AS '迁移的关联记录数' FROM app_event_track_relation;
    SELECT COUNT(*) AS '迁移的事件数' FROM app_composite_event WHERE track_ids IS NOT NULL AND track_ids != '';

END$$

DELIMITER ;

-- 执行迁移
CALL migrate_event_track_data();

-- 验证迁移结果
SELECT
    e.id AS event_id,
    e.event_id AS event_business_id,
    COUNT(r.track_id) AS track_count_in_relation,
    e.track_count AS track_count_in_event,
    e.track_ids
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id
ORDER BY e.id DESC
LIMIT 10;

-- =============================================
-- 使用说明：
-- 1. 确保已创建关系表（create_event_track_relation.sql）
-- 2. 执行此脚本进行数据迁移
-- 3. 检查验证结果，确认track_count_in_relation = track_count_in_event
-- 4. 如果正确，继续执行cleanup_old_design.sql清理旧字段
-- =============================================
