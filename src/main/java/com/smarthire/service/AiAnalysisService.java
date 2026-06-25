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

    @Value("${gemini.api.key:NOT_SET}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    private static final String MODEL = "gemini-1.5-flash";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30)).build();

    @PostConstruct
    public void init() {
        if ("NOT_SET".equals(apiKey) || apiKey == null || apiKey.isBlank()) {
            log.severe("GEMINI_API_KEY environment variable is NOT SET. Set it in Railway/Render Variables.");
        } else {
            log.info("Gemini ready — model: " + MODEL + " | key: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        }
    }

    public AnalysisResponse analyzeResume(AnalysisRequest request) {
        if ("NOT_SET".equals(apiKey) || apiKey == null || apiKey.isBlank()) {
            return error("GEMINI_API_KEY is not configured. Please set it in your deployment environment variables.");
        }
        try {
            String prompt = buildPrompt(request.getResumeText(), request.getJobDescription());
            String rawText = callGemini(prompt);
            return parseResponse(rawText);
        } catch (Exception e) {
            log.severe("analyzeResume error: " + e.getMessage());
            return error("Analysis failed: " + e.getMessage());
        }
    }

    private String buildPrompt(String resume, String jd) {
        return "You are an expert ATS analyst and HR consultant.\n"
             + "Analyze this resume against the job description carefully.\n\n"
             + "RESUME:\n" + resume + "\n\n"
             + "JOB DESCRIPTION:\n" + jd + "\n\n"
             + "Return ONLY a raw JSON object. No markdown. No backticks. No explanation.\n"
             + "Start your response with { and end with }.\n\n"
             + "{\n"
             + "  \"atsScore\": <integer 0-100>,\n"
             + "  \"scoreLabel\": \"<Poor|Below Average|Average|Good|Excellent>\",\n"
             + "  \"overallFeedback\": \"<2-3 sentence honest assessment>\",\n"
             + "  \"experienceLevel\": \"<Fresher|Junior|Mid-Level|Senior>\",\n"
             + "  \"matchingSkills\": [\"skill1\", \"skill2\"],\n"
             + "  \"missingSkills\": [\"skill1\", \"skill2\"],\n"
             + "  \"strengths\": [\"strength1\", \"strength2\", \"strength3\"],\n"
             + "  \"improvements\": [\"area1\", \"area2\", \"area3\"],\n"
             + "  \"actionItems\": [\"action1\", \"action2\", \"action3\"]\n"
             + "}\n\n"
             + "Rules:\n"
             + "- atsScore: 0-100 based on keyword and experience match\n"
             + "- matchingSkills: max 8, skills in BOTH resume AND job description\n"
             + "- missingSkills: max 6, key skills in JD but NOT in resume\n"
             + "- strengths: 3-4 genuine strong points\n"
             + "- improvements: 3-4 specific things to fix\n"
             + "- actionItems: 3-5 concrete next steps\n"
             + "Return ONLY the JSON. Nothing else.";
    }

    private String callGemini(String prompt) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.2
            )
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status == 400) throw new RuntimeException("Invalid request format to Gemini.");
        if (status == 403) throw new RuntimeException("Invalid Gemini API key. Check GEMINI_API_KEY env var.");
        if (status == 429) throw new RuntimeException("Gemini rate limit exceeded.");
        if (status != 200) throw new RuntimeException("Gemini error " + status + ": " +
                response.body().substring(0, Math.min(300, response.body().length())));

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty())
            throw new RuntimeException("Gemini returned empty choices.");

        return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
    }

    private AnalysisResponse parseResponse(String raw) {
        try {
            String s = raw.trim()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*",     "")
                .replaceAll("```\\s*$",          "")
                .trim();

            int start = s.indexOf('{');
            int end   = s.lastIndexOf('}');
            if (start < 0 || end <= start)
                throw new RuntimeException("No JSON found in response: " + s.substring(0, Math.min(200, s.length())));
            s = s.substring(start, end + 1);

            JsonNode j = objectMapper.readTree(s);
            AnalysisResponse r = new AnalysisResponse();
            r.setSuccess(true);
            r.setAtsScore(       clamp(j.path("atsScore").asInt(50)));
            r.setScoreLabel(     txt(j, "scoreLabel",      "Average"));
            r.setOverallFeedback(txt(j, "overallFeedback", "Analysis complete."));
            r.setExperienceLevel(txt(j, "experienceLevel", "Fresher"));
            r.setMatchingSkills( arr(j.path("matchingSkills")));
            r.setMissingSkills(  arr(j.path("missingSkills")));
            r.setStrengths(      arr(j.path("strengths")));
            r.setImprovements(   arr(j.path("improvements")));
            r.setActionItems(    arr(j.path("actionItems")));
            return r;
        } catch (Exception e) {
            log.severe("parseResponse error: " + e.getMessage());
            return error("Could not parse AI response. Please try again.");
        }
    }

    private AnalysisResponse error(String msg) {
        AnalysisResponse r = new AnalysisResponse(); r.setSuccess(false); r.setErrorMessage(msg); return r;
    }
    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }
    private String txt(JsonNode j, String f, String def) { String v = j.path(f).asText("").trim(); return v.isEmpty() ? def : v; }
    private List<String> arr(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) for (JsonNode i : node) { String v = i.asText("").trim(); if (!v.isEmpty()) out.add(v); }
        return out;
    }
}
