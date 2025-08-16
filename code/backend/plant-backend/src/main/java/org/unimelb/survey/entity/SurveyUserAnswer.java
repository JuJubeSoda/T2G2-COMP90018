package org.unimelb.survey.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;

@Data
@TableName("wj_survey_user_answer")
public class SurveyUserAnswer implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer scoreId;
    private Integer surveyId;
    private Integer questionId;
    private String userAnswer;
    private Integer answerValid;
    private java.util.Date fcd;
}