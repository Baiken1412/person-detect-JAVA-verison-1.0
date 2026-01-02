-- ============================================
-- 图片路径迁移脚本
-- 功能：将完整URL转换为相对路径
-- 日期：2026-01-02
-- ============================================

-- 备份提示
-- 执行前请先备份数据库！
-- mysqldump -u root -p caseapp_db > backup_before_migration.sql

-- 1. 更新 app_track 表的 pstp 字段（轨迹首图）
UPDATE app_track
SET pstp = CASE
    -- 如果包含完整URL，则提取相对路径
    WHEN pstp LIKE 'http%/profile/caseapp/%' THEN
        SUBSTRING(pstp, LOCATE('/profile/caseapp/', pstp) + LENGTH('/profile/caseapp/'))
    -- 如果只包含 /profile/caseapp/，则去掉这个前缀
    WHEN pstp LIKE '/profile/caseapp/%' THEN
        SUBSTRING(pstp, LENGTH('/profile/caseapp/') + 1)
    -- 已经是相对路径，不处理
    ELSE pstp
END
WHERE pstp IS NOT NULL AND pstp != '';

-- 2. 更新 app_track_screenshot 表的 screenshot_url 字段（轨迹截图）
UPDATE app_track_screenshot
SET screenshot_url = CASE
    WHEN screenshot_url LIKE 'http%/profile/caseapp/%' THEN
        SUBSTRING(screenshot_url, LOCATE('/profile/caseapp/', screenshot_url) + LENGTH('/profile/caseapp/'))
    WHEN screenshot_url LIKE '/profile/caseapp/%' THEN
        SUBSTRING(screenshot_url, LENGTH('/profile/caseapp/') + 1)
    ELSE screenshot_url
END
WHERE screenshot_url IS NOT NULL AND screenshot_url != '';

-- 3. 验证迁移结果
-- 检查 app_track 表
SELECT
    COUNT(*) as total_records,
    SUM(CASE WHEN pstp LIKE 'http%' THEN 1 ELSE 0 END) as still_has_http,
    SUM(CASE WHEN pstp LIKE '/profile/%' THEN 1 ELSE 0 END) as still_has_profile,
    SUM(CASE WHEN pstp NOT LIKE 'http%' AND pstp NOT LIKE '/profile/%' AND pstp IS NOT NULL AND pstp != '' THEN 1 ELSE 0 END) as relative_paths
FROM app_track;

-- 检查 app_track_screenshot 表
SELECT
    COUNT(*) as total_records,
    SUM(CASE WHEN screenshot_url LIKE 'http%' THEN 1 ELSE 0 END) as still_has_http,
    SUM(CASE WHEN screenshot_url LIKE '/profile/%' THEN 1 ELSE 0 END) as still_has_profile,
    SUM(CASE WHEN screenshot_url NOT LIKE 'http%' AND screenshot_url NOT LIKE '/profile/%' AND screenshot_url IS NOT NULL AND screenshot_url != '' THEN 1 ELSE 0 END) as relative_paths
FROM app_track_screenshot;

-- 4. 查看示例数据（可选）
SELECT id, pstp FROM app_track WHERE pstp IS NOT NULL LIMIT 10;
SELECT id, screenshot_url FROM app_track_screenshot WHERE screenshot_url IS NOT NULL LIMIT 10;

-- ============================================
-- 迁移完成后的数据格式示例：
-- 旧格式：https://192.168.1.20:8090/profile/caseapp/20260102/1_大门区域摄像头1_20260102093827.jpg
-- 新格式：20260102/1_大门区域摄像头1_20260102093827.jpg
-- ============================================
