package org.unimelb.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.unimelb.common.vo.Result;
import org.unimelb.survey.entity.*;
import org.unimelb.survey.service.*;
import org.unimelb.survey.entity.*;
import org.unimelb.survey.service.*;
import org.unimelb.survey.vo.SurveyQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "survey", description = "survey api")
@RestController
@RequestMapping("/survey")
@Slf4j
public class SurveyController {

    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyQuestionService surveyQuestionService;

    @Operation(summary = "create survey")
    @PostMapping
    public Result<?> addSurvey(@RequestBody Survey survey) {
        surveyService.save(survey);
        return Result.success(survey);
    }

    @Operation(summary = "update survey")
    @PutMapping
    public Result<?> updateSurvey(@RequestBody Survey survey) {
        survey.setLud(new Date());
        surveyService.updateById(survey);

        return Result.success(survey);
    }

    @Operation(summary = "complete survey")
    @PutMapping("/finish")
    public Result<?> finishEditSurvey(@RequestBody Survey survey) {
        survey.setLud(new Date());
        surveyService.updateById(survey);
        if (!surveyService.validateQuestion(survey.getId())) {
            return Result.fail(210, "请完善问卷试题或选项");
        } else {
            surveyService.updateSurveyStatus(survey.getId(), 1); // 更新状态为已就绪
        }

        return Result.success(survey);
    }

    @Operation(summary = "query survey by id")
    @GetMapping("/{id}")
    public Result<?> getSurveyById(@PathVariable("id") Integer id) {
        Survey survey = surveyService.getById(id);
        survey.setExamineeList(surveyQuestionService.getExamineeList(id));
        survey.setQuestionList(surveyQuestionService.getQuestionList(id));
        return Result.success(survey);
    }


    @Operation(summary = "query questions list by survey id")
    @GetMapping("/question/list/{surveyId}")
    public Result<?> getSurveyQuestionList(@PathVariable("surveyId") Integer surveyId) {
        List<SurveyQuestion> list = surveyQuestionService.getQuestionList(surveyId);
        return Result.success(list);
    }

    @Operation(summary = "get survey by examinee list")
    @GetMapping("/examinee/list/{surveyId}")
    public Result<?> getSurveyExamineeList(@PathVariable("surveyId") Integer surveyId) {
        List<SurveyQuestion> list = surveyQuestionService.getExamineeList(surveyId);
        return Result.success(list);
    }

    @Operation(summary = "delete question")
    @DeleteMapping("/question/{questionId}")
    public Result<?> deleteSurveyQuestion(@PathVariable("questionId") Integer questionId) {
        surveyQuestionService.deleteSurveyQuestion(questionId);
        return Result.success();
    }

    @Operation(summary = "update question order")
    @PutMapping("/question/order")
    public Result<?> updateQuestionOrder(@RequestParam("surveyId") Integer surveyId,
                                         @RequestParam("questionId") Integer questionId,
                                         @RequestParam("oldOrderNum") Integer oldOrderNum,
                                         @RequestParam("newOrderNum") Integer newOrderNum) {
        surveyQuestionService.updateQuestionOrder(surveyId, questionId, oldOrderNum, newOrderNum);
        return Result.success();
    }


    @Operation(summary = "create survey question")
    @PostMapping("/question")
    public Result<?> addSurveyQuestion(@RequestBody SurveyQuestion surveyQuestion) {
        surveyQuestionService.addSurveyQuestion(surveyQuestion);
        surveyService.updateSurveyStatus(surveyQuestion.getSurveyId(), 0); // 更新状态为编辑中
        return Result.success(surveyQuestion);
    }

    @Operation(summary = "query question by question id")
    @GetMapping("/question/{questionId}")
    public Result<?> getSurveyQuestionById(@PathVariable("questionId") Integer questionId) {
        SurveyQuestion surveyQuestion = surveyQuestionService.getById(questionId);
        surveyQuestion.setAnswerOptions(SurveyQuestionOptionService.getOptionsByQuestionId(questionId));
        return Result.success(surveyQuestion);
    }

    @Autowired
    private org.unimelb.survey.service.SurveyQuestionOptionService SurveyQuestionOptionService;

    @Operation(summary = "update options")
    @PutMapping("/question/option")
    public Result<?> updateSurveyQuestionOption(@RequestBody SurveyQuestionOption surveyQuestionOption) {
        surveyQuestionOption.setLud(new Date());
        SurveyQuestionOptionService.updateById(surveyQuestionOption);
        return Result.success();
    }

    @Operation(summary = "create options")
    @PostMapping("/question/option")
    public Result<?> addSurveyQuestionOption(@RequestBody SurveyQuestionOption surveyQuestionOption) {
        SurveyQuestionOptionService.addSurveyQuestionOption(surveyQuestionOption);
        return Result.success(surveyQuestionOption);
    }


    @Operation(summary = "query question option by question id")
    @GetMapping("/question/option/{questionId}")
    public Result<?> getSurveyQuestionOptionById(@PathVariable("questionId") Integer questionId) {
        List<SurveyQuestionOption> options = SurveyQuestionOptionService.getOptionsByQuestionId(questionId);
        return Result.success(options);
    }

    @Operation(summary = "根据id删除试题选项")
    @DeleteMapping("/question/option/{optionId}")
    public Result<?> deleteSurveyQuestionOptionById(@PathVariable("optionId") Integer optionId) {
        SurveyQuestionOptionService.deleteSurveyQuestionOptionById(optionId);
        return Result.success();
    }

