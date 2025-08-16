package org.unimelb.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.survey.entity.SurveyUserScore;

import java.util.List;

public interface SurveyUserScoreService extends IService<SurveyUserScore> {

    List<SurveyUserScore> getExamRanking(Integer surveyId);
}
