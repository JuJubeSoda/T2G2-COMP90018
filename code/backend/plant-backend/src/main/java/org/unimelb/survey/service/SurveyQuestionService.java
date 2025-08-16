package org.unimelb.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.survey.entity.SurveyQuestion;

import java.util.List;

public interface SurveyQuestionService extends IService<SurveyQuestion> {
    public List<SurveyQuestion> getExamineeList(Integer surveyId);

    public List<SurveyQuestion> getQuestionList(Integer surveyId);

    void updateQuestionOrder(Integer surveyId,Integer questionId,Integer oldOrderNum, Integer newOrderNum);

    void deleteSurveyQuestion(Integer questionId);

    void addSurveyQuestion(SurveyQuestion surveyQuestion);

    void setCorrectAnswer(Integer questionId, String correctAnswer);

    Integer countUnqualifiedQuestion(Integer id);

    void deleteBySurveyId(Integer surveyId);

    List<SurveyQuestion> getQuestionListForExam(Integer surveyId);

    List<SurveyQuestion> getExamineeListForResult(Integer surveyId,Integer scoreId);
    List<SurveyQuestion> getQuestionListForResult(Integer surveyId,Integer scoreId);

    void updateCorrectAnswer(Integer id, String answer);
}
