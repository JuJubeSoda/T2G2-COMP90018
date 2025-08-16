package org.unimelb.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.survey.entity.Survey;
import org.unimelb.survey.vo.SurveyQuery;

import java.util.List;

public interface SurveyService extends IService<Survey> {
    Page<Survey> getSurveyList(SurveyQuery param);

    boolean validateQuestion(Integer id);

    void updateSurveyStatus(Integer surveyId, int status);

    Integer copySurvey(Integer sourceSurveyId);

    void deleteSurveyToRecycle(Integer id);

    List<Survey> getRecycleList(Integer userId);

    void setSurveyStar(Integer surveyId);

    void restoreSurvey(Integer surveyId);

    void deleteSurvey(Integer id);

    Integer publishSurvey(Integer surveyId);

    Integer stopSurvey(Integer surveyId);
}
