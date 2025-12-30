-- =============================================
-- 资产视频监控系统 - 数据库初始化脚本
-- 版本：4.7.8
-- 创建日期：2025-12-27
-- 说明：包含所有核心表结构
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `caseappdb`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `caseappdb`;

-- =============================================
-- 表1：活动轨迹表 (app_track)
-- 说明：存储所有的活动轨迹记录，包括时间、区域、人数、图片、视频等
-- =============================================

DROP TABLE IF EXISTS `app_track`;

CREATE TABLE `app_track` (
  -- 【主键】
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  -- 【基本信息】
  `pssj` DATETIME DEFAULT NULL COMMENT '拍摄时间（轨迹时间）',
  `qyid` BIGINT DEFAULT NULL COMMENT '区域ID',
  `qymc` VARCHAR(100) DEFAULT NULL COMMENT '区域名称',
  `rysl` INT DEFAULT 0 COMMENT '人员数量',

  -- 【媒体文件】
  `pstp` VARCHAR(500) DEFAULT NULL COMMENT '拍摄图片路径',
  `spdz` VARCHAR(500) DEFAULT NULL COMMENT '视频地址',

  -- 【设备信息】
  `sbmc` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
  `sbid` BIGINT DEFAULT NULL COMMENT '设备ID',

  -- 【标注信息】（会从复合事件同步过来）
  `bzzt` CHAR(1) DEFAULT '0' COMMENT '标注状态：0=未标注，1=已标注',
  `xwyy` VARCHAR(200) DEFAULT NULL COMMENT '行为原因',
  `ryxm` VARCHAR(100) DEFAULT NULL COMMENT '人员姓名',
  `wlry` VARCHAR(100) DEFAULT NULL COMMENT '外来人员',
  `bz` VARCHAR(500) DEFAULT NULL COMMENT '备注',

  -- 【其他字段】
  `sjly` VARCHAR(50) DEFAULT NULL COMMENT '数据来源（海康/本地等）',
  `zt` CHAR(1) DEFAULT '1' COMMENT '状态：0=禁用，1=启用',

  -- 【系统字段】
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`),
  KEY `idx_pssj` (`pssj`) COMMENT '拍摄时间索引',
  KEY `idx_qymc` (`qymc`) COMMENT '区域名称索引',
  KEY `idx_qyid` (`qyid`) COMMENT '区域ID索引',
  KEY `idx_bzzt` (`bzzt`) COMMENT '标注状态索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动轨迹表';

-- =============================================
-- 表2：复合事件表 (app_composite_event)
-- 说明：基于30秒空闲检测算法自动聚合的复合事件
-- =============================================

DROP TABLE IF EXISTS `app_composite_event`;

CREATE TABLE `app_composite_event` (
  -- 【主键和标识】
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` BIGINT NOT NULL COMMENT '事件ID（第一条轨迹的ID）',

  -- 【区域和状态】
  `qyid` BIGINT DEFAULT NULL COMMENT '主要区域ID',
  `qymc` VARCHAR(100) DEFAULT NULL COMMENT '主要区域名称',
  `is_closed` TINYINT(1) DEFAULT 1 COMMENT '是否结束：0=进行中，1=已结束',
  `bzzt` CHAR(1) DEFAULT '0' COMMENT '标注状态：0=待标注，1=已标注',

  -- 【时间范围】
  `start_time` DATETIME NOT NULL COMMENT '开始时间（第一条轨迹时间）',
  `end_time` DATETIME NOT NULL COMMENT '结束时间（最后一条轨迹时间）',
  `duration` INT DEFAULT 0 COMMENT '持续时长（分钟）',

  -- 【统计信息】
  `track_count` INT DEFAULT 0 COMMENT '包含轨迹数量',
  `rysl_max` INT DEFAULT 0 COMMENT '最大人员数量',

  -- 【路径轨迹】
  `path_areas` VARCHAR(1000) DEFAULT NULL COMMENT '经过的区域路径（逗号分隔）',

  -- 【业务标注信息】（聚合的字段）
  `xwyy` VARCHAR(500) DEFAULT NULL COMMENT '行为原因（聚合）',
  `ryxm` VARCHAR(500) DEFAULT NULL COMMENT '人员姓名（聚合，逗号分隔）',
  `wlry` VARCHAR(500) DEFAULT NULL COMMENT '外来人员（聚合，逗号分隔）',

  -- 【特殊标签】
  `has_nonworktime` TINYINT(1) DEFAULT 0 COMMENT '是否包含非工作时间：0=否，1=是',
  `has_abnormal_person` TINYINT(1) DEFAULT 0 COMMENT '是否人员异常：0=否，1=是',

  -- 【旧设计字段】（仅用于兼容，推荐使用关系表）
  `track_ids` VARCHAR(2000) DEFAULT NULL COMMENT '包含的轨迹ID列表（逗号分隔，已废弃）',

  -- 【系统字段】
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_id` (`event_id`) COMMENT '事件ID唯一索引',
  KEY `idx_time_range` (`start_time`, `end_time`) COMMENT '时间范围查询',
  KEY `idx_status` (`bzzt`, `is_closed`) COMMENT '状态查询',
  KEY `idx_created` (`create_time`) COMMENT '创建时间查询',
  KEY `idx_qymc` (`qymc`) COMMENT '区域名称索引',
  KEY `idx_person` (`ryxm`(100)) COMMENT '人员姓名前缀索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复合事件表（30秒空闲检测算法）';

-- =============================================
-- 表3：复合事件与轨迹关系表 (app_event_track_relation)
-- 说明：多对多关系表，记录事件和轨迹的关联关系
-- =============================================

DROP TABLE IF EXISTS `app_event_track_relation`;

CREATE TABLE `app_event_track_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `event_id` BIGINT NOT NULL COMMENT '复合事件ID，关联app_composite_event.id',
  `track_id` BIGINT NOT NULL COMMENT '轨迹ID，关联app_track.id',
  `seq_no` INT DEFAULT 0 COMMENT '轨迹在事件中的顺序号（从1开始）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关联建立时间',

  PRIMARY KEY (`id`),
  KEY `idx_event_id` (`event_id`),
  KEY `idx_track_id` (`track_id`),
  UNIQUE KEY `uk_event_track` (`event_id`, `track_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复合事件与轨迹关系表';

-- =============================================
-- 表4：门禁信息表 (app_person) - 可选
-- 说明：如果系统使用门禁数据，可以创建此表
-- =============================================

DROP TABLE IF EXISTS `app_person`;

CREATE TABLE `app_person` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mjmc` VARCHAR(100) DEFAULT NULL COMMENT '门禁名称',
  `mjwz` VARCHAR(200) DEFAULT NULL COMMENT '门禁位置',
  `mjzt` CHAR(1) DEFAULT '1' COMMENT '门禁状态：0=禁用，1=启用',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `port` INT DEFAULT NULL COMMENT '端口号',

  -- 系统字段
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁信息表';

-- =============================================
-- 表5：房间IP配置表 (app_room_ip) - 可选
-- 说明：房间与摄像头IP的对应关系
-- =============================================

DROP TABLE IF EXISTS `app_room_ip`;

CREATE TABLE `app_room_ip` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_name` VARCHAR(100) DEFAULT NULL COMMENT '房间名称',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `camera_id` VARCHAR(100) DEFAULT NULL COMMENT '摄像头ID',
  `status` CHAR(1) DEFAULT '1' COMMENT '状态：0=禁用，1=启用',

  -- 系统字段
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间IP配置表';

-- =============================================
-- 初始化示例数据（可选）
-- =============================================

-- 插入示例区域数据（如果需要）
/*
INSERT INTO `app_track` (`pssj`, `qymc`, `rysl`, `pstp`, `spdz`, `bzzt`, `create_time`)
VALUES
  ('2025-12-27 09:00:00', '办案区入口', 1, '/profile/upload/2025/12/27/test1.jpg', '/profile/upload/2025/12/27/test1.mp4', '0', NOW()),
  ('2025-12-27 09:00:15', '办案区入口', 1, '/profile/upload/2025/12/27/test2.jpg', '/profile/upload/2025/12/27/test2.mp4', '0', NOW()),
  ('2025-12-27 09:01:00', '走廊', 2, '/profile/upload/2025/12/27/test3.jpg', '/profile/upload/2025/12/27/test3.mp4', '0', NOW()),
  ('2025-12-27 10:00:00', '办案区入口', 1, '/profile/upload/2025/12/27/test4.jpg', '/profile/upload/2025/12/27/test4.mp4', '0', NOW());
*/

-- =============================================
-- 外键约束（可选，数据稳定后添加）
-- =============================================

/*
-- 为关系表添加外键约束
ALTER TABLE `app_event_track_relation`
ADD CONSTRAINT `fk_relation_event`
  FOREIGN KEY (`event_id`) REFERENCES `app_composite_event` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `app_event_track_relation`
ADD CONSTRAINT `fk_relation_track`
  FOREIGN KEY (`track_id`) REFERENCES `app_track` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
*/

-- =============================================
-- 统计信息
-- =============================================

SELECT '数据库初始化完成' AS status;
SELECT
  '核心表已创建:' AS info,
  'app_track (活动轨迹表)' AS table1,
  'app_composite_event (复合事件表)' AS table2,
  'app_event_track_relation (关系表)' AS table3;

-- 查看表结构
SHOW TABLES LIKE 'app_%';

-- =============================================
-- 使用说明
-- =============================================
/*
部署步骤：
1. 创建数据库：CREATE DATABASE caseappdb
2. 执行本脚本：mysql -u root -p caseappdb < 00_init_database.sql
3. 导入RuoYi框架的系统表（如果有单独的脚本）
4. 修改application-druid.yml配置数据库连接
5. 启动应用，访问：https://localhost:8090

表关系说明：
- app_track: 存储原始活动轨迹数据
- app_composite_event: 存储聚合后的复合事件
- app_event_track_relation: 记录事件和轨迹的多对多关系

查询示例：
-- 查询某个事件的所有轨迹
SELECT h.*
FROM app_track h
JOIN app_event_track_relation r ON h.id = r.track_id
WHERE r.event_id = (SELECT id FROM app_composite_event WHERE event_id = 1)
ORDER BY r.seq_no;

-- 查询今日复合事件
SELECT * FROM app_composite_event
WHERE DATE(start_time) = CURDATE()
ORDER BY start_time DESC;

-- 查询待标注事件
SELECT * FROM app_composite_event
WHERE bzzt = '0'
ORDER BY start_time DESC;
*/
