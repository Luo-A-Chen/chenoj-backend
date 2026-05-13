package com.luochen.chenoj.model.dto.stat;

import lombok.Data;

/**
 * 一周内题目提交次数聚合行
 */
@Data
public class QuestionWeekSubmitAggDTO {
    private Long questionId;
    private Long submitCount;
}
