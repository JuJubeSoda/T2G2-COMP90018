package org.unimelb.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.unimelb.survey.entity.SurveyQuestion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SurveyQuestionMapper extends BaseMapper<SurveyQuestion> {

    @Select("select * from wj_survey_question where survey_id=#{sruveyId} and question_type=0 order by order_num")
    public List<SurveyQuestion> getExamineeList(Integer surveyId);

    @Select("select * from wj_survey_question where survey_id=#{sruveyId} and question_type>0 order by order_num")
    public List<SurveyQuestion> getQuestionList(Integer surveyId);

    //@Update("update wj_survey_question set order_num=order_num+#{increment} where survey_id=#{serveyId} and order_num>=#{orderNumStart} and order_num<=#{orderNumEnd}")
    void questionOrderBatchIncr(@Param("serveyId") Integer serveyId,
                                @Param("questionType") Integer questionType,
                                @Param("questionId") Integer questionId,
                                @Param("orderNumStart") Integer orderNumStart,
                                @Param("orderNumEnd") Integer orderNumEnd,
                                @Param("increment") Integer increment);

    @Update("update wj_survey_question set order_num=#{newOrderNum} where id=#{questionId}")
    void updateQuestionOrder(Integer questionId, Integer newOrderNum);

    @Update("update wj_survey_question set correct_answer = #{correctAnswer} where id=#{questionId}")
    void updateCorrectAnswer(Integer questionId, String correctAnswer);

    @Select("select count(1) from wj_survey_question where survey_id = #{surveyId} and content is null")
    Integer countUnqualifiedQuestion(Integer surveyId);

    @Select("delete   from wj_survey_question where survey_id = #{surveyId}")
    void deleteBySurveyId(Integer surveyId);


    public List<SurveyQuestion> getExamineeListForResult(@Param("surveyId") Integer surveyId, @Param("scoreId") Integer scoreId);
    public List<SurveyQuestion> getQuestionListForResult(@Param("surveyId") Integer surveyId, @Param("scoreId") Integer scoreId);
}
