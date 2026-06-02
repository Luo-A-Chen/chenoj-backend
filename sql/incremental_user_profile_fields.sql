-- 用户表：移除微信字段，新增个人资料字段
-- 在 Navicat / mysql 客户端对 my_db 执行

USE my_db;

ALTER TABLE user DROP INDEX idx_unionId;
ALTER TABLE user DROP COLUMN unionId;
ALTER TABLE user DROP COLUMN mpOpenId;

ALTER TABLE user
    ADD COLUMN careerDirection varchar(64) NULL COMMENT '职业方向' AFTER userProfile,
    ADD COLUMN position varchar(64) NULL COMMENT '职位' AFTER careerDirection,
    ADD COLUMN companyType varchar(64) NULL COMMENT '公司类型' AFTER position;
