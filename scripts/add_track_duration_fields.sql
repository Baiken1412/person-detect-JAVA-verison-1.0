-- =============================================
-- 脚本功能：为 app_track 表添加轨迹时长相关字段
-- 创建时间：2026-01-12
-- 说明：添加 track_duration（轨迹时长，秒）和 is_long_track（是否时间过长）字段
-- =============================================

-- 1. 添加字段
ALTER TABLE app_track
ADD COLUMN track_duration INT DEFAULT NULL COMMENT '轨迹时长（秒）',
ADD COLUMN is_long_track TINYINT DEFAULT 0 COMMENT '是否时间过长：0=否，1=是';

-- 2. 更新现有数据
-- 计算轨迹时长并判断是否过长（阈值：180秒 = 3分钟）
UPDATE app_track
SET track_duration = TIMESTAMPDIFF(SECOND, pssj, jssj),
    is_long_track = CASE
        WHEN TIMESTAMPDIFF(SECOND, pssj, jssj) > 180 THEN 1
        ELSE 0
    END
WHERE pssj IS NOT NULL AND jssj IS NOT NULL;

-- 3. 验证更新结果
SELECT
    COUNT(*) AS total_tracks,
    COUNT(track_duration) AS has_duration,
    SUM(CASE WHEN is_long_track = 1 THEN 1 ELSE 0 END) AS long_tracks,
    AVG(track_duration) AS avg_duration_seconds
FROM app_track;

-- 4. 查看时间过长的轨迹示例
SELECT id, qymc, pssj, jssj, track_duration, is_long_track
FROM app_track
WHERE is_long_track = 1
ORDER BY track_duration DESC
LIMIT 10;
