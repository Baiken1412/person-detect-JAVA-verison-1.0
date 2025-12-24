-- =============================================
-- 数据迁移验证脚本
-- 验证关系表数据是否完整、正确
-- =============================================

-- 1. 验证迁移数据总量
SELECT
    '1. 数据量验证' AS test_name,
    (SELECT COUNT(*) FROM app_composite_event) AS total_events,
    (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation) AS events_in_relation,
    (SELECT COUNT(*) FROM app_event_track_relation) AS total_relations,
    CASE
        WHEN (SELECT COUNT(*) FROM app_composite_event) = (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation)
        THEN '✅ PASS'
        ELSE '❌ FAIL'
    END AS status;

-- 2. 验证每个事件的轨迹数量是否一致
SELECT
    '2. 轨迹数量一致性验证' AS test_name,
    COUNT(*) AS total_events,
    SUM(CASE WHEN track_count = actual_count THEN 1 ELSE 0 END) AS matched_events,
    SUM(CASE WHEN track_count != actual_count THEN 1 ELSE 0 END) AS mismatched_events,
    CASE
        WHEN SUM(CASE WHEN track_count != actual_count THEN 1 ELSE 0 END) = 0
        THEN '✅ PASS'
        ELSE '❌ FAIL'
    END AS status
FROM (
    SELECT
        e.id,
        e.track_count,
        COUNT(r.track_id) AS actual_count
    FROM app_composite_event e
    LEFT JOIN app_event_track_relation r ON e.id = r.event_id
    GROUP BY e.id
) AS counts;

-- 3. 查找不匹配的事件（如果有）
SELECT
    '3. 不匹配事件详情' AS test_name,
    e.id AS event_id,
    e.event_id AS event_business_id,
    e.track_count AS expected_count,
    COUNT(r.track_id) AS actual_count,
    e.track_ids AS old_track_ids
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id
HAVING e.track_count != COUNT(r.track_id)
LIMIT 10;

-- 4. 验证顺序号是否连续
SELECT
    '4. 顺序号连续性验证' AS test_name,
    COUNT(*) AS events_checked,
    SUM(CASE WHEN is_continuous THEN 1 ELSE 0 END) AS continuous_events,
    SUM(CASE WHEN NOT is_continuous THEN 1 ELSE 0 END) AS discontinuous_events,
    CASE
        WHEN SUM(CASE WHEN NOT is_continuous THEN 1 ELSE 0 END) = 0
        THEN '✅ PASS'
        ELSE '⚠️ WARNING'
    END AS status
FROM (
    SELECT
        event_id,
        COUNT(*) = MAX(seq_no) AND MIN(seq_no) = 1 AS is_continuous
    FROM app_event_track_relation
    GROUP BY event_id
) AS seq_check;

-- 5. 验证轨迹ID是否都有效（存在于app_track表）
SELECT
    '5. 轨迹ID有效性验证' AS test_name,
    COUNT(DISTINCT r.track_id) AS total_track_ids,
    COUNT(DISTINCT h.id) AS valid_track_ids,
    COUNT(DISTINCT r.track_id) - COUNT(DISTINCT h.id) AS invalid_track_ids,
    CASE
        WHEN COUNT(DISTINCT r.track_id) = COUNT(DISTINCT h.id)
        THEN '✅ PASS'
        ELSE '❌ FAIL'
    END AS status
FROM app_event_track_relation r
LEFT JOIN app_track h ON r.track_id = h.id;

-- 6. 显示一些样本数据
SELECT
    '6. 样本数据展示' AS test_name,
    e.id AS event_id,
    e.event_id AS event_business_id,
    COUNT(r.track_id) AS track_count,
    GROUP_CONCAT(r.track_id ORDER BY r.seq_no) AS track_ids_from_relation,
    e.track_ids AS track_ids_from_old_field
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id
ORDER BY e.id DESC
LIMIT 5;

-- 7. 统计汇总
SELECT
    '7. 统计汇总' AS summary,
    (SELECT COUNT(*) FROM app_composite_event) AS '复合事件总数',
    (SELECT COUNT(*) FROM app_event_track_relation) AS '关系记录总数',
    (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation) AS '关联的事件数',
    (SELECT AVG(track_count) FROM app_composite_event) AS '平均轨迹数',
    (SELECT MAX(track_count) FROM app_composite_event) AS '最大轨迹数',
    (SELECT MIN(track_count) FROM app_composite_event) AS '最小轨迹数';

-- =============================================
-- 预期结果：
-- - 所有验证项都应该显示 ✅ PASS
-- - 不匹配事件详情应该返回空结果
-- - 样本数据中，track_ids_from_relation 应该与 track_ids_from_old_field 一致
-- =============================================
