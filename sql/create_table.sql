# 数据库初始化
# @author <a href="https://github.com/luochen">程序员啊琛</a>
# @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>

-- 创建库
create database if not exists my_db;

-- 切换库
use my_db;

-- 1.用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    careerDirection varchar(64)                         null comment '职业方向',
    position     varchar(64)                            null comment '职位',
    companyType  varchar(64)                            null comment '公司类型',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;

-- 1.1 用户账号绑定表
create table if not exists user_auth_bind
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint       not null comment '本站用户 id',
    authType   varchar(32)  not null comment 'phone/wechat/weibo/github',
    authId     varchar(256) not null comment '平台唯一标识或手机号',
    authName   varchar(256) null comment '展示名',
    createTime datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint      default 0                 not null comment '是否删除',
    unique key uk_auth (authType, authId),
    unique key uk_user_type (userId, authType),
    index idx_user (userId)
) comment '用户账号绑定' collate = utf8mb4_unicode_ci;

-- 2.题目表
create table if not exists question
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    answer      text          null comment '题目答案',
    submitNum   int default 0 not null comment '题目提交数',
    acceptedNum int default 0 not null comment '题目通过数',
    judgeCase   text          null comment '判题用例（json 数组）',
    judgeConfig text          null comment '判题配置（json：timeLimit/ms,memoryLimit/KB,methodName,paramTypes,returnType）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '题目' collate = utf8mb4_unicode_ci;

-- 3.题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(256)                       null comment '编程语言',
    code       text                               null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    status     int      default 0                 not null comment '状态（0-待判题, 1-判题中, 2-判题成功, 3-判题失败）',
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目提交';

-- 4.题目评论表（支持一级评论 + 二级回复）
create table if not exists question_comment
(
    id         bigint auto_increment comment 'id' primary key,
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '评论用户 id',
    content    varchar(1000)                      not null comment '评论内容',
    parentId   bigint   default 0                 not null comment '父评论 id，0 为一级评论，>0 为二级回复',
    replyToUserId bigint                          null comment '被回复用户 id（仅二级回复使用）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId),
    index idx_parentId (parentId)
) comment '题目评论';
