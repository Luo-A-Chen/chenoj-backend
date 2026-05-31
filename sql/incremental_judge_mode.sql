-- ============================================================
-- 增量脚本：判题模式（judgeMode）与函数题配置
-- 适用：已有 my_db / question 表，无需新增列（字段均在 judgeConfig JSON 内）
-- 执行前请确认库名、管理员 userId
-- ============================================================

USE my_db;

-- 1) 更新字段注释（可选，仅文档用途）
ALTER TABLE question
    MODIFY COLUMN judgeConfig text NULL COMMENT '判题配置（json：timeLimit/ms,memoryLimit/KB,judgeMode,methodName,paramTypes,returnType）';

-- 2) 为旧题目的 judgeConfig 补全 judgeMode=stdio（仅处理合法 JSON 且尚未配置 judgeMode 的行）
UPDATE question
SET judgeConfig = JSON_SET(CAST(judgeConfig AS JSON), '$.judgeMode', 'stdio')
WHERE judgeConfig IS NOT NULL
  AND TRIM(judgeConfig) <> ''
  AND JSON_VALID(judgeConfig)
  AND (
        JSON_EXTRACT(judgeConfig, '$.judgeMode') IS NULL
        OR JSON_UNQUOTE(JSON_EXTRACT(judgeConfig, '$.judgeMode')) = ''
    );

-- 3) 可选：插入「二维数组中查找整数」示例题（Java 函数题）
--    请将 @admin_user_id 改为你库中真实管理员/出题人 id
SET @admin_user_id = 1;

INSERT INTO question (
    title,
    content,
    tags,
    answer,
    submitNum,
    acceptedNum,
    judgeCase,
    judgeConfig,
    thumbNum,
    favourNum,
    userId,
    isDelete
) VALUES (
    '二维数组中查找整数',
    '在一个二维数组 array 中（每个一维数组的长度相同），每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。请完成函数 `Find(int target, int[][] array)`，判断数组中是否含有该整数。

**示例**

- 给定 `target = 7`，`array = [[1,2,8,9],[2,4,9,12],[4,7,10,13],[6,8,11,15]]`，返回 `true`
- 给定 `target = 3`，返回 `false`

**数据范围**

- `0 <= n, m <= 500`
- `-10^9 <= val <= 10^9`

**进阶**

- 空间复杂度 O(1)，时间复杂度 O(n + m)

**说明**

- 判题模式为 Java 函数题，只需实现 `class Solution` 中的 `Find` 方法
- 平台会自动将 JSON 用例转换为方法参数并比对返回值',
    '["数组","矩阵","二分","Java函数题"]',
    NULL,
    0,
    0,
    '[{"input":"[7,[[1,2,8,9],[2,4,9,12],[4,7,10,13],[6,8,11,15]]]","output":"true"},{"input":"[1,[[2]]]","output":"false"},{"input":"[3,[[1,2,8,9],[2,4,9,12],[4,7,10,13],[6,8,11,15]]]","output":"false"}]',
    '{"judgeMode":"function_java","methodName":"Find","paramTypes":["int","int[][]"],"returnType":"boolean","timeLimit":1000,"memoryLimit":65536}',
    0,
    0,
    @admin_user_id,
    0
);

-- 4) 校验（执行后可查看）
-- SELECT id, title, judgeConfig, LEFT(judgeCase, 120) AS judgeCase_preview FROM question ORDER BY id DESC LIMIT 5;
