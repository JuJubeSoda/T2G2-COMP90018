package org.unimelb.ai.controller;


import org.unimelb.ai.service.OpenAIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai_bot")
public class OpenAIController {

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String q) {
        return openAIService.ask(q);
    }
}

