package org.unimelb.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.unimelb.survey.entity.SurveyQuestionOption;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SurveyQuestionOptionMapper extends BaseMapper<SurveyQuestionOption> {
    @Select("select * from  wj_survey_question_option where question_id = #{questionId} order by order_num")
    List<SurveyQuestionOption> getOptionsByQuestionId(Integer questionId);

    @Update("update wj_survey_question_option set order_num=order_num+#{increment} where question_id = #{questionId} and order_num>=#{orderNum}")
    void updateOptionOrder(Integer questionId, Integer orderNum, Integer increment);

    Integer countUnqualifiedOption(Integer surveyId);

    @Delete("delete  from  wj_survey_question_option where survey_id = #{surveyId}")
    void deleteBySurveyId(Integer surveyId);

    @Delete("delete  from  wj_survey_question_option where question_id = #{questionId}")
    void deleteByQuestionId(Integer questionId);
}
