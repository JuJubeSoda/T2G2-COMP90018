package org.unimelb.survey.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;

@Data
@TableName("wj_survey_question_option")
public class SurveyQuestionOption implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer questionId;
    private Integer surveyId;
    private String optionContent;
    private Integer orderNum;
    private java.util.Date fcd;
    private java.util.Date lud;
}