package org.unimelb.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/responses";

    public String ask(String question) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4.1-nano");
        body.put("input", question);
        body.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                API_URL, HttpMethod.POST, request, Map.class
        );

        Map<String, Object> resp = response.getBody();
        if (resp == null) return "No response from OpenAI";

        Map<String, Object> output = (Map<String, Object>) ((List<?>) resp.get("output")).get(0);
        Map<String, Object> content = (Map<String, Object>) ((List<?>) output.get("content")).get(0);
        return (String) content.get("text");
    }
}
