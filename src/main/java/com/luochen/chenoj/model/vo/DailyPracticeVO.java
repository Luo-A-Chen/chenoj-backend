package com.luochen.chenoj.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 首页每日一题 / 打卡看板（全站同一天同一题：按题库 id 与日期的确定性打乱）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPracticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题库是否有可用题目 */
    private Boolean hasDailyQuestion;

    private Long dailyQuestionId;

    private String dailyQuestionTitle;

    /** 当前用户是否在「日历日」内对今日每日题产生过 Accepted 提交 */
    private Boolean todayDailyCompleted;

    /** 已完成「当日对应每日题」的连续日历日数（从今日或未完成的昨日向前回溯） */
    private Integer streakDays;

    /** 是否已登录 */
    private Boolean loggedIn;

    /** 打卡所依据的日历日 yyyy-MM-dd（默认 Asia/Shanghai） */
    private String dateKey;

}
