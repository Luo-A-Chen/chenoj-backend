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
public class DailySubmitTrendItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** yyyy-MM-dd */
    private String dateKey;
    private Long submitCount;
    private Long acCount;
}