    @Operation(summary = "设置正确答案")
    @PutMapping("/question/correct")
    public Result<?> setCorrectAnswer(@RequestParam("questionId") Integer questionId,
                                      @RequestParam("correctAnswer") String correctAnswer) {
        surveyQuestionService.setCorrectAnswer(questionId, correctAnswer);
        return Result.success();
    }

    @Operation(summary = "更新试题")
    @PutMapping("/question")
    public Result<?> updateSurveyQuestion(@RequestBody SurveyQuestion surveyQuestion) {
        surveyQuestionService.updateById(surveyQuestion);
        return Result.success(surveyQuestion);
    }

    @Operation(summary = "分页查询查询问卷")
    @GetMapping("/list")
    public Result<Map<String, Object>> getSurveyList(SurveyQuery param) {
        Page<Survey> page = surveyService.getSurveyList(param);

        Map<String, Object> data = Map.of(
                "total", page.getTotal(),
                "rows", page.getRecords()
        );
        return Result.success(data);
    }


    @Operation(summary = "复制问卷")
    @GetMapping("/copy")
    public Result<Integer> copySurvey(@RequestParam("sourceSurveyId") Integer sourceSurveyId) {
        Integer newSurveyId = surveyService.copySurvey(sourceSurveyId);
        return Result.success(newSurveyId);
    }

    @Operation(summary = "逻辑删除问卷")
    @DeleteMapping("/{id}")
    public Result<?> deleteSurveyToRecycle(@PathVariable("id") Integer id) {
        surveyService.deleteSurveyToRecycle(id);
        return Result.success();
    }

    @Operation(summary = "物理删除问卷")
    @DeleteMapping("/physical/{id}")
    public Result<?> deleteSurvey(@PathVariable("id") Integer id) {
        surveyService.deleteSurvey(id);
        return Result.success();
    }

    @Operation(summary = "回收站恢复问卷")
    @PutMapping("/restore/{id}")
    public Result<?> restoreSurvey(@PathVariable("id") Integer id) {
        surveyService.restoreSurvey(id);
        return Result.success();
    }

    @Operation(summary = "回收站问卷列表")
    @GetMapping("/recycle/list")
    public Result<?> getRecycleList(@RequestParam("userId") Integer userId) {
        List<Survey> recycleList = surveyService.getRecycleList(userId);
        return Result.success(recycleList);
    }

    @Operation(summary = "设置星标")
    @PutMapping("/star")
    public Result<?> setSurveyStar(@RequestParam("surveyId") Integer surveyId) {
        surveyService.setSurveyStar(surveyId);
        return Result.success();
    }

    @Operation(summary = "发布问卷")
    @PutMapping("/publish")
    public Result<?> publishSurvey(@RequestParam("surveyId") Integer surveyId) {
        Integer status = surveyService.publishSurvey(surveyId);
        return Result.success(status);
    }

    @Operation(summary = "停止发布问卷")
    @PutMapping("/stop")
    public Result<?> stopSurvey(@RequestParam("surveyId") Integer surveyId) {
        Integer status = surveyService.stopSurvey(surveyId);
        return Result.success(status);
    }

    @Operation(summary = "根据id查询问卷")
    @GetMapping("/examination/{id}")
    public Result<?> getSurveyForExam(@PathVariable("id") Integer id) {
        Survey survey = surveyService.getById(id);
        if (survey.getStatus() != 2) {
            return Result.success();
        }
        survey.setExamineeList(surveyQuestionService.getExamineeList(id));
        survey.setQuestionList(surveyQuestionService.getQuestionListForExam(id));
        return Result.success(survey);
    }

    @Autowired
    private SurveyUserAnswerService surveyUserAnswerService;

    @Operation(summary = "提交考试")
    @PostMapping("/examination")
    public Result<?> submitExam(@RequestBody List<SurveyUserAnswer> answers,
                                @RequestParam("examDuration") Integer examDuration) {
        log.debug("---------> answers:" + answers);
        Integer scoreId = surveyUserAnswerService.submitExam(answers, examDuration);
        return Result.success(scoreId);
    }

    @Autowired
    private SurveyUserScoreService surveyUserScoreService;

    @Operation(summary = "查询考试")
    @GetMapping("/examination/score")
    public Result<?> getExamResult(@RequestParam("scoreId") Integer scoreId) {
        SurveyUserScore surveyUserScore = surveyUserScoreService.getById(scoreId);
        return Result.success(surveyUserScore);
    }

    @Operation(summary = "查询问卷答题情况")
    @GetMapping("/examination/score/details")
    public Result<?> getExamResultInfo(@RequestParam("surveyId") Integer surveyId,
                                       @RequestParam("scoreId") Integer scoreId) {
        /*Survey survey = surveyService.getById(id);
        survey.setExamineeList(surveyQuestionService.getExamineeList(id));
        survey.setQuestionList(surveyQuestionService.getQuestionList(id));*/

        Map<String, Object> data = new HashMap();
        data.put("examineeList", surveyQuestionService.getExamineeListForResult(surveyId, scoreId));
        data.put("questionList", surveyQuestionService.getQuestionListForResult(surveyId, scoreId));

        return Result.success(data);
    }

    @Operation(summary = "查询排行榜")
    @GetMapping("/examination/ranking")
    public Result<Map<String, Object>> getExamRanking(@RequestParam("surveyId") Integer surveyId) {
        Survey survey = surveyService.getById(surveyId);
        List<SurveyUserScore> list = surveyUserScoreService.getExamRanking(surveyId);

        Map<String, Object> data = Map.of(
                "title", survey.getTitle(),
                "rankingList", list);
        return Result.success(data);
    }

}
