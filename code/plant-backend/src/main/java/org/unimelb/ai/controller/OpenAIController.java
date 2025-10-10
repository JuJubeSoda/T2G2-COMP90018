package org.unimelb.ai.controller;


import org.unimelb.ai.service.OpenAIService;
import org.springframework.web.bind.annotation.*;
import org.unimelb.ai.vo.BaseResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/ai_bot")
public class OpenAIController {

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/ask")
    public BaseResponse<Map<String, String>> ask(@RequestParam String q) {
        System.out.println("===== AI Reply =====");
        String answer = openAIService.ask(q);
        System.out.println(answer);
        System.out.println("====================");
        return new BaseResponse<>(200, "ok", Map.of("reply", answer));
    }
}

