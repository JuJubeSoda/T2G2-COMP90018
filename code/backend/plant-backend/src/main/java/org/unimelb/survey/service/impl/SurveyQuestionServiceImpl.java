package org.unimelb.survey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.unimelb.survey.entity.SurveyQuestion;
import org.unimelb.survey.entity.SurveyQuestionOption;
import org.unimelb.survey.mapper.SurveyQuestionMapper;
import org.unimelb.survey.service.SurveyQuestionOptionService;
import org.unimelb.survey.service.SurveyQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyQuestionServiceImpl extends ServiceImpl<SurveyQuestionMapper, SurveyQuestion> implements SurveyQuestionService {

    @Override
    public List<SurveyQuestion> getExamineeList(Integer surveyId) {
        return baseMapper.getExamineeList(surveyId);
    }



    @Transactional
    @Override
    public void updateQuestionOrder(Integer surveyId,Integer questionId,Integer oldOrderNum, Integer newOrderNum) {
        int orderNumStart = newOrderNum < oldOrderNum ? newOrderNum : oldOrderNum;
        int orderNumEnd = newOrderNum < oldOrderNum ? oldOrderNum : newOrderNum;
        int increment = newOrderNum < oldOrderNum ? 1 : -1;
        SurveyQuestion surveyQuestion = baseMapper.selectById(questionId);
        baseMapper.questionOrderBatchIncr(surveyId,surveyQuestion.getQuestionType(),questionId,orderNumStart,orderNumEnd,increment);
        baseMapper.updateQuestionOrder(questionId,newOrderNum);
    }


    @Autowired
    private SurveyQuestionOptionService surveyQuestionOptionService;

    @Transactional
    @Override
    public void deleteSurveyQuestion(Integer questionId) {
        SurveyQuestion surveyQuestion = baseMapper.selectById(questionId);
        baseMapper.questionOrderBatchIncr(surveyQuestion.getSurveyId(),surveyQuestion.getQuestionType(),questionId,surveyQuestion.getOrderNum(),99,-1);
        baseMapper.deleteById(questionId);
        surveyQuestionOptionService.deleteByQuestionId(questionId);
    }


    @Autowired
    private SurveyQuestionOptionService questionOptionService;

    @Override
    public List<SurveyQuestion> getQuestionList(Integer surveyId) {
        List<SurveyQuestion> list = baseMapper.getQuestionList(surveyId);
        return list.stream().map(item -> {
            List<SurveyQuestionOption> options = questionOptionService.getOptionsByQuestionId(item.getId());
            item.setAnswerOptions(options);
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public void addSurveyQuestion(SurveyQuestion surveyQuestion) {
        // 1. 新增题目
        log.debug("----> " + surveyQuestion);
        baseMapper.insert(surveyQuestion);
        // 2. 如果是单选或多选，默认新增4个选项
        if(surveyQuestion.getQuestionType()==1 || surveyQuestion.getQuestionType()==2){
            for (int i = 0; i < 4 ; i++) {
                SurveyQuestionOption option = new SurveyQuestionOption();
                option.setQuestionId(surveyQuestion.getId());
                option.setOrderNum(i);
                option.setSurveyId(surveyQuestion.getSurveyId());
                questionOptionService.save(option);
                surveyQuestion.getAnswerOptions().add(option);
            }
        }
    }

    @Override
    public void setCorrectAnswer(Integer questionId, String correctAnswer) {
        baseMapper.updateCorrectAnswer(questionId,correctAnswer);
    }

    @Override
    public Integer countUnqualifiedQuestion(Integer surveyId) {
        return baseMapper.countUnqualifiedQuestion(surveyId);
    }

    @Override
    public void deleteBySurveyId(Integer surveyId) {
        baseMapper.deleteBySurveyId(surveyId);
    }

    @Override
    public List<SurveyQuestion> getQuestionListForExam(Integer surveyId) {
        List<SurveyQuestion> list = baseMapper.getQuestionList(surveyId);
        return list.stream().map(item -> {
            item.setCorrectAnswer(null);
            List<SurveyQuestionOption> options = questionOptionService.getOptionsByQuestionId(item.getId());
            item.setAnswerOptions(options);
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SurveyQuestion> getExamineeListForResult(Integer surveyId, Integer scoreId) {
        return baseMapper.getExamineeListForResult(surveyId,scoreId);
    }

    @Override
    public List<SurveyQuestion> getQuestionListForResult(Integer surveyId, Integer scoreId) {

        return baseMapper.getQuestionListForResult(surveyId,scoreId).stream().map(item -> {
            List<SurveyQuestionOption> options = questionOptionService.getOptionsByQuestionId(item.getId());
            item.setAnswerOptions(options);
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateCorrectAnswer(Integer id, String answer) {
        baseMapper.updateCorrectAnswer(id,answer);
    }
}
