package com.luochen.chenoj.service.impl;

import com.luochen.chenoj.mapper.QuestionSubmitMapper;
import com.luochen.chenoj.model.dto.stat.QuestionWeekSubmitAggDTO;
import com.luochen.chenoj.model.dto.stat.UserWeekAcAggDTO;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.enums.QuestionSubmitStatusEnum;
import com.luochen.chenoj.model.vo.WeeklyBoardVO;
import com.luochen.chenoj.model.vo.WeeklyHotQuestionItem;
import com.luochen.chenoj.model.vo.WeeklyUserRankItem;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.service.UserService;
import com.luochen.chenoj.service.WeeklyStatsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeeklyStatsServiceImpl implements WeeklyStatsService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    /** 用户数排名条数（左侧榜） */
    private static final int TOP_USER_RANK = 10;

    /** 热题 TOP N */
    private static final int TOP_HOT_QUESTION = 3;

    private static final DateTimeFormatter LABEL_FMT = DateTimeFormatter.ofPattern("MM/dd");

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Override
    public WeeklyBoardVO getWeeklyBoard() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate weekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekSunday = weekMonday.plusDays(6);
        ZonedDateTime weekStart = weekMonday.atStartOfDay(ZONE);
        ZonedDateTime weekExclusiveEnd = weekMonday.plusWeeks(1).atStartOfDay(ZONE);
        Date start = Date.from(weekStart.toInstant());
        Date end = Date.from(weekExclusiveEnd.toInstant());

        String weekLabel =
                String.format(
                        "本周（东八区 · 周一至周日 · 每周重置）· %s - %s",
                        weekMonday.format(LABEL_FMT),
                        weekSunday.format(LABEL_FMT));

        int succeed = QuestionSubmitStatusEnum.SUCCEED.getValue();

        List<UserWeekAcAggDTO> acRows =
                questionSubmitMapper.selectWeeklyUserAcRanking(succeed, start, end, TOP_USER_RANK);
        List<WeeklyUserRankItem> userRank = buildUserRank(acRows);

        List<QuestionWeekSubmitAggDTO> hotRows =
                questionSubmitMapper.selectWeeklyHotQuestions(start, end, TOP_HOT_QUESTION);
        List<WeeklyHotQuestionItem> hotQuestions = buildHotQuestions(hotRows);

        return WeeklyBoardVO.builder()
                .weekLabel(weekLabel)
                .userAcRank(userRank)
                .hotQuestions(hotQuestions)
                .build();
    }

    private List<WeeklyUserRankItem> buildUserRank(List<UserWeekAcAggDTO> rows) {
        List<WeeklyUserRankItem> out = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return out;
        }
        List<Long> ids =
                rows.stream().map(UserWeekAcAggDTO::getUserId).filter(o -> o != null).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(ids).stream().collect(Collectors.toMap(User::getId, u -> u));

        int r = 1;
        for (UserWeekAcAggDTO row : rows) {
            Long uid = row.getUserId();
            if (uid == null) {
                continue;
            }
            User u = userMap.get(uid);
            String name = uid.toString();
            if (u != null) {
                if (StringUtils.isNotBlank(u.getUserName())) {
                    name = u.getUserName();
                } else if (StringUtils.isNotBlank(u.getUserAccount())) {
                    name = u.getUserAccount();
                }
            }
            WeeklyUserRankItem item =
                    WeeklyUserRankItem.builder()
                            .rank(r++)
                            .userId(uid)
                            .userName(name)
                            .acCount(row.getAcCount())
                            .build();
            out.add(item);
        }
        return out;
    }

    private List<WeeklyHotQuestionItem> buildHotQuestions(List<QuestionWeekSubmitAggDTO> rows) {
        List<WeeklyHotQuestionItem> out = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return out;
        }
        List<Long> qids =
                rows.stream()
                        .map(QuestionWeekSubmitAggDTO::getQuestionId)
                        .filter(q -> q != null)
                        .distinct()
                        .collect(Collectors.toList());
        Map<Long, Question> qmap = questionService.listByIds(qids).stream().collect(Collectors.toMap(Question::getId, q -> q));

        int r = 1;
        for (QuestionWeekSubmitAggDTO row : rows) {
            Long qid = row.getQuestionId();
            if (qid == null) {
                continue;
            }
            Question q = qmap.get(qid);
            String title = q != null && StringUtils.isNotBlank(q.getTitle()) ? q.getTitle() : ("题目#" + qid);
            WeeklyHotQuestionItem item =
                    WeeklyHotQuestionItem.builder()
                            .rank(r++)
                            .questionId(qid)
                            .questionTitle(title)
                            .submitCount(row.getSubmitCount())
                            .build();
            out.add(item);
        }
        return out;
    }
}
