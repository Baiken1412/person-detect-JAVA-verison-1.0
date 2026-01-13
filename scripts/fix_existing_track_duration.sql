-- =============================================
-- 脚本功能：修复现有轨迹的 track_duration 和 is_long_track 字段
-- 创建时间：2026-01-13
-- 说明：更新所有已存在的轨迹数据，重新计算轨迹时长和是否过长标记
-- =============================================

-- 1. 更新现有数据
-- 计算轨迹时长并判断是否过长（阈值：180秒 = 3分钟）
UPDATE app_track
SET track_duration = TIMESTAMPDIFF(SECOND, pssj, jssj),
    is_long_track = CASE
        WHEN TIMESTAMPDIFF(SECOND, pssj, jssj) > 180 THEN 1
        ELSE 0
    END
WHERE pssj IS NOT NULL AND jssj IS NOT NULL;

-- 2. 将没有结束时间的轨迹设置为默认值
UPDATE app_track
SET track_duration = NULL,
    is_long_track = 0
WHERE pssj IS NULL OR jssj IS NULL;

-- 3. 验证更新结果
SELECT
    COUNT(*) AS total_tracks,
    COUNT(track_duration) AS has_duration,
    SUM(CASE WHEN is_long_track = 1 THEN 1 ELSE 0 END) AS long_tracks,
    AVG(track_duration) AS avg_duration_seconds,
    MIN(track_duration) AS min_duration,
    MAX(track_duration) AS max_duration
FROM app_track;

-- 4. 查看时间过长的轨迹示例
SELECT id, qymc, pssj, jssj, track_duration, is_long_track
FROM app_track
WHERE is_long_track = 1
ORDER BY track_duration DESC
LIMIT 10;

-- 5. 查看更新前后对比（可选）
SELECT
    '更新完成' AS status,
    COUNT(*) AS total,
    COUNT(track_duration) AS with_duration,
    CONCAT(ROUND(COUNT(track_duration) * 100.0 / COUNT(*), 2), '%') AS completion_rate
FROM app_track;
