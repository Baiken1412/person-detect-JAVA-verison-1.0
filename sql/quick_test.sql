-- =============================================
-- 快速测试脚本 - 在Navicat中直接运行
-- 复制全部内容到Navicat查询窗口并执行
-- =============================================

-- 测试1：关系表基本统计
SELECT '=== 测试1：关系表基本统计 ===' AS test_section;
SELECT
    COUNT(*) AS 总关系数,
    COUNT(DISTINCT event_id) AS 关联的事件数,
    COUNT(DISTINCT track_id) AS 关联的轨迹数,
    MIN(created_at) AS 最早关系创建时间,
    MAX(created_at) AS 最新关系创建时间
FROM app_event_track_relation;

-- 测试2：健康检查
SELECT '=== 测试2：健康检查 ===' AS test_section;
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM app_event_track_relation) = 0
        THEN '⚠️ 关系表为空'
        WHEN EXISTS (
            SELECT 1 FROM app_event_track_relation r
            LEFT JOIN app_composite_event e ON r.event_id = e.id
            WHERE e.id IS NULL
        )
        THEN '❌ 存在孤立事件引用'
        WHEN EXISTS (
            SELECT 1 FROM app_event_track_relation r
            LEFT JOIN app_track h ON r.track_id = h.id
            WHERE h.id IS NULL
        )
        THEN '❌ 存在孤立轨迹引用'
        WHEN EXISTS (
            SELECT 1 FROM app_composite_event e
            LEFT JOIN app_event_track_relation r ON e.id = r.event_id
            WHERE r.id IS NULL
        )
        THEN '⚠️ 存在没有轨迹的事件'
        ELSE '✅ 关系表健康'
    END AS 健康状态;

-- 测试3：数据完整性检查
SELECT '=== 测试3：数据完整性检查 ===' AS test_section;

-- 3.1 检查孤立事件
SELECT '孤立事件检查' AS 检查项, COUNT(DISTINCT r.event_id) AS 数量
FROM app_event_track_relation r
LEFT JOIN app_composite_event e ON r.event_id = e.id
WHERE e.id IS NULL;

-- 3.2 检查孤立轨迹
SELECT '孤立轨迹检查' AS 检查项, COUNT(DISTINCT r.track_id) AS 数量
FROM app_event_track_relation r
LEFT JOIN app_track h ON r.track_id = h.id
WHERE h.id IS NULL;

-- 3.3 检查没有轨迹的事件
SELECT '无轨迹事件检查' AS 检查项, COUNT(*) AS 数量
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
WHERE r.id IS NULL;

-- 测试4：查看最近5个事件的轨迹关联
SELECT '=== 测试4：最近5个事件的轨迹关联详情 ===' AS test_section;
SELECT
    e.event_id AS 复合事件ID,
    e.start_time AS 开始时间,
    e.qymc AS 区域名称,
    COUNT(r.track_id) AS 轨迹数量,
    GROUP_CONCAT(r.track_id ORDER BY r.seq_no SEPARATOR ',') AS 轨迹ID列表,
    e.bzzt AS 标注状态
FROM app_composite_event e
LEFT JOIN app_event_track_relation r ON e.id = r.event_id
GROUP BY e.id, e.event_id, e.start_time, e.qymc, e.bzzt
ORDER BY e.create_time DESC
LIMIT 5;

-- 测试5：序号连续性检查
SELECT '=== 测试5：序号连续性检查 ===' AS test_section;
SELECT
    r.event_id AS 事件ID,
    COUNT(*) AS 轨迹数量,
    MAX(r.seq_no) AS 最大序号,
    CASE
        WHEN COUNT(*) = MAX(r.seq_no) THEN '✅ 序号连续'
        ELSE '⚠️ 序号不连续'
    END AS 检查结果
FROM app_event_track_relation r
GROUP BY r.event_id
HAVING 检查结果 = '⚠️ 序号不连续'
LIMIT 10;

-- 测试6：事件轨迹数量分布
SELECT '=== 测试6：事件轨迹数量分布 ===' AS test_section;
SELECT
    track_count AS 轨迹数量,
    COUNT(*) AS 事件数量
FROM (
    SELECT
        event_id,
        COUNT(*) AS track_count
    FROM app_event_track_relation
    GROUP BY event_id
) AS counts
GROUP BY track_count
ORDER BY track_count;

-- 测试7：对比复合事件表和关系表的数据一致性
SELECT '=== 测试7：数据一致性验证 ===' AS test_section;
SELECT
    (SELECT COUNT(*) FROM app_composite_event) AS 复合事件总数,
    (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation) AS 关系表中的事件数,
    CASE
        WHEN (SELECT COUNT(*) FROM app_composite_event) =
             (SELECT COUNT(DISTINCT event_id) FROM app_event_track_relation)
        THEN '✅ 数据一致'
        ELSE '⚠️ 数据不一致'
    END AS 一致性检查;

-- 测试8：查看一个具体事件的详细信息（用于验证前端显示）
SELECT '=== 测试8：随机抽取一个事件的详细信息 ===' AS test_section;

-- 设置变量
SET @sample_event_id = (SELECT id FROM app_composite_event ORDER BY create_time DESC LIMIT 1);

-- 事件基本信息
SELECT
    '事件基本信息' AS 信息类型,
    e.id AS 数据库ID,
    e.event_id AS 复合事件ID,
    e.start_time AS 开始时间,
    e.end_time AS 结束时间,
    e.qymc AS 区域名称,
    e.bzzt AS 标注状态,
    e.xwyy AS 行为原因,
    e.ryxm AS 管理员,
    e.wlry AS 外来人员
FROM app_composite_event e
WHERE e.id = @sample_event_id;

-- 该事件的所有轨迹
SELECT
    '事件轨迹列表' AS 信息类型,
    r.seq_no AS 序号,
    r.track_id AS 轨迹ID,
    h.pssj AS 拍摄时间,
    h.qymc AS 区域,
    h.ryxm AS 管理员,
    h.wlry AS 外来人员,
    h.rysl AS 人员数量,
    h.bzzt AS 标注状态
FROM app_event_track_relation r
LEFT JOIN app_track h ON r.track_id = h.id
WHERE r.event_id = @sample_event_id
ORDER BY r.seq_no;

-- 测试9：索引使用情况验证
SELECT '=== 测试9：索引使用情况（执行计划） ===' AS test_section;
-- 注意：下面两个EXPLAIN语句需要单独执行查看结果

-- EXPLAIN SELECT track_id FROM app_event_track_relation WHERE event_id = 1 ORDER BY seq_no;
-- EXPLAIN SELECT event_id FROM app_event_track_relation WHERE track_id = 1;

-- 测试总结
SELECT '=== 测试总结 ===' AS test_section;
SELECT
    '✅ 测试完成' AS 状态,
    '如果以上测试都通过，说明关系表工作正常' AS 说明,
    CONCAT('关系表共有 ',
           (SELECT COUNT(*) FROM app_event_track_relation),
           ' 条记录') AS 统计信息;
