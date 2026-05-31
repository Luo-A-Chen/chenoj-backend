package com.luochen.chenoj.mapper;

import com.luochen.chenoj.model.dto.stat.DailySubmitAggDTO;
import com.luochen.chenoj.model.dto.stat.LanguageSubmitAggDTO;
import com.luochen.chenoj.model.dto.stat.QuestionWeekSubmitAggDTO;
import com.luochen.chenoj.model.dto.stat.UserWeekAcAggDTO;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author luochen
* @description 针对表【question_submit(题目提交)】的数据库操作Mapper
* @createDate 2026-03-04 20:07:44
* @Entity com.luochen.chenoj.model.entity.QuestionSubmit
*/
public interface QuestionSubmitMapper extends BaseMapper<QuestionSubmit> {

    @Select(
            "SELECT userId AS userId, COUNT(*) AS acCount FROM question_submit WHERE isDelete = 0 "
                    + "AND status = #{acStatus} AND createTime >= #{start} AND createTime < #{end} "
                    + "GROUP BY userId ORDER BY COUNT(*) DESC LIMIT #{limit}"
    )
    List<UserWeekAcAggDTO> selectWeeklyUserAcRanking(
            @Param("acStatus") int acStatus,
            @Param("start") Date start,
            @Param("end") Date end,
            @Param("limit") int limit);

    @Select(
            "SELECT questionId AS questionId, COUNT(*) AS submitCount FROM question_submit WHERE isDelete = 0 "
                    + "AND createTime >= #{start} AND createTime < #{end} GROUP BY questionId "
                    + "ORDER BY COUNT(*) DESC LIMIT #{limit}"
    )
    List<QuestionWeekSubmitAggDTO> selectWeeklyHotQuestions(
            @Param("start") Date start,
            @Param("end") Date end,
            @Param("limit") int limit);

    @Select(
            "SELECT DATE_FORMAT(createTime, '%Y-%m-%d') AS dateKey, COUNT(*) AS submitCount, "
                    + "SUM(CASE WHEN status = #{acStatus} THEN 1 ELSE 0 END) AS acCount "
                    + "FROM question_submit WHERE isDelete = 0 AND createTime >= #{start} "
                    + "GROUP BY DATE_FORMAT(createTime, '%Y-%m-%d') ORDER BY dateKey")
    List<DailySubmitAggDTO> selectDailySubmitTrend(
            @Param("acStatus") int acStatus, @Param("start") Date start);

    @Select(
            "SELECT language AS language, COUNT(*) AS count FROM question_submit "
                    + "WHERE isDelete = 0 AND language IS NOT NULL AND language <> '' "
                    + "GROUP BY language ORDER BY COUNT(*) DESC")
    List<LanguageSubmitAggDTO> selectLanguageDistribution();
}




