-- ============================================
-- 基于货架位置的人员活动轨迹查询 - 数据库脚本
-- ============================================

-- 1. 创建货架位置表
DROP TABLE IF EXISTS `app_camera_location_rel`;
DROP TABLE IF EXISTS `app_location`;

CREATE TABLE `app_location` (
  `location_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '位置主键ID',
  `location_code` varchar(64) NOT NULL COMMENT '位置编码，如 A01-03-02',
  `location_name` varchar(128) NOT NULL COMMENT '位置名称',
  `area_name` varchar(64) DEFAULT NULL COMMENT '所属区域，如 A区、B区',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`location_id`),
  UNIQUE KEY `idx_location_code` (`location_code`),
  KEY `idx_area_name` (`area_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货架位置表';

-- 2. 创建摄像头与位置关联表（多对多）
CREATE TABLE `app_camera_location_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID（关联app_roomip.id）',
  `location_id` bigint(20) NOT NULL COMMENT '位置ID（关联app_location.location_id）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_clr_camera_id` (`camera_id`),
  KEY `idx_clr_location_id` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摄像头与位置关联表';

-- 3. 插入测试位置数据
INSERT INTO `app_location` (`location_code`, `location_name`, `area_name`, `remark`) VALUES
('A01-03-02', '东区货架A组', 'A区', '东区入口货架区域'),
('A01-03-03', '东区货架B组', 'A区', '东区货架B组区域'),
('A02-01-05', '西区货架C组', 'A区', '西区货架区域'),
('B01-02-01', '北区货架D组', 'B区', '北区货架区域'),
('B01-02-02', '北区货架E组', 'B区', '北区货架E组'),
('C01-01-03', '南区货架F组', 'C区', '南区货架区域');

-- 4. 查看app_roomip表中的摄像头数据（用于手动配置关联）
-- 执行下面的查询，查看可用的摄像头
SELECT
    id AS '摄像头ID',
    fjmc AS '房间名称/区域名称',
    ip AS 'IP地址',
    dk AS '端口'
FROM app_roomip
ORDER BY fjmc;

-- 5. 根据上面查询的结果，手动插入关联关系
-- 下面是示例SQL，请根据实际的摄像头ID修改
--
-- 方法1：如果知道具体的摄像头ID，直接插入
-- INSERT INTO app_camera_location_rel (camera_id, location_id) VALUES
-- (1, 1),  -- 摄像头1 关联 位置1(A01-03-02)
-- (2, 2);  -- 摄像头2 关联 位置2(A01-03-03)
--
-- 方法2：使用子查询自动匹配（如果fjmc字段有值）
-- 这种方式会自动查找匹配的摄像头ID

-- A区位置关联
INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '进门' AND l.location_code = 'A01-03-02'
LIMIT 1;

INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '枪械库外门' AND l.location_code = 'A01-03-03'
LIMIT 1;

INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '枪械库' AND l.location_code = 'A02-01-05'
LIMIT 1;

-- B区位置关联
INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '枪械库内门' AND l.location_code = 'B01-02-01'
LIMIT 1;

INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '卷宗库' AND l.location_code = 'B01-02-02'
LIMIT 1;

-- C区位置关联
INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '卷宗库内门' AND l.location_code = 'C01-01-03'
LIMIT 1;

-- 6. 验证数据插入情况
SELECT
    l.location_code AS '位置编码',
    l.location_name AS '位置名称',
    l.area_name AS '所属区域',
    r.fjmc AS '关联摄像头区域',
    r.ip AS '摄像头IP',
    rel.id AS '关联ID'
FROM app_location l
LEFT JOIN app_camera_location_rel rel ON l.location_id = rel.location_id
LEFT JOIN app_roomip r ON rel.camera_id = r.id
ORDER BY l.area_name, l.location_code;
