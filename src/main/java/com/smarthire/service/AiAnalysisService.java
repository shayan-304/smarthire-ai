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

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private static final String MODEL = "gemini-2.5-flash";

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
             + "Analyze this resume against the job description carefully and generate a complete Career Success Suite analysis.\n\n"
             + "RESUME:\n" + resume + "\n\n"
             + "JOB DESCRIPTION:\n" + jd + "\n\n"
             + "Return ONLY a raw JSON object. No markdown. No backticks. No explanation.\n"
             + "Start your response with { and end with }.\n\n"
             + "{\n"
             + "  \"atsScore\": <integer 0-100>,\n"
             + "  \"scoreLabel\": \"<Poor|Below Average|Average|Good|Excellent>\",\n"
             + "  \"overallFeedback\": \"<2-3 sentence honest assessment>\",\n"
             + "  \"experienceLevel\": \"<Fresher|Junior|Mid-Level|Senior>\",\n"
             + "  \"matchingSkills\": [\"skill1\"],\n"
             + "  \"missingSkills\": [\"skill1\"],\n"
             + "  \"strengths\": [\"strength1\"],\n"
             + "  \"improvements\": [\"area1\"],\n"
             + "  \"actionItems\": [\"action1\"],\n"
             + "  \"interviewPrep\": {\n"
             + "    \"topQuestions\": [ { \"question\": \"?\", \"category\": \"Technical\", \"difficulty\": \"Beginner\", \"guidance\": \"?\", \"mistakesToAvoid\": \"?\", \"topicsToRevise\": \"?\", \"exampleAnswer\": \"?\" } ],\n"
             + "    \"resources\": [ { \"skill\": \"?\", \"documentationUrl\": \"?\", \"youtubeQuery\": \"?\" } ]\n"
             + "  },\n"
             + "  \"keywordGapAnalysis\": {\n"
             + "    \"missingKeywords\": [ { \"keyword\": \"?\", \"impact\": \"High\", \"category\": \"DevOps\" } ]\n"
             + "  },\n"
             + "  \"resumeEnhancement\": {\n"
             + "    \"recommendations\": [ { \"section\": \"Experience\", \"currentContent\": \"?\", \"suggestedContent\": \"?\", \"reason\": \"?\" } ],\n"
             + "    \"keywordPlacements\": [ { \"keyword\": \"?\", \"suggestedSection\": \"?\", \"exampleSentence\": \"?\" } ]\n"
             + "  },\n"
             + "  \"atsSimulator\": {\n"
             + "    \"currentScore\": 72,\n"
             + "    \"predictedScore\": 89,\n"
             + "    \"estimatedImprovement\": \"+17%\"\n"
             + "  },\n"
             + "  \"profileInsights\": {\n"
             + "    \"strongMatchingAreas\": [\"?\"],\n"
             + "    \"missingExperienceIndicators\": [\"?\"],\n"
             + "    \"suggestedImprovements\": [\"?\"]\n"
             + "  },\n"
             + "  \"learningRoadmap\": {\n"
             + "    \"prioritySkillsToLearn\": [\"?\"],\n"
             + "    \"technologiesToExplore\": [\"?\"],\n"
             + "    \"recommendedCertifications\": [\"?\"],\n"
             + "    \"practiceAreas\": [\"?\"]\n"
             + "  }\n"
             + "}\n\n"
             + "Rules:\n"
             + "- Generate up to 10 highly personalized interview questions.\n"
             + "- Provide specific, actionable resume enhancement rewrites.\n"
             + "- Do NOT encourage keyword stuffing. Provide natural rewrites.\n"
             + "- Return ONLY valid JSON.";
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
            
            // New Modules
            r.setInterviewPrep(j.path("interviewPrep"));
            r.setKeywordGapAnalysis(j.path("keywordGapAnalysis"));
            r.setResumeEnhancement(j.path("resumeEnhancement"));
            r.setAtsSimulator(j.path("atsSimulator"));
            r.setProfileInsights(j.path("profileInsights"));
            r.setLearningRoadmap(j.path("learningRoadmap"));
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
