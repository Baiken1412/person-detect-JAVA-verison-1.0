-- ============================================
-- 查询 app_roomip 表的字段，了解摄像头配置
-- ============================================

-- 查看所有摄像头的 fjmc（摄像头名称）和 gnslx（区域名称）
SELECT
    id AS '摄像头ID',
    fjmc AS '摄像头名称(fjmc)',
    gnslx AS '区域名称(gnslx)',
    ip AS 'IP地址',
    dk AS '端口',
    tdh AS '通道号'
FROM app_roomip
ORDER BY id;

-- 查看轨迹表中实际使用的区域名称
SELECT
    DISTINCT qymc AS '轨迹表中的区域名称(qymc)',
    COUNT(*) AS '轨迹数量'
FROM app_track
WHERE qymc IS NOT NULL
GROUP BY qymc
ORDER BY COUNT(*) DESC;

-- 检查哪些摄像头的 gnslx 与轨迹表的 qymc 匹配
SELECT
    r.id AS '摄像头ID',
    r.fjmc AS '摄像头名称',
    r.gnslx AS 'app_roomip.gnslx',
    COUNT(DISTINCT h.id) AS '匹配的轨迹数量'
FROM app_roomip r
LEFT JOIN app_track h ON r.gnslx = h.qymc
GROUP BY r.id, r.fjmc, r.gnslx
ORDER BY r.id;

-- 查看不匹配的情况（摄像头的gnslx在轨迹表中找不到）
SELECT
    r.id AS '摄像头ID',
    r.fjmc AS '摄像头名称',
    r.gnslx AS 'app_roomip.gnslx (找不到对应轨迹)'
FROM app_roomip r
WHERE r.gnslx NOT IN (SELECT DISTINCT qymc FROM app_track WHERE qymc IS NOT NULL)
   OR r.gnslx IS NULL;
