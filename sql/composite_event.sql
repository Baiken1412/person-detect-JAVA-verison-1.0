-- =============================================
-- 复合事件表（基于30秒空闲检测算法）
-- 作者：Claude Code
-- 日期：2025-12-23
-- 说明：应用层实时写入方案
-- =============================================

DROP TABLE IF EXISTS `app_composite_event`;

CREATE TABLE `app_composite_event` (
  -- 【主键和标识】
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` bigint(20) NOT NULL COMMENT '事件ID（第一条轨迹的ID）',

  -- 【区域和状态】
  `qyid` bigint(20) DEFAULT NULL COMMENT '主要区域ID',
  `qymc` varchar(100) DEFAULT NULL COMMENT '主要区域名称',
  `is_closed` tinyint(1) DEFAULT '1' COMMENT '是否结束：0=进行中，1=已结束',
  `bzzt` char(1) DEFAULT '0' COMMENT '标注状态：0=待标注，1=已标注',

  -- 【时间范围】
  `start_time` datetime NOT NULL COMMENT '开始时间（第一条轨迹时间）',
  `end_time` datetime NOT NULL COMMENT '结束时间（最后一条轨迹时间）',
  `duration` int(11) DEFAULT '0' COMMENT '持续时长（分钟）',

  -- 【统计信息】
  `track_count` int(11) DEFAULT '0' COMMENT '包含轨迹数量',

  -- 【业务标注信息】（聚合的字段）
  `xwyy` varchar(500) DEFAULT NULL COMMENT '行为原因（聚合）',
  `ryxm` varchar(500) DEFAULT NULL COMMENT '人员姓名（聚合，逗号分隔）',
  `wlry` varchar(500) DEFAULT NULL COMMENT '外来人员（聚合，逗号分隔）',
  `rysl_max` int(11) DEFAULT '0' COMMENT '最大人员数量',

  -- 【路径轨迹】
  `path_areas` varchar(1000) DEFAULT NULL COMMENT '经过的区域路径（逗号分隔）',

  -- 【特殊标签】
  `has_nonworktime` tinyint(1) DEFAULT '0' COMMENT '是否包含非工作时间：0=否，1=是',
  `has_abnormal_person` tinyint(1) DEFAULT '0' COMMENT '是否人员异常：0=否，1=是',

  -- 【关联轨迹】（重要！用于数据一致性）
  `track_ids` varchar(2000) DEFAULT NULL COMMENT '包含的轨迹ID列表（逗号分隔）',

  -- 【系统字段】
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_id` (`event_id`) COMMENT '事件ID唯一索引',
  KEY `idx_time_range` (`start_time`, `end_time`) COMMENT '时间范围查询',
  KEY `idx_status` (`bzzt`, `is_closed`) COMMENT '状态查询',
  KEY `idx_created` (`create_time`) COMMENT '创建时间查询',
  KEY `idx_person` (`ryxm`(100)) COMMENT '人员姓名前缀索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复合事件表（30秒空闲检测）';

-- 创建索引说明
-- uk_event_id: 保证每个事件ID唯一，避免重复
-- idx_time_range: 支持时间范围查询
-- idx_status: 支持按标注状态和结束状态筛选
-- idx_created: 支持按创建时间排序
-- idx_person: 支持按人员姓名模糊查询
