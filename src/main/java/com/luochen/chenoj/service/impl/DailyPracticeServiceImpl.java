package com.luochen.chenoj.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.enums.QuestionSubmitStatusEnum;
import com.luochen.chenoj.model.vo.DailyPracticeVO;
import com.luochen.chenoj.service.DailyPracticeService;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.service.QuestionSubmitService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DailyPracticeServiceImpl implements DailyPracticeService {

    /** 与业务展示一致：打卡、每日题换日以中国东八区日历为准 */
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter DATE_KEY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Override
    public DailyPracticeVO getDailyPracticeBoard(User loginUserOrNull) {
        LocalDate today = LocalDate.now(ZONE);
        String dateKey = today.format(DATE_KEY_FMT);

        List<Long> allIds = listAllSortedQuestionIds();
        boolean hasQ = !allIds.isEmpty();
        Long dailyQid = hasQ ? pickDailyId(allIds, today) : null;

        String title = "";
        if (dailyQid != null) {
            Question q = questionService.getById(dailyQid);
            title = q == null ? "" : (q.getTitle() == null ? "" : q.getTitle());
        }

        boolean loggedIn = loginUserOrNull != null;
        Long userId = loggedIn ? loginUserOrNull.getId() : null;

        boolean todayDone = Boolean.FALSE;
        int streak = 0;
        if (loggedIn && userId != null && dailyQid != null) {
            todayDone = hasAcceptedOnDay(userId, dailyQid, today);
            streak = computeStreakDays(userId, today, todayDone, allIds);
        }

        return DailyPracticeVO.builder()
                .hasDailyQuestion(hasQ)
                .dailyQuestionId(dailyQid)
                .dailyQuestionTitle(title)
                .todayDailyCompleted(todayDone)
                .streakDays(streak)
                .loggedIn(loggedIn)
                .dateKey(dateKey)
                .build();
    }

    /** 题库 id 升序，确定性每日下标选择 */
    private List<Long> listAllSortedQuestionIds() {
        return questionService.lambdaQuery()
                .select(Question::getId)
                .orderByAsc(Question::getId)
                .list()
                .stream()
                .map(Question::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 使用日期 epoch 与黄金比例常数打散，同一天全站同一题。
     */
    private Long pickDailyId(List<Long> sortedIds, LocalDate day) {
        int n = sortedIds.size();
        if (n == 1) {
            return sortedIds.get(0);
        }
        long seed = day.toEpochDay();
        int idx = (int) Math.floorMod(seed * 1103515245L + n * 0x9E3779B9L, n);
        return sortedIds.get(idx);
    }

    private boolean hasAcceptedOnDay(long userId, long questionId, LocalDate calendarDay) {
        ZonedDateTime zStart = calendarDay.atStartOfDay(ZONE);
        ZonedDateTime zEnd = calendarDay.plusDays(1).atStartOfDay(ZONE);
        Date start = Date.from(zStart.toInstant());
        Date end = Date.from(zEnd.toInstant());

        LambdaQueryChainWrapper<QuestionSubmit> q = questionSubmitService.lambdaQuery()
                .eq(QuestionSubmit::getUserId, userId)
                .eq(QuestionSubmit::getQuestionId, questionId)
                .eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.SUCCEED.getValue())
                .ge(QuestionSubmit::getCreateTime, start)
                .lt(QuestionSubmit::getCreateTime, end);

        return q.count() > 0;
    }

    /**
     * 从「包含今日或未完成的起点日」向后（向过去）数连续完成每日题_assign 的日历日。
     */
    private int computeStreakDays(long userId, LocalDate today, boolean todayDone, List<Long> sortedIds) {
        if (sortedIds.isEmpty()) {
            return 0;
        }
        LocalDate d = todayDone ? today : today.minusDays(1);
        int streak = 0;
        int guard = 0;
        final int maxDays = 4000;

        while (guard++ < maxDays) {
            Long qid = pickDailyId(sortedIds, d);
            if (!hasAcceptedOnDay(userId, qid, d)) {
                break;
            }
            streak++;
            d = d.minusDays(1);
        }
        return streak;
    }
}
