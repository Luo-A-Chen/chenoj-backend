package com.luochen.chenoj.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.luochen.chenoj.mapper.QuestionSubmitMapper;
import com.luochen.chenoj.model.dto.stat.DailySubmitAggDTO;
import com.luochen.chenoj.model.dto.stat.LanguageSubmitAggDTO;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.enums.QuestionSubmitStatusEnum;
import com.luochen.chenoj.model.vo.stat.DailySubmitTrendItem;
import com.luochen.chenoj.model.vo.stat.LanguageCountItem;
import com.luochen.chenoj.model.vo.stat.QuestionPlatformStatsVO;
import com.luochen.chenoj.model.vo.stat.QuestionRankItem;
import com.luochen.chenoj.model.vo.stat.TagCountItem;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.service.QuestionStatsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionStatsServiceImpl implements QuestionStatsService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final int TREND_DAYS = 7;
    private static final int TOP_QUESTION_LIMIT = 10;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Override
    public QuestionPlatformStatsVO getPlatformStats() {
        List<Question> questions = questionService.list(new QueryWrapper<>());

        long totalQuestions = questions.size();
        long totalSubmits = 0L;
        long totalAccepted = 0L;
        Map<String, Long> tagCountMap = new HashMap<>();

        for (Question q : questions) {
            int submit = q.getSubmitNum() == null ? 0 : q.getSubmitNum();
            int accepted = q.getAcceptedNum() == null ? 0 : q.getAcceptedNum();
            totalSubmits += submit;
            totalAccepted += accepted;
            aggregateTags(tagCountMap, q.getTags());
        }

        int overallRate =
                totalSubmits > 0
                        ? (int) Math.round(totalAccepted * 100.0 / totalSubmits)
                        : 0;

        List<TagCountItem> tagDistribution =
                tagCountMap.entrySet().stream()
                        .map(e -> TagCountItem.builder().tag(e.getKey()).count(e.getValue()).build())
                        .sorted(Comparator.comparing(TagCountItem::getCount).reversed())
                        .collect(Collectors.toList());

        List<LanguageCountItem> languageDistribution = buildLanguageDistribution();

        List<DailySubmitTrendItem> submitTrendLast7Days = buildSubmitTrend();

        List<QuestionRankItem> topQuestionsBySubmit = buildTopQuestions(questions);

        return QuestionPlatformStatsVO.builder()
                .totalQuestions(totalQuestions)
                .totalSubmits(totalSubmits)
                .totalAccepted(totalAccepted)
                .overallAcceptRatePercent(overallRate)
                .tagDistribution(tagDistribution)
                .languageDistribution(languageDistribution)
                .submitTrendLast7Days(submitTrendLast7Days)
                .topQuestionsBySubmit(topQuestionsBySubmit)
                .build();
    }

    private void aggregateTags(Map<String, Long> tagCountMap, String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return;
        }
        try {
            List<String> tags = JSONUtil.toList(tagsJson, String.class);
            if (tags == null) {
                return;
            }
            for (String tag : tags) {
                if (StrUtil.isBlank(tag)) {
                    continue;
                }
                tagCountMap.merge(tag.trim(), 1L, Long::sum);
            }
        } catch (Exception ignored) {
            // 忽略非法 tags JSON
        }
    }

    private List<LanguageCountItem> buildLanguageDistribution() {
        List<LanguageSubmitAggDTO> rows = questionSubmitMapper.selectLanguageDistribution();
        if (rows == null) {
            return new ArrayList<>();
        }
        return rows.stream()
                .map(
                        r ->
                                LanguageCountItem.builder()
                                        .language(r.getLanguage())
                                        .count(r.getCount() == null ? 0L : r.getCount())
                                        .build())
                .collect(Collectors.toList());
    }

    private List<DailySubmitTrendItem> buildSubmitTrend() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate startDay = today.minusDays(TREND_DAYS - 1L);
        Date start =
                Date.from(startDay.atStartOfDay(ZONE).toInstant());

        int acStatus = QuestionSubmitStatusEnum.SUCCEED.getValue();
        List<DailySubmitAggDTO> rows =
                questionSubmitMapper.selectDailySubmitTrend(acStatus, start);
        Map<String, DailySubmitAggDTO> rowMap = new HashMap<>();
        if (rows != null) {
            for (DailySubmitAggDTO row : rows) {
                if (row.getDateKey() != null) {
                    rowMap.put(row.getDateKey(), row);
                }
            }
        }

        List<DailySubmitTrendItem> trend = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate day = startDay.plusDays(i);
            String key = day.format(DAY_FMT);
            DailySubmitAggDTO row = rowMap.get(key);
            long submitCount = row == null || row.getSubmitCount() == null ? 0L : row.getSubmitCount();
            long acCount = row == null || row.getAcCount() == null ? 0L : row.getAcCount();
            trend.add(
                    DailySubmitTrendItem.builder()
                            .dateKey(key)
                            .submitCount(submitCount)
                            .acCount(acCount)
                            .build());
        }
        return trend;
    }

    private List<QuestionRankItem> buildTopQuestions(List<Question> questions) {
        return questions.stream()
                .sorted(
                        Comparator.comparingInt(
                                        (Question q) -> q.getSubmitNum() == null ? 0 : q.getSubmitNum())
                                .reversed())
                .limit(TOP_QUESTION_LIMIT)
                .map(this::toRankItem)
                .collect(Collectors.toList());
    }

    private QuestionRankItem toRankItem(Question q) {
        int submit = q.getSubmitNum() == null ? 0 : q.getSubmitNum();
        int accepted = q.getAcceptedNum() == null ? 0 : q.getAcceptedNum();
        int rate = submit > 0 ? (int) Math.round(accepted * 100.0 / submit) : 0;
        return QuestionRankItem.builder()
                .questionId(q.getId())
                .title(q.getTitle())
                .submitNum(submit)
                .acceptedNum(accepted)
                .acceptRatePercent(rate)
                .build();
    }
}
