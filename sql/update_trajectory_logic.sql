-- ============================================
-- 活动轨迹逻辑升级：添加检测次数和截图关联表
-- ============================================

-- 1. 添加检测次数字段到轨迹表
ALTER TABLE app_track ADD COLUMN jscs INT DEFAULT 1 COMMENT '检测次数，默认为1';

-- 2. 创建活动轨迹与截图关联表
DROP TABLE IF EXISTS `app_track_screenshot`;
CREATE TABLE `app_track_screenshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `track_id` bigint(20) NOT NULL COMMENT '活动轨迹ID（关联app_track.id）',
  `screenshot_url` varchar(512) NOT NULL COMMENT '截图URL路径',
  `screenshot_time` datetime NOT NULL COMMENT '截图时间',
  `screenshot_order` int DEFAULT 1 COMMENT '截图顺序（第几次检测）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_track_id` (`track_id`),
  KEY `idx_screenshot_time` (`screenshot_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动轨迹截图关联表';

-- 3. 验证表结构
SHOW COLUMNS FROM app_track LIKE 'jscs';
SHOW CREATE TABLE app_track_screenshot;
