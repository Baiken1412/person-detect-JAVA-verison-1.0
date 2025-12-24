-- =============================================
-- 检查视频路径配置
-- 用于排查视频播放问题
-- =============================================

-- 1. 查看复合事件ID=1的所有关联轨迹的视频路径
SELECT
    h.id AS '轨迹ID',
    h.pssj AS '时间',
    h.qymc AS '区域',
    h.spdz AS '视频路径',
    CASE
        WHEN h.spdz IS NULL THEN '❌ 路径为空'
        WHEN h.spdz = '' THEN '❌ 路径为空字符串'
        WHEN h.spdz NOT LIKE '%.mp4%' AND h.spdz NOT LIKE '%.avi%' THEN '⚠️ 非视频文件格式'
        ELSE '✓ 路径已设置'
    END AS '路径状态',
    LENGTH(h.spdz) AS '路径长度'
FROM app_track h
INNER JOIN event_track_relation r ON h.id = r.track_id
INNER JOIN composite_event c ON r.event_id = c.id
WHERE c.event_id = 1  -- 修改这里的事件ID
ORDER BY r.seq_no;

-- 2. 统计所有轨迹的视频路径情况
SELECT
    '总轨迹数' AS '统计项',
    COUNT(*) AS '数量'
FROM app_track
UNION ALL
SELECT
    '有视频路径的轨迹',
    COUNT(*)
FROM app_track
WHERE spdz IS NOT NULL AND spdz != ''
UNION ALL
SELECT
    '无视频路径的轨迹',
    COUNT(*)
FROM app_track
WHERE spdz IS NULL OR spdz = '';

-- 3. 查看最近的几条轨迹的视频路径（示例）
SELECT
    id AS '轨迹ID',
    pssj AS '时间',
    qymc AS '区域',
    COALESCE(spdz, '(空)') AS '视频路径',
    spsc AS '时长(秒)'
FROM app_track
ORDER BY pssj DESC
LIMIT 10;

-- 4. 查看测试数据中的视频路径格式（如果有测试数据）
SELECT DISTINCT
    LEFT(spdz, 20) AS '视频路径前缀',
    COUNT(*) AS '数量'
FROM app_track
WHERE spdz IS NOT NULL AND spdz != ''
GROUP BY LEFT(spdz, 20)
ORDER BY COUNT(*) DESC
LIMIT 10;
