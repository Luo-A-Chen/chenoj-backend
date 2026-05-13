package com.luochen.chenoj.mapper;

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
            "SELECT user_id AS userId, COUNT(*) AS acCount FROM question_submit WHERE is_delete = 0 "
                    + "AND status = #{acStatus} AND create_time >= #{start} AND create_time < #{end} "
                    + "GROUP BY user_id ORDER BY COUNT(*) DESC LIMIT #{limit}"
    )
    List<UserWeekAcAggDTO> selectWeeklyUserAcRanking(
            @Param("acStatus") int acStatus,
            @Param("start") Date start,
            @Param("end") Date end,
            @Param("limit") int limit);

    @Select(
            "SELECT question_id AS questionId, COUNT(*) AS submitCount FROM question_submit WHERE is_delete = 0 "
                    + "AND create_time >= #{start} AND create_time < #{end} GROUP BY question_id "
                    + "ORDER BY COUNT(*) DESC LIMIT #{limit}"
    )
    List<QuestionWeekSubmitAggDTO> selectWeeklyHotQuestions(
            @Param("start") Date start,
            @Param("end") Date end,
            @Param("limit") int limit);
}




