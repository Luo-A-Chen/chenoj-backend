-- 按 question_submit 回填 question 表的 submitNum / acceptedNum
-- 适用：判题计数逻辑上线前已有提交记录，导致列表与统计页仍显示 0% (0/0)
-- status: 2=判题成功(AC)，3=判题失败；仅统计已结束判题的提交
-- 执行前请备份；在 MySQL 中运行一次即可

UPDATE question q
    INNER JOIN (
        SELECT questionId,
               SUM(CASE WHEN status IN (2, 3) THEN 1 ELSE 0 END) AS submit_cnt,
               SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END)       AS accept_cnt
        FROM question_submit
        WHERE isDelete = 0
        GROUP BY questionId
    ) stats ON q.id = stats.questionId
SET q.submitNum   = stats.submit_cnt,
    q.acceptedNum = stats.accept_cnt;

-- 从未有过已完成判题提交的题目归零
UPDATE question q
SET q.submitNum = 0,
    q.acceptedNum = 0
WHERE NOT EXISTS (
    SELECT 1
    FROM question_submit qs
    WHERE qs.questionId = q.id
      AND qs.isDelete = 0
      AND qs.status IN (2, 3)
);
