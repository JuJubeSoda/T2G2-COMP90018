package org.unimelb.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.unimelb.survey.entity.SurveyUserScore;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SurveyUserScoreMapper extends BaseMapper<SurveyUserScore> {

    @Select("select * from wj_survey_user_score where survey_id = #{surveyId} order by score desc, exam_duration asc")
    List<SurveyUserScore> getExamRanking(Integer surveyId);
}
