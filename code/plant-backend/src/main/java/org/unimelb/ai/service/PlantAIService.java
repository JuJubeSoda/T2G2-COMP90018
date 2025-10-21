package org.unimelb.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class PlantAIService {

    @Value("${openai.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * Plant Identification - Identify plants from images
     */
    public String identifyPlant(MultipartFile imageFile, String location) {
        try {
            // Convert image to base64 for OpenAI Vision API
            String base64Image = convertImageToBase64(imageFile);
            
            String prompt = String.format(
                "Please identify the plant species in this image. Location: %s." +
                "Please provide: 1. Plant name (English and Chinese) 2. Family and genus 3. Basic characteristics 4. Growing environment requirements 5. Care tips",
                location != null ? location : "Unknown location"
            );
            
            return callOpenAIVision(prompt, base64Image, "You are a professional plant identification expert. Please accurately identify plants and provide detailed information.");
            
        } catch (Exception e) {
            return "Error processing image: " + e.getMessage();
        }
    }
    
    /**
     * Convert MultipartFile to base64 string
     */
    private String convertImageToBase64(MultipartFile imageFile) throws IOException {
        byte[] imageBytes = imageFile.getBytes();
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Call OpenAI Vision API for image analysis
     */
    private String callOpenAIVision(String prompt, String base64Image, String systemMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4-vision-preview");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            List<Map<String, Object>> messages = new ArrayList<>();
            
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            messages.add(systemMsg);
            
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            
            List<Map<String, Object>> content = new ArrayList<>();
            
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", prompt);
            content.add(textContent);
            
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
            imageContent.put("image_url", imageUrl);
            content.add(imageContent);
            
            userMsg.put("content", content);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, request, Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                return "Unable to get AI response, please try again later.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }

            return "AI response format error, please try again later.";

        } catch (Exception e) {
            e.printStackTrace();
            return "AI service temporarily unavailable, please try again later.";
        }
    }

    /**
     * Plant Recommendations - Recommend suitable plants based on location and sensor data
     */
    public String getPlantRecommendations(String location, Map<String, Object> sensorData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following information, recommend suitable plants for cultivation:\n");
        prompt.append("Location: ").append(location != null ? location : "Unknown").append("\n");
        
        if (sensorData != null) {
            prompt.append("Sensor Data:\n");
            sensorData.forEach((key, value) -> 
                prompt.append("- ").append(key).append(": ").append(value).append("\n")
            );
        }
        
        prompt.append("Please recommend 3-5 suitable plants, including: plant name, cultivation difficulty, care tips, expected harvest time.");
        
        return callOpenAI(prompt.toString(), 
            "You are a professional horticulture expert. Please recommend the most suitable plants based on environmental conditions.");
    }

    /**
     * Plant Care Advice - Provide care advice based on plant type and current conditions
     */
    public String getPlantCareAdvice(String plantName, Map<String, Object> currentConditions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please provide care advice for the following plant:\n");
        prompt.append("Plant Name: ").append(plantName).append("\n");
        
        if (currentConditions != null) {
            prompt.append("Current Conditions:\n");
            currentConditions.forEach((key, value) -> 
                prompt.append("- ").append(key).append(": ").append(value).append("\n")
            );
        }
        
        prompt.append("Please provide: watering frequency, fertilization advice, light requirements, common problem prevention.");
        
        return callOpenAI(prompt.toString(), 
            "You are a professional plant care expert. Please provide detailed care guidance.");
    }

    /**
     * General Plant Q&A - Limited to plant-related topics
     */
    public String askPlantQuestion(String question) {
        // Check if the question is plant-related
        if (!isPlantRelatedQuestion(question)) {
            return "Sorry, I can only answer questions related to plants, gardening, and cultivation. Please ask about plant identification, cultivation advice, care tips, etc.";
        }
        
        return callOpenAI(question, 
            "You are a professional plant and gardening expert. Please answer plant-related questions.");
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAI(String userMessage, String systemMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, request, Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                return "Unable to get AI response, please try again later.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }

            return "AI response format error, please try again later.";

        } catch (Exception e) {
            e.printStackTrace();
            return "AI service temporarily unavailable, please try again later.";
        }
    }

    /**
     * Check if the question is plant-related
     */
    private boolean isPlantRelatedQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        String[] plantKeywords = {
            "plant", "flower", "tree", "garden", "grow", "care", "water", 
            "fertilizer", "soil", "seed", "harvest", "bloom", "cultivation",
            "gardening", "horticulture", "vegetable", "fruit", "herb", "leaf",
            "root", "stem", "bud", "sprout", "seedling", "crop", "farming",
            "greenhouse", "compost", "mulch", "pruning", "pollination"
        };
        
        for (String keyword : plantKeywords) {
            if (lowerQuestion.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
