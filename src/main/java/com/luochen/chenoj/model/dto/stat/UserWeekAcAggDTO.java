package com.luochen.chenoj.model.dto.stat;

import lombok.Data;

/**
 * 一周内用户 AC 次数聚合行
 */
@Data
public class UserWeekAcAggDTO {
    private Long userId;
    private Long acCount;
}
