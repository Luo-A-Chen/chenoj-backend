package com.luochen.chenoj.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyHotQuestionItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer rank;
    private Long questionId;
    private String questionTitle;
    private Long submitCount;
}
