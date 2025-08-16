package org.unimelb.survey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.unimelb.survey.entity.SurveyUserScore;
import org.unimelb.survey.mapper.SurveyUserScoreMapper;
import org.unimelb.survey.service.SurveyUserScoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurveyUserScoreServiceImpl extends ServiceImpl<SurveyUserScoreMapper, SurveyUserScore>  implements SurveyUserScoreService{
    @Override
    public List<SurveyUserScore> getExamRanking(Integer surveyId) {
        return baseMapper.getExamRanking(surveyId);
    }
}