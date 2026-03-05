package com.luochen.chenoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luochen.chenoj.model.dto.question.JudgeConfig;
import com.luochen.chenoj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目封装类
 *
 * @TableName question
 */
@TableName(value = "question")
@Data
public class QuestionVO implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     * 这里显示为list形式是因为需要将数据展示给前端
     */
    private List<String> tagList;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建题目人的信息
     */
    private UserVO userVO;

    private static final long serialVersionUID = 1L;

    //两个方法的作用，将前端传来的vo对象转换成数据库操作的question实体

    /**
     * 包装类转对象
     * 用户修改题目时，前端传来vo对象，需要转换成entity才能保存到数据库
     * java代码和前端交互时，需要将json字符串转换成对象或者list
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);//拷贝同名属性
        List<String> tagList = questionVO.getTagList();
        if(tagList != null){
            //对传来的list进行转换，并且序列化成json存储到数据库中
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        JudgeConfig judgeConfigVO = questionVO.getJudgeConfig();
        if(judgeConfigVO != null){
            //对传来的对象进行转换，并且序列化成json存储到数据库中
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfigVO));
        }
        return question;
    }

    /**
     * 对象转包装类
     * 查询题目列表时，从数据库查到entity，要转成vo脱敏才能返回给前端
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);//拷贝同名属性
        //将数据库存储的json转换成list，并且赋值给vo对象
        List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
        questionVO.setTagList(tagList);
        //将数据库存储的json转换成对象，并且赋值给vo对象
        String judgeConfigStr = question.getJudgeConfig();
        questionVO.setJudgeConfig(JSONUtil.toBean(judgeConfigStr, JudgeConfig.class));
        return questionVO;
    }

}