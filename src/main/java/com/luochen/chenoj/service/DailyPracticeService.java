package com.luochen.chenoj.service;

import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.vo.DailyPracticeVO;

/**
 * 每日一题 / 做题打卡
 */
public interface DailyPracticeService {

    DailyPracticeVO getDailyPracticeBoard(User loginUserOrNull);
}
