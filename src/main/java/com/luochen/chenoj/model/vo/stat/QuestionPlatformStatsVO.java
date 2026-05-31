package com.luochen.chenoj.model.vo.stat;

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
public class QuestionPlatformStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long totalQuestions;
    private Long totalSubmits;
    private Long totalAccepted;
    /** 全站通过率 0-100 */
    private Integer overallAcceptRatePercent;

    private List<TagCountItem> tagDistribution;
    private List<LanguageCountItem> languageDistribution;
    private List<DailySubmitTrendItem> submitTrendLast7Days;
    private List<QuestionRankItem> topQuestionsBySubmit;
}
