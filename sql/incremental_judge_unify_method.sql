-- ============================================================
-- 增量脚本：统一为 Java 方法题（LeetCode 式）
-- 变更：
--   1) question.judgeConfig 列注释（去掉 judgeMode 说明）
--   2) 从已有 judgeConfig JSON 中删除 $.judgeMode 字段
-- 适用：已执行过 create_table / incremental_judge_mode 的 my_db
-- 在 Navicat / mysql 客户端执行；执行前请确认库名
-- 说明：本脚本替代 incremental_judge_mode.sql 中「补全 judgeMode=stdio」等步骤，勿再执行旧脚本第 2 步
-- ============================================================

USE my_db;

-- 1) 列注释（与 create_table.sql 终态一致，无 judgeMode）
ALTER TABLE question
    MODIFY COLUMN judgeConfig text NULL
        COMMENT '判题配置（json：timeLimit/ms,memoryLimit/KB,methodName,paramTypes,returnType）';

-- 2) 清理 judgeConfig 中的 judgeMode（stdio / function_java 等均移除）
UPDATE question
SET judgeConfig = JSON_REMOVE(CAST(judgeConfig AS JSON), '$.judgeMode')
WHERE judgeConfig IS NOT NULL
  AND TRIM(judgeConfig) <> ''
  AND JSON_VALID(judgeConfig)
  AND JSON_EXTRACT(judgeConfig, '$.judgeMode') IS NOT NULL;

-- 3) 校验（可选，执行后查看）
-- SELECT id, title, judgeConfig, LEFT(judgeCase, 80) AS judgeCase_preview
-- FROM question
-- WHERE isDelete = 0
-- ORDER BY id DESC
-- LIMIT 10;
