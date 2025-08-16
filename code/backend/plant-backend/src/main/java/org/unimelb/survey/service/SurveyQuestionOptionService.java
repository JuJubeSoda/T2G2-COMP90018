package org.unimelb.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.survey.entity.SurveyQuestionOption;

import java.util.List;

public interface SurveyQuestionOptionService extends IService<SurveyQuestionOption> {
    List<SurveyQuestionOption> getOptionsByQuestionId(Integer id);

    void addSurveyQuestionOption(SurveyQuestionOption surveyQuestionOption);

    void deleteSurveyQuestionOptionById(Integer optionId);

    Integer countUnqualifiedOption(Integer surveyId);

    void deleteBySurveyId(Integer surveyId);
    void deleteByQuestionId(Integer questionId);
}
