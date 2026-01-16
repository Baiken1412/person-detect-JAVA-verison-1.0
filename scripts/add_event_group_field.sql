-- 为 app_roomip 表添加 event_group 字段
-- 用于摄像头分组计算复合事件
-- null 或 0 = 不参与复合事件计算
-- 相同数字的摄像头一起计算复合事件（如全是1的一起计算，全是2的一起计算）

-- 添加 event_group 字段
ALTER TABLE app_roomip ADD COLUMN event_group INT DEFAULT NULL COMMENT '事件分组';

-- 注意：执行此脚本后，需要手动为每个摄像头设置 event_group 值
-- 例如：1层的摄像头设置为 1，2层的摄像头设置为 2
-- 不需要参与复合事件计算的摄像头（如收物室）设置为 null 或 0
