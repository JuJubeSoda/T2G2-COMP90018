package org.unimelb.survey.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;

@Data
@TableName("wj_survey_user_score")
public class SurveyUserScore implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String userName;
    private Integer surveyId;
    private Integer score;
    private Integer examDuration;
    private Integer surveyScore;
    private Integer questionNumber;
    private Integer correctNum;
    private java.util.Date fcd;
}