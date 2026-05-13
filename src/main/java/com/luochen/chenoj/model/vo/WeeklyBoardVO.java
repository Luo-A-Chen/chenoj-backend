package com.luochen.chenoj.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyBoardVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 展示文案，如：本周（东八区 · 周一计）· 05/12 - 05/18 */
    private String weekLabel;

    private List<WeeklyUserRankItem> userAcRank;

    private List<WeeklyHotQuestionItem> hotQuestions;
}
