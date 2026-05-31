-- ============================================================
-- 增量脚本：question_comment 增加 replyToUserId
-- 适用：已有 question_comment 表，需要补充二级回复目标用户字段
-- ============================================================

USE my_db;

ALTER TABLE question_comment
ADD COLUMN replyToUserId BIGINT NULL COMMENT '被回复用户 id（仅二级回复使用）';
