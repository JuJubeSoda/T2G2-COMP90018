package org.unimelb.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.unimelb.ai.service.PlantAIService;
import org.unimelb.common.vo.Result;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/plant-ai")
public class PlantAIController {

    @Autowired
    private PlantAIService plantAIService;

    /**
     * 植物识别
     * POST /api/plant-ai/identify
     */
    @PostMapping("/identify")
    public Result<Map<String, String>> identifyPlant(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "location", required = false) String location) {
        
        try {
            String result = plantAIService.identifyPlant(imageFile, location);
            Map<String, String> response = new HashMap<>();
            response.put("identification", result);
            response.put("imageName", imageFile.getOriginalFilename());
            response.put("location", location != null ? location : "未知");
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(500, "植物识别失败: " + e.getMessage());
        }
    }

    /**
     * 获取种植建议
     * POST /api/plant-ai/recommendations
     */
    @PostMapping("/recommendations")
    public Result<Map<String, String>> getPlantRecommendations(
            @RequestParam("location") String location,
            @RequestBody(required = false) Map<String, Object> sensorData) {
        
        try {
            String recommendations = plantAIService.getPlantRecommendations(location, sensorData);
            Map<String, String> response = new HashMap<>();
            response.put("recommendations", recommendations);
            response.put("location", location);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(500, "获取种植建议失败: " + e.getMessage());
        }
    }

    /**
     * 获取植物养护建议
     * POST /api/plant-ai/care-advice
     */
    @PostMapping("/care-advice")
    public Result<Map<String, String>> getPlantCareAdvice(
            @RequestParam("plantName") String plantName,
            @RequestBody(required = false) Map<String, Object> currentConditions) {
        
        try {
            String advice = plantAIService.getPlantCareAdvice(plantName, currentConditions);
            Map<String, String> response = new HashMap<>();
            response.put("careAdvice", advice);
            response.put("plantName", plantName);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(500, "获取养护建议失败: " + e.getMessage());
        }
    }

    /**
     * 植物相关问答
     * GET /api/plant-ai/ask
     */
    @GetMapping("/ask")
    public Result<Map<String, String>> askPlantQuestion(@RequestParam("question") String question) {
        try {
            String answer = plantAIService.askPlantQuestion(question);
            Map<String, String> response = new HashMap<>();
            response.put("question", question);
            response.put("answer", answer);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(500, "AI问答失败: " + e.getMessage());
        }
    }

    /**
     * 传感器数据示例
     * GET /api/plant-ai/sensor-example
     */
    @GetMapping("/sensor-example")
    public Result<Map<String, Object>> getSensorDataExample() {
        Map<String, Object> example = new HashMap<>();
        example.put("temperature", "25°C");
        example.put("humidity", "60%");
        example.put("lightLevel", "中等光照");
        example.put("soilMoisture", "湿润");
        example.put("phLevel", "6.5");
        example.put("description", "这是一个传感器数据示例，你可以根据实际传感器数据调整");
        
        return Result.success(example);
    }
}
