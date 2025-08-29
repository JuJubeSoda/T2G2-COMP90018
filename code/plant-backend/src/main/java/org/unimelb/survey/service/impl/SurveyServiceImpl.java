package org.unimelb.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.unimelb.survey.entity.Survey;
import org.unimelb.survey.entity.SurveyQuestion;
import org.unimelb.survey.entity.SurveyQuestionOption;
import org.unimelb.survey.mapper.SurveyMapper;
import org.unimelb.survey.service.SurveyQuestionOptionService;
import org.unimelb.survey.service.SurveyQuestionService;
import org.unimelb.survey.service.SurveyService;
import org.unimelb.survey.vo.SurveyQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SurveyServiceImpl extends ServiceImpl<SurveyMapper, Survey> implements SurveyService {
    @Override
    public Page<Survey> getSurveyList(SurveyQuery param) {
        Page<Survey> page = new Page<>(param.getPageNo(),param.getPageSize());

        QueryWrapper<Survey> wrapper = new QueryWrapper<>();
//        wrapper.eq("deleted",0);
        wrapper.lambda().like(StringUtils.hasLength(param.getTitle()),Survey::getTitle,param.getTitle());
        wrapper.lambda().eq(param.getStatus() != null,Survey::getStatus,param.getStatus());
        wrapper.lambda().eq(param.getStar() != null,Survey::getStar,param.getStar());
        if(StringUtils.hasLength(param.getOrderCondition())){
            wrapper.orderBy(StringUtils.hasLength(param.getOrderCondition()),param.isAsc(),param.getOrderColumn());
        }else{
            wrapper.lambda().orderByDesc(Survey::getId);
        }
        baseMapper.selectPage(page,wrapper);
        return page;
    }

    @Autowired
    private SurveyQuestionService surveyQuestionService;
    @Autowired
    private SurveyQuestionOptionService surveyQuestionOptionService;

    @Override
    public boolean validateQuestion(Integer surveyId) {
        boolean valid = false;
        Integer count1 = surveyQuestionService.countUnqualifiedQuestion(surveyId);
        Integer count2 = surveyQuestionOptionService.countUnqualifiedOption(surveyId);
        if(count1 == 0 && count2 == 0){
            valid = true;
        }

        return valid;
    }

    @Override
    public void updateSurveyStatus(Integer surveyId, int status) {
        baseMapper.updateSurveyStatus(surveyId,status);
    }

    @Transactional
    @Override
    public Integer copySurvey(Integer sourceSurveyId) {
        // 问卷
        Survey newSurvey = baseMapper.selectById(sourceSurveyId);
        newSurvey.setId(null);
        newSurvey.setFcd(null);
        newSurvey.setLud(null);
        newSurvey.setStatus(0);
        newSurvey.setTitle(newSurvey.getTitle() + "【复制】");
        baseMapper.insert(newSurvey);
        // 考生信息
        List<SurveyQuestion> examineeList = surveyQuestionService.getExamineeList(sourceSurveyId);
        examineeList.forEach(examinee -> {
            examinee.setId(null);
            examinee.setSurveyId(newSurvey.getId());
            examinee.setFcd(null);
            examinee.setLud(null);
            surveyQuestionService.addSurveyQuestion(examinee);
        });
        // 试题信息
        List<SurveyQuestion> questionList = surveyQuestionService.getQuestionList(sourceSurveyId);
        questionList.forEach(question -> {
            String sourceCorrentAnswer = question.getCorrectAnswer();
            question.setId(null);
            question.setSurveyId(newSurvey.getId());
            question.setFcd(null);
            question.setLud(null);
            question.setCorrectAnswer("");
            surveyQuestionService.save(question);
            // 选项信息
            List<SurveyQuestionOption> answerOptions = question.getAnswerOptions();
            List<String> answerList = new ArrayList<>();
            List<String> sourceAnswerList = Arrays.asList(sourceCorrentAnswer.split(","));
            if(answerOptions != null){
                answerOptions.forEach(option -> {
                    Integer sourceOptionId = option.getId();
                    option.setQuestionId(question.getId());
                    option.setSurveyId(newSurvey.getId());
                    option.setId(null);
                    option.setFcd(null);
                    option.setLud(null);

                    surveyQuestionOptionService.save(option);

                    log.debug(sourceAnswerList+ " ------ "+sourceOptionId);
                    if(sourceAnswerList.contains(sourceOptionId+"")){
                        answerList.add(option.getId()+"");
                    }
                });
            }
            log.debug("answerList:::"+answerList);
            if(answerList.size()>0){

                String answer = String.join(",",answerList);
                surveyQuestionService.updateCorrectAnswer(question.getId(),answer);
            }
        });

        return newSurvey.getId();
    }


    @Override
    public void deleteSurveyToRecycle(Integer id) {
        baseMapper.deleteSurveyToRecycle(id);
    }


    @Override
    public List<Survey> getRecycleList(Integer userId) {
        return baseMapper.getRecycleList(userId);
    }

    @Override
    public void setSurveyStar(Integer surveyId) {
        baseMapper.setSurveyStar(surveyId);
    }

    @Override
    public void restoreSurvey(Integer surveyId) {
        baseMapper.restoreSurvey(surveyId);
    }

    @Transactional
    @Override
    public void deleteSurvey(Integer surveyId) {
        baseMapper.deleteById(surveyId);
        surveyQuestionService.deleteBySurveyId(surveyId);
        surveyQuestionOptionService.deleteBySurveyId(surveyId);
    }

    @Override
    public Integer publishSurvey(Integer surveyId) {
        baseMapper.updateSurveyStatus(surveyId,2);
        return 2;
    }

    @Override
    public Integer stopSurvey(Integer surveyId) {
        baseMapper.updateSurveyStatus(surveyId,1);
        return 1;
    }
}
