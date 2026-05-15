package com.smarthire.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthire.model.AnalysisRequest;
import com.smarthire.model.AnalysisResponse;
import jakarta.annotation.PostConstruct;
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
import java.util.logging.Logger;

@Service
public class AiAnalysisService {

    private static final Logger log = Logger.getLogger(AiAnalysisService.class.getName());

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your-gemini-api-key-here")) {
            log.severe("⚠️ GEMINI_API_KEY is not configured! Set it as an environment variable.");
        } else {
            log.info("✅ Gemini API key loaded — starts with: " + apiKey.substring(0, 8) + "...");
        }
    }

    public AnalysisResponse analyzeResume(AnalysisRequest request) {
        // Guard: check key before calling
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your-gemini-api-key-here")) {
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage("Gemini API key is not configured. Please set GEMINI_API_KEY environment variable.");
            return err;
        }

        try {
            String prompt  = buildPrompt(request.getResumeText(), request.getJobDescription());
            String rawJson = callGemini(prompt);
            return parseAiResponse(rawJson);
        } catch (Exception e) {
            log.severe("Analysis failed: " + e.getMessage());
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage("Analysis failed: " + e.getMessage());
            return err;
        }
    }

    private String buildPrompt(String resumeText, String jobDescription) {
        return "You are an expert ATS (Applicant Tracking System) and HR consultant.\n" +
               "Analyze the following resume against the job description.\n\n" +
               "RESUME:\n" + resumeText + "\n\n" +
               "JOB DESCRIPTION:\n" + jobDescription + "\n\n" +
               "Respond ONLY with a valid JSON object. No markdown, no backticks, no explanation. " +
               "Return ONLY the raw JSON starting with { and ending with }.\n\n" +
               "{\n" +
               "  \"atsScore\": <integer 0-100>,\n" +
               "  \"scoreLabel\": \"<Poor|Below Average|Average|Good|Excellent>\",\n" +
               "  \"overallFeedback\": \"<2-3 sentence honest assessment>\",\n" +
               "  \"experienceLevel\": \"<Fresher|Junior|Mid-Level|Senior>\",\n" +
               "  \"matchingSkills\": [\"skill1\", \"skill2\"],\n" +
               "  \"missingSkills\": [\"skill1\", \"skill2\"],\n" +
               "  \"strengths\": [\"strength1\", \"strength2\", \"strength3\"],\n" +
               "  \"improvements\": [\"area1\", \"area2\", \"area3\"],\n" +
               "  \"actionItems\": [\"action1\", \"action2\", \"action3\"]\n" +
               "}\n\n" +
               "Rules:\n" +
               "- atsScore: realistic integer 0-100 based on keyword match\n" +
               "- matchingSkills: max 8 skills found in BOTH resume AND job description\n" +
               "- missingSkills: max 6 important skills from job description NOT in resume\n" +
               "- strengths: 3-4 genuine strong points\n" +
               "- improvements: 3-4 specific areas to fix\n" +
               "- actionItems: 3-5 concrete steps the candidate should take next\n" +
               "- Return ONLY the JSON. Nothing before it. Nothing after it.";
    }

    private String callGemini(String prompt) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ),
            "generationConfig", Map.of(
                "temperature",     0.2,
                "maxOutputTokens", 1500,
                "topP",            0.8
            )
        ));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(90))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 400) {
            throw new RuntimeException("Invalid Gemini API key or bad request. Check GEMINI_API_KEY.");
        }
        if (res.statusCode() == 429) {
            throw new RuntimeException("Gemini API quota exceeded. Wait a minute and try again.");
        }
        if (res.statusCode() != 200) {
            throw new RuntimeException("Gemini API error " + res.statusCode() + ": " + res.body());
        }

        JsonNode root = objectMapper.readTree(res.body());

        // Validate Gemini response structure
        JsonNode candidates = root.path("candidates");
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.size() == 0) {
            throw new RuntimeException("Gemini returned no candidates. Raw: " + res.body().substring(0, Math.min(200, res.body().length())));
        }

        return candidates.get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();
    }

    private AnalysisResponse parseAiResponse(String rawText) {
        try {
            // Strip markdown fences if Gemini adds them despite instructions
            String cleaned = rawText.trim();
            cleaned = cleaned.replaceAll("(?s)^```json\\s*", "");
            cleaned = cleaned.replaceAll("(?s)^```\\s*",     "");
            cleaned = cleaned.replaceAll("```\\s*$",          "");

            // Find the JSON object boundaries in case there's extra text
            int start = cleaned.indexOf('{');
            int end   = cleaned.lastIndexOf('}');
            if (start >= 0 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            cleaned = cleaned.trim();
            JsonNode json = objectMapper.readTree(cleaned);

            AnalysisResponse resp = new AnalysisResponse();
            resp.setSuccess(true);
            resp.setAtsScore(       clampScore(json.path("atsScore").asInt(50)));
            resp.setScoreLabel(     json.path("scoreLabel").asText("Average"));
            resp.setOverallFeedback(json.path("overallFeedback").asText("Analysis complete."));
            resp.setExperienceLevel(json.path("experienceLevel").asText("Fresher"));
            resp.setMatchingSkills( toList(json.path("matchingSkills")));
            resp.setMissingSkills(  toList(json.path("missingSkills")));
            resp.setStrengths(      toList(json.path("strengths")));
            resp.setImprovements(   toList(json.path("improvements")));
            resp.setActionItems(    toList(json.path("actionItems")));
            return resp;

        } catch (Exception e) {
            log.severe("Failed to parse Gemini response: " + e.getMessage() + "\nRaw: " + rawText);
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage("Could not parse AI response. Please try again.");
            return err;
        }
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private List<String> toList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String val = item.asText("").trim();
                if (!val.isEmpty()) list.add(val);
            }
        }
        return list;
    }
}
