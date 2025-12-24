-- =============================================
-- 事件轨迹关系表功能测试脚本
-- 日期：2025-12-24
-- 说明：测试 app_event_track_relation 表的增删改功能
-- =============================================

-- ========================================
-- 测试1：查看当前关系表数据
-- ========================================
SELECT
    '当前关系表数据' AS test_name,
    COUNT(*) AS total_relations,
    COUNT(DISTINCT event_id) AS total_events,
    COUNT(DISTINCT track_id) AS total_tracks,
    MIN(created_at) AS earliest_relation,
    MAX(created_at) AS latest_relation
FROM app_event_track_relation;

-- 查看最近创建的5个事件及其轨迹关联
SELECT
    '最近5个事件的轨迹关联' AS info,
    r.event_id,
    r.track_id,
    r.seq_no,
    r.created_at,
    e.event_id AS composite_event_id,
    e.start_time,
    e.qymc AS event_area,
    h.qymc AS track_area,
    h.pssj AS track_time
FROM app_event_track_relation r
LEFT JOIN app_composite_event e ON r.event_id = e.id
LEFT JOIN app_track h ON r.track_id = h.id
ORDER BY r.created_at DESC
LIMIT 5;

-- ========================================
-- 测试2：验证关系表数据完整性
-- ========================================

-- 2.1 检查是否有孤立的事件ID（关系表中的event_id在事件表中不存在）
SELECT
    '孤立事件检查' AS test_name,
    COUNT(DISTINCT r.event_id) AS orphan_event_count
FROM app_event_track_relation r
LEFT JOIN app_composite_event e ON r.event_id = e.id
WHERE e.id IS NULL;
-- 预期结果：0（不应该有孤立事件）

-- 2.2 检查是否有孤立的轨迹ID（关系表中的track_id在轨迹表中不存在）
SELECT
    '孤立轨迹检查' AS test_name,
    COUNT(DISTINCT r.track_id) AS orphan_track_count
FROM app_event_track_relation r
LEFT JOIN app_track h ON r.track_id = h.id
WHERE h.id IS NULL;
-- 预期结果：0（不应该有孤立轨迹）

-- 2.3 检查每个事件的轨迹数量
SELECT
    '事件轨迹数量分布' AS test_name,
    track_count,
    COUNT(*) AS event_count
FROM (
    SELECT
        event_id,
        COUNT(*) AS track_count
    FROM app_event_track_relation
    GROUP BY event_id
) AS counts
GROUP BY track_count
ORDER BY track_count;
-- 预期结果：应该看到各种轨迹数量的分布（1个轨迹、2个轨迹、3个轨迹...）

-- ========================================
-- 测试3：验证序号（seq_no）的连续性
-- ========================================
SELECT
    '序号连续性检查' AS test_name,
    r.event_id,
    GROUP_CONCAT(r.seq_no ORDER BY r.seq_no) AS seq_numbers,
    COUNT(*) AS track_count,
    CASE
        WHEN COUNT(*) = MAX(r.seq_no) THEN '✅ 序号连续'
        ELSE '⚠️ 序号不连续'
    END AS status
FROM app_event_track_relation r
GROUP BY r.event_id
HAVING status = '⚠️ 序号不连续'
LIMIT 10;
-- 预期结果：Empty set（所有事件的序号都应该连续）

-- ========================================
-- 测试4：对比新旧数据是否一致（如果还保留了track_ids字段）
-- ========================================
-- 注意：如果已经删除了track_ids字段，跳过此测试

-- SELECT
--     '新旧数据对比' AS test_name,
--     e.id AS event_id,
--     e.track_ids AS old_track_ids,
--     GROUP_CONCAT(r.track_id ORDER BY r.seq_no) AS new_track_ids,
--     CASE
--         WHEN e.track_ids = GROUP_CONCAT(r.track_id ORDER BY r.seq_no) THEN '✅ 一致'
--         ELSE '⚠️ 不一致'
--     END AS status
-- FROM app_composite_event e
-- LEFT JOIN app_event_track_relation r ON e.id = r.event_id
-- GROUP BY e.id
-- HAVING status = '⚠️ 不一致'
-- LIMIT 10;
-- 预期结果：Empty set（所有数据都应该一致）

-- ========================================
-- 测试5：查询特定事件的完整信息（用于手动验证）
-- ========================================
-- 选择一个事件ID进行详细查询（替换下面的1为实际的event_id）
SET @test_event_id = (SELECT id FROM app_composite_event LIMIT 1);

SELECT
    '测试事件基本信息' AS info,
    @test_event_id AS event_id,
    e.event_id AS composite_event_id,
    e.start_time,
    e.end_time,
    e.qymc,
    e.bzzt,
    e.ryxm,
    e.wlry,
    e.xwyy
FROM app_composite_event e
WHERE e.id = @test_event_id;

-- 该事件的所有轨迹（按序号排序）
SELECT
    '测试事件的轨迹列表' AS info,
    r.seq_no,
    r.track_id,
    h.pssj,
    h.jssj,
    h.qymc,
    h.ryxm,
    h.wlry,
    h.rysl
FROM app_event_track_relation r
LEFT JOIN app_track h ON r.track_id = h.id
WHERE r.event_id = @test_event_id
ORDER BY r.seq_no;

-- ========================================
-- 测试6：模拟前端查询（验证业务逻辑）
-- ========================================

-- 6.1 模拟"根据事件ID查询轨迹"（selectTrackIdsByEventId）
SELECT
    '查询事件的轨迹ID列表' AS query_type,
    @test_event_id AS event_id,
    GROUP_CONCAT(track_id ORDER BY seq_no) AS track_ids
FROM app_event_track_relation
WHERE event_id = @test_event_id;

-- 6.2 模拟"根据轨迹ID查询事件"（selectEventIdsByTrackId）
SET @test_track_id = (SELECT track_id FROM app_event_track_relation LIMIT 1);

SELECT
    '查询轨迹所属的事件ID列表' AS query_type,
    @test_track_id AS track_id,
    GROUP_CONCAT(DISTINCT event_id) AS event_ids
FROM app_event_track_relation
WHERE track_id = @test_track_id;

-- ========================================
-- 测试7：性能测试（索引效果验证）
-- ========================================

-- 7.1 查看索引使用情况
EXPLAIN SELECT track_id
FROM app_event_track_relation
WHERE event_id = 1
ORDER BY seq_no;
-- 预期：应该使用 idx_event_id 索引

EXPLAIN SELECT event_id
FROM app_event_track_relation
WHERE track_id = 1;
-- 预期：应该使用 idx_track_id 索引

-- ========================================
-- 测试总结
-- ========================================
SELECT
    '测试总结' AS summary,
    (SELECT COUNT(*) FROM app_event_track_relation) AS total_relations,
    (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation) AS total_events_with_tracks,
    (SELECT COUNT(*) FROM app_composite_event) AS total_events_in_table,
    CASE
        WHEN (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation) =
             (SELECT COUNT(*) FROM app_composite_event)
        THEN '✅ 所有事件都有轨迹关联'
        ELSE '⚠️ 有事件缺少轨迹关联'
    END AS status
FROM DUAL;
