-- 查询最新轨迹信息
SET @latest_jssj = (SELECT jssj FROM app_track ORDER BY pssj DESC LIMIT 1);
SET @latest_qyid = (SELECT qyid FROM app_track WHERE qyid != 1 ORDER BY pssj DESC LIMIT 1); -- 排除USB摄像头(qyid=1)

-- 显示最新轨迹的结束时间
SELECT 
    '最新轨迹结束时间' AS info,
    @latest_jssj AS jssj;

-- 计算新轨迹的开始时间（最新轨迹结束后15秒，保证在30秒窗口内）
SET @new_pssj = DATE_ADD(@latest_jssj, INTERVAL 15 SECOND);
SET @new_jssj = DATE_ADD(@new_pssj, INTERVAL 5 SECOND);

-- 显示将要插入的时间
SELECT 
    '新轨迹时间范围' AS info,
    @new_pssj AS pssj,
    @new_jssj AS jssj,
    TIMESTAMPDIFF(SECOND, @latest_jssj, @new_pssj) AS '距上条轨迹间隔(秒)';

-- 插入测试轨迹（使用非USB摄像头）
INSERT INTO app_track (
    pssj,           -- 开始时间
    jssj,           -- 结束时间  
    qyid,           -- 区域ID（使用非USB摄像头）
    qymc,           -- 区域名称
    sxtmx,          -- 摄像头名称
    jscs,           -- 检测次数
    rysl,           -- 人员数量
    bzzt,           -- 标注状态（0=未标注）
    pstp,           -- 拍摄图片路径（可选）
    sptp,           -- 视频路径（可选）
    create_time     -- 创建时间
) VALUES (
    @new_pssj,
    @new_jssj,
    IFNULL(@latest_qyid, 2),  -- 如果没找到非USB摄像头，使用qyid=2
    '测试区域',
    '测试摄像头',
    1,              -- 检测1次
    2,              -- 2个人
    '0',            -- 未标注
    NULL,
    NULL,
    NOW()
);

-- 显示插入结果
SELECT 
    '✅ 测试轨迹已插入' AS status,
    LAST_INSERT_ID() AS new_track_id,
    @new_pssj AS pssj,
    @new_jssj AS jssj;

-- 显示最近的两条轨迹（验证）
SELECT 
    id,
    pssj,
    jssj,
    qymc,
    sxtmx,
    jscs,
    rysl,
    '--- 上下两条轨迹应该在30秒内 ---' AS note
FROM app_track
ORDER BY pssj DESC
LIMIT 2;
