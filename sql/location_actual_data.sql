-- ============================================
-- 基于实际摄像头数据的配置
-- ============================================

-- 1. 先清空并重建表
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

CREATE TABLE `app_camera_location_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID（关联app_roomip.id）',
  `location_id` bigint(20) NOT NULL COMMENT '位置ID（关联app_location.location_id）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_clr_camera_id` (`camera_id`),
  KEY `idx_clr_location_id` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摄像头与位置关联表';

-- 2. 插入位置数据（根据实际摄像头区域调整）
INSERT INTO `app_location` (`location_code`, `location_name`, `area_name`, `remark`) VALUES
('A01-01', '贵重物品区货架A', 'A区', '贵重物品存放区域'),
('A01-02', '贵重物品区货架B', 'A区', '贵重物品存放区域'),
('B01-01', '刑事案常规物品库货架A', 'B区', '刑事案物品存放区域'),
('B01-02', '刑事案常规物品库货架B', 'B区', '刑事案物品存放区域'),
('C01-01', '恒温恒温库货架A', 'C区', '恒温库存放区域'),
('C01-02', '恒温恒温库货架B', 'C区', '恒温库存放区域'),
('D01-01', '测试区域货架', '测试区', '测试用货架位置');

-- 3. 插入摄像头与位置的关联关系（基于实际gnslx区域名称值）
-- 注意：查询轨迹时使用 app_roomip.gnslx 字段（区域名称），对应 app_track.qymc
-- 不要使用 fjmc 字段（摄像头名称）
-- 根据你提供的数据，摄像头ID分别是：26, 27, 28, 29, 30

-- 贵重物品区的货架关联到"二层贵重物品区"摄像头（ID=26）
INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '二层贵重物品区' AND l.location_code = 'A01-01'
LIMIT 1;

INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '二层贵重物品区' AND l.location_code = 'A01-02'
LIMIT 1;

-- 刑事案物品库的货架关联到"二层刑事案常规物品库"摄像头（ID=27）
INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '二层刑事案常规物品库' AND l.location_code = 'B01-01'
LIMIT 1;

INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '二层刑事案常规物品库' AND l.location_code = 'B01-02'
LIMIT 1;

-- 恒温库的货架关联到"二层恒温恒温库"摄像头（ID=28和29）
-- 一个位置可以关联多个摄像头
INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '二层恒温恒温库' AND l.location_code = 'C01-01';

INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '二层恒温恒温库' AND l.location_code = 'C01-02';

-- 测试区域关联到USB摄像头
INSERT INTO app_camera_location_rel (camera_id, location_id)
SELECT r.id, l.location_id
FROM app_roomip r
CROSS JOIN app_location l
WHERE r.fjmc = '测试区域' AND l.location_code = 'D01-01'
LIMIT 1;

-- 4. 验证插入结果
SELECT
    rel.id AS '关联ID',
    l.location_code AS '位置编码',
    l.location_name AS '位置名称',
    l.area_name AS '所属区域',
    r.id AS '摄像头ID',
    r.fjmc AS '摄像头监控区域',
    r.ip AS '摄像头IP'
FROM app_camera_location_rel rel
INNER JOIN app_location l ON rel.location_id = l.location_id
INNER JOIN app_roomip r ON rel.camera_id = r.id
ORDER BY l.area_name, l.location_code;

-- 5. 检查哪些位置还没有关联摄像头
SELECT
    l.location_code AS '位置编码',
    l.location_name AS '位置名称',
    l.area_name AS '所属区域',
    '未关联' AS '状态'
FROM app_location l
LEFT JOIN app_camera_location_rel rel ON l.location_id = rel.location_id
WHERE rel.id IS NULL;
