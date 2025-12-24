-- =============================================
-- 插入测试轨迹数据
-- 日期：2025-12-24
-- 说明：创建一些测试轨迹，用于验证复合事件功能
-- =============================================

-- 测试场景1：同一区域、同一人员、30秒内的多个轨迹（应该生成1个复合事件）
INSERT INTO app_track (pssj, jssj, pstp, qyid, qymc, sxtmx, spdz, spsc, jqzt, bzzt, bzsj, xwyy, ryxm, wlry, rysl)
VALUES
-- 第1条轨迹：管理员张三在仓库A，2025-12-24 10:00:00
('2025-12-24 10:00:00', '2025-12-24 10:00:10', '/upload/test1.jpg', 1, '仓库A', '摄像头1', '/video/test1.mp4', '10', '1', '0', NULL, NULL, '张三', NULL, 1),

-- 第2条轨迹：管理员张三在仓库A，10秒后（应该合并到同一事件）
('2025-12-24 10:00:10', '2025-12-24 10:00:20', '/upload/test2.jpg', 1, '仓库A', '摄像头2', '/video/test2.mp4', '10', '1', '0', NULL, NULL, '张三', NULL, 1),

-- 第3条轨迹：管理员张三在仓库A，20秒后（应该合并到同一事件）
('2025-12-24 10:00:20', '2025-12-24 10:00:30', '/upload/test3.jpg', 1, '仓库A', '摄像头3', '/video/test3.mp4', '10', '1', '0', NULL, NULL, '张三', NULL, 1);

-- 测试场景2：不同区域的轨迹（应该生成1个复合事件，包含路径）
INSERT INTO app_track (pssj, jssj, pstp, qyid, qymc, sxtmx, spdz, spsc, jqzt, bzzt, bzsj, xwyy, ryxm, wlry, rysl)
VALUES
-- 管理员李四从仓库B移动到仓库C
('2025-12-24 11:00:00', '2025-12-24 11:00:10', '/upload/test4.jpg', 2, '仓库B', '摄像头4', '/video/test4.mp4', '10', '1', '0', NULL, NULL, '李四', NULL, 1),
('2025-12-24 11:00:15', '2025-12-24 11:00:25', '/upload/test5.jpg', 3, '仓库C', '摄像头5', '/video/test5.mp4', '10', '1', '0', NULL, NULL, '李四', NULL, 1);

-- 测试场景3：外来人员出现（应该生成1个复合事件，包含异常标记）
INSERT INTO app_track (pssj, jssj, pstp, qyid, qymc, sxtmx, spdz, spsc, jqzt, bzzt, bzsj, xwyy, ryxm, wlry, rysl)
VALUES
('2025-12-24 12:00:00', '2025-12-24 12:00:10', '/upload/test6.jpg', 1, '仓库A', '摄像头1', '/video/test6.mp4', '10', '1', '0', NULL, NULL, NULL, '访客-王五', 1);

-- 测试场景4：多人同时出现（应该生成1个复合事件，人员数量>1）
INSERT INTO app_track (pssj, jssj, pstp, qyid, qymc, sxtmx, spdz, spsc, jqzt, bzzt, bzsj, xwyy, ryxm, wlry, rysl)
VALUES
('2025-12-24 13:00:00', '2025-12-24 13:00:10', '/upload/test7.jpg', 2, '仓库B', '摄像头4', '/video/test7.mp4', '10', '1', '0', NULL, NULL, '张三', NULL, 2);

-- 测试场景5：时间间隔超过30秒（应该生成2个独立事件）
INSERT INTO app_track (pssj, jssj, pstp, qyid, qymc, sxtmx, spdz, spsc, jqzt, bzzt, bzsj, xwyy, ryxm, wlry, rysl)
VALUES
-- 第一个事件
('2025-12-24 14:00:00', '2025-12-24 14:00:10', '/upload/test8.jpg', 3, '仓库C', '摄像头5', '/video/test8.mp4', '10', '1', '0', NULL, NULL, '赵六', NULL, 1),
-- 间隔40秒，应该是新事件
('2025-12-24 14:00:50', '2025-12-24 14:01:00', '/upload/test9.jpg', 3, '仓库C', '摄像头5', '/video/test9.mp4', '10', '1', '0', NULL, NULL, '赵六', NULL, 1);

-- 测试场景6：非工作时间的轨迹（应该生成1个复合事件，标记为非工作时间）
INSERT INTO app_track (pssj, jssj, pstp, qyid, qymc, sxtmx, spdz, spsc, jqzt, bzzt, bzsj, xwyy, ryxm, wlry, rysl)
VALUES
('2025-12-24 02:00:00', '2025-12-24 02:00:10', '/upload/test10.jpg', 1, '仓库A', '摄像头1', '/video/test10.mp4', '10', '1', '0', NULL, NULL, '张三', NULL, 1);

-- 测试场景7：已标注的轨迹（应该生成1个已标注的复合事件）
INSERT INTO app_track (pssj, jssj, pstp, qyid, qymc, sxtmx, spdz, spsc, jqzt, bzzt, bzsj, xwyy, ryxm, wlry, rysl)
VALUES
('2025-12-24 15:00:00', '2025-12-24 15:00:10', '/upload/test11.jpg', 2, '仓库B', '摄像头4', '/video/test11.mp4', '10', '1', '1', '2025-12-24 15:05:00', '正常巡检', '李四', NULL, 1);

-- 查看插入结果
SELECT '测试数据插入完成' AS 状态;
SELECT
    '共插入轨迹数' AS 统计项,
    COUNT(*) AS 数量
FROM app_track
WHERE pssj >= '2025-12-24 00:00:00';

-- 预期生成的复合事件统计
SELECT '预期生成事件' AS 说明;
SELECT
    '场景1: 同一区域连续轨迹' AS 场景, '1个事件，包含3条轨迹' AS 预期结果
UNION ALL SELECT '场景2: 跨区域移动', '1个事件，包含2条轨迹，路径=仓库B,仓库C'
UNION ALL SELECT '场景3: 外来人员', '1个事件，hasAbnormalPerson=true'
UNION ALL SELECT '场景4: 多人同时', '1个事件，ryslMax=2'
UNION ALL SELECT '场景5: 超过30秒', '2个独立事件'
UNION ALL SELECT '场景6: 非工作时间', '1个事件，hasNonworktime=true'
UNION ALL SELECT '场景7: 已标注', '1个事件，bzzt=1';

SELECT '下一步操作' AS 提示;
SELECT
    '1. 在前端进入"活动轨迹"页面' AS 步骤1,
    '2. 点击"重新同步复合事件"按钮' AS 步骤2,
    '3. 应该生成约8-9个复合事件' AS 步骤3,
    '4. 在"复合事件管理"页面查看生成的事件' AS 步骤4;
