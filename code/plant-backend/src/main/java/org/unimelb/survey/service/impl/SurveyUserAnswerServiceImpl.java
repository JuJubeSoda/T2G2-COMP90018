package org.unimelb.survey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.unimelb.survey.entity.SurveyQuestion;
import org.unimelb.survey.entity.SurveyUserAnswer;
import org.unimelb.survey.entity.SurveyUserScore;
import org.unimelb.survey.mapper.SurveyMapper;
import org.unimelb.survey.mapper.SurveyUserAnswerMapper;
import org.unimelb.survey.mapper.SurveyUserScoreMapper;
import org.unimelb.survey.service.SurveyQuestionService;
import org.unimelb.survey.service.SurveyUserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SurveyUserAnswerServiceImpl extends ServiceImpl<SurveyUserAnswerMapper, SurveyUserAnswer> implements SurveyUserAnswerService {

    @Autowired
    private SurveyQuestionService surveyQuestionService;

    @Autowired
    private SurveyUserScoreMapper surveyUserScoreMapper;

    @Autowired
    private SurveyMapper surveyMapper;

    @Transactional
    @Override
    public Integer submitExam(List<SurveyUserAnswer> answers, Integer examDuration) {
        // 新增分数记录，先新增的目的是获取id
        SurveyUserScore surveyUserScore = new SurveyUserScore();
        surveyUserScore.setExamDuration(examDuration);
        surveyUserScoreMapper.insert(surveyUserScore);

        // 批改
        Integer totalScore = 0;  // 得分
        Integer questionNum = 0; // 问题数
        Integer correctNum = 0;  // 答对数
        String username = "匿名";
        Integer surveyId = null;
        Integer surveyTotal = 0;
        for (SurveyUserAnswer answer:answers) {

            if(surveyId == null) {
                surveyId = answer.getSurveyId();
            }

            answer.setScoreId(surveyUserScore.getId());
            SurveyQuestion question = surveyQuestionService.getById(answer.getQuestionId());

            if(question.getQuestionType() > 0){
                questionNum++;
                surveyTotal += question.getScore();
            }

            if(question.getQuestionType() == 0 && question.getContent().equals("姓名") && StringUtils.hasLength(answer.getUserAnswer())){
                username = answer.getUserAnswer();
            }
            if(question.getQuestionType()>0 && question.getCorrectAnswer().equals(answer.getUserAnswer())){
                totalScore += question.getScore();
                correctNum++;
                answer.setAnswerValid(1);
            }
        }

        // 更新分数
        surveyUserScore.setSurveyId(surveyId);
        surveyUserScore.setScore(totalScore);
        surveyUserScore.setSurveyScore(surveyTotal);
        surveyUserScore.setUserName(username);
        surveyUserScore.setCorrectNum(correctNum);
        surveyUserScore.setQuestionNumber(questionNum);
        surveyUserScoreMapper.updateById(surveyUserScore);

        // 记录答案
        this.saveBatch(answers);

        // 更新答卷数
        surveyMapper.updateaAnswerTotal(surveyId);


        return surveyUserScore.getId();
    }
}
