-- 用户第三方/手机绑定记录（仅落库，OAuth 后续扩展）
USE my_db;

CREATE TABLE IF NOT EXISTS user_auth_bind
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userId     BIGINT       NOT NULL COMMENT '本站用户 id',
    authType   VARCHAR(32)  NOT NULL COMMENT 'phone/wechat/weibo/github',
    authId     VARCHAR(256) NOT NULL COMMENT '平台唯一标识或手机号',
    authName   VARCHAR(256) NULL COMMENT '展示名',
    createTime DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_auth (authType, authId),
    UNIQUE KEY uk_user_type (userId, authType),
    INDEX idx_user (userId)
) COMMENT '用户账号绑定' COLLATE = utf8mb4_unicode_ci;
