package org.unimelb.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.survey.entity.SurveyUserAnswer;

import java.util.List;

public interface SurveyUserAnswerService extends IService<SurveyUserAnswer> {

    Integer submitExam(List<SurveyUserAnswer> answers, Integer examTime);
}
