package com.luochen.chenoj.service;

import com.luochen.chenoj.model.vo.WeeklyBoardVO;

/**
 * 自然周口径统计（东八区，周一至次日周一 0 点为一周；「清空」语义为到新一周数据自然不包含上周）
 */
public interface WeeklyStatsService {

    WeeklyBoardVO getWeeklyBoard();
}
