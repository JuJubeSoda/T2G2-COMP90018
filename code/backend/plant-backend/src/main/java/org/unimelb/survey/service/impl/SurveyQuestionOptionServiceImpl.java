package org.unimelb.survey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.unimelb.survey.entity.SurveyQuestionOption;
import org.unimelb.survey.mapper.SurveyQuestionOptionMapper;
import org.unimelb.survey.service.SurveyQuestionOptionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurveyQuestionOptionServiceImpl extends ServiceImpl<SurveyQuestionOptionMapper, SurveyQuestionOption> implements SurveyQuestionOptionService {
    @Override
    public List<SurveyQuestionOption> getOptionsByQuestionId(Integer questionId) {
        return baseMapper.getOptionsByQuestionId(questionId);
    }

    @Override
    public void addSurveyQuestionOption(SurveyQuestionOption surveyQuestionOption) {
        // 更新排序
        baseMapper.updateOptionOrder(surveyQuestionOption.getQuestionId(),surveyQuestionOption.getOrderNum(),1);
        // 新增选项
        baseMapper.insert(surveyQuestionOption);

    }

    @Override
    public void deleteSurveyQuestionOptionById(Integer optionId) {
        // 更新排序
        SurveyQuestionOption option = baseMapper.selectById(optionId);
        baseMapper.updateOptionOrder(option.getQuestionId(),option.getOrderNum(),-1);
        // 新增选项
        baseMapper.deleteById(optionId);
    }

    @Override
    public Integer countUnqualifiedOption(Integer surveyId) {
        return baseMapper.countUnqualifiedOption(surveyId);
    }

    @Override
    public void deleteBySurveyId(Integer surveyId) {
        baseMapper.deleteBySurveyId(surveyId);
    }

    @Override
    public void deleteByQuestionId(Integer questionId) {
        baseMapper.deleteByQuestionId(questionId);
    }
}
