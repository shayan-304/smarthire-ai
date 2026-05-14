package com.smarthire.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthire.model.AnalysisRequest;
import com.smarthire.model.AnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiAnalysisService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public AnalysisResponse analyzeResume(AnalysisRequest request) {
        try {
            String prompt = buildPrompt(request.getResumeText(), request.getJobDescription());
            String aiResponse = callGemini(prompt);
            return parseAiResponse(aiResponse);
        } catch (Exception e) {
            AnalysisResponse errorResponse = new AnalysisResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Analysis failed: " + e.getMessage());
            return errorResponse;
        }
    }

    private String buildPrompt(String resumeText, String jobDescription) {
        return String.format("""
                You are an expert ATS (Applicant Tracking System) and HR consultant. Analyze the following resume against the job description.

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Respond ONLY with a valid JSON object (no markdown, no backticks, no extra text) in this exact structure:
                {
                  "atsScore": <integer 0-100>,
                  "scoreLabel": "<Poor/Below Average/Average/Good/Excellent>",
                  "overallFeedback": "<2-3 sentence overall assessment>",
                  "experienceLevel": "<Fresher/Junior/Mid-Level/Senior>",
                  "matchingSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill1", "skill2"],
                  "strengths": ["strength1", "strength2"],
                  "improvements": ["area1", "area2"],
                  "actionItems": ["action1", "action2", "action3"]
                }

                Rules:
                - atsScore must be a realistic integer from 0 to 100
                - matchingSkills: skills in the resume that match the job description (max 8)
                - missingSkills: important skills from job description not found in resume (max 6)
                - strengths: 3-4 strong points of the resume
                - improvements: 3-4 specific areas to improve
                - actionItems: 3-5 concrete next steps the candidate should take
                - Be honest and constructive
                - Return ONLY the JSON object, nothing else
                """, resumeText, jobDescription);
    }

    private String callGemini(String prompt) throws Exception {
        String requestJson = objectMapper.writeValueAsString(
            Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.3,
                    "maxOutputTokens", 1200
                )
            )
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(
                httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Gemini API error: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode responseNode = objectMapper.readTree(response.body());
        return responseNode
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();
    }

    private AnalysisResponse parseAiResponse(String aiResponseText) {
        try {
            String cleaned = aiResponseText.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();

            JsonNode json = objectMapper.readTree(cleaned);

            AnalysisResponse response = new AnalysisResponse();
            response.setSuccess(true);
            response.setAtsScore(json.path("atsScore").asInt(50));
            response.setScoreLabel(json.path("scoreLabel").asText("Average"));
            response.setOverallFeedback(json.path("overallFeedback").asText("Analysis complete."));
            response.setExperienceLevel(json.path("experienceLevel").asText("Fresher"));
            response.setMatchingSkills(parseStringList(json.path("matchingSkills")));
            response.setMissingSkills(parseStringList(json.path("missingSkills")));
            response.setStrengths(parseStringList(json.path("strengths")));
            response.setImprovements(parseStringList(json.path("improvements")));
            response.setActionItems(parseStringList(json.path("actionItems")));

            return response;
        } catch (Exception e) {
            AnalysisResponse errorResponse = new AnalysisResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Failed to parse AI response: " + e.getMessage());
            return errorResponse;
        }
    }

    private List<String> parseStringList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                list.add(item.asText());
            }
        }
        return list;
    }
}
