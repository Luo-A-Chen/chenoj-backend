package com.luochen.chenoj.model.vo.stat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRankItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long questionId;
    private String title;
    private Integer submitNum;
    private Integer acceptedNum;
    /** 通过率 0-100，无提交时为 0 */
    private Integer acceptRatePercent;
}
