package com.luochen.chenoj.model.dto.stat;

import lombok.Data;

@Data
public class DailySubmitAggDTO {
    private String dateKey;
    private Long submitCount;
    private Long acCount;
}
