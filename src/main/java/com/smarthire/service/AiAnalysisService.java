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

    // Groq API — free, works in India, 14400 req/day
    // OpenAI-compatible format — simple and reliable
    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    // Best free model on Groq — fast and smart
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("your-")) {
            log.severe("GROQ_API_KEY is not set! Get a free key at console.groq.com");
        } else {
            log.info("Groq API ready — model: " + MODEL +
                     " | key: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        }
    }

    public AnalysisResponse analyzeResume(AnalysisRequest request) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("your-")) {
            return error("AI service not configured. GROQ_API_KEY environment variable is missing.");
        }
        try {
            String prompt  = buildPrompt(request.getResumeText(), request.getJobDescription());
            String rawText = callGroq(prompt);
            return parseResponse(rawText);
        } catch (Exception e) {
            log.severe("analyzeResume failed: " + e.getMessage());
            return error("Analysis failed: " + e.getMessage());
        }
    }

    // ─── Build Prompt ──────────────────────────────────────────────────────────

    private String buildPrompt(String resume, String jd) {
        return "You are an expert ATS (Applicant Tracking System) analyst and HR consultant.\n"
             + "Carefully analyze the resume against the job description below.\n\n"
             + "RESUME:\n" + resume + "\n\n"
             + "JOB DESCRIPTION:\n" + jd + "\n\n"
             + "Return ONLY a raw JSON object. No markdown. No backticks. No explanation.\n"
             + "Start your response with { and end with }.\n\n"
             + "JSON structure:\n"
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
             + "Scoring rules:\n"
             + "- atsScore: keyword match + experience relevance (0=terrible, 100=perfect)\n"
             + "- matchingSkills: max 8 — skills present in BOTH resume AND job description\n"
             + "- missingSkills: max 6 — key skills in job description but NOT in resume\n"
             + "- strengths: 3-4 genuine strong points of this resume\n"
             + "- improvements: 3-4 specific things the candidate should fix\n"
             + "- actionItems: 3-5 concrete next steps for the candidate\n"
             + "Return ONLY the JSON. Nothing else.";
    }

    // ─── Call Groq API (OpenAI-compatible) ────────────────────────────────────

    private String callGroq(String prompt) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "model",    MODEL,
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "max_tokens",   1500,
            "temperature",  0.2,
            "top_p",        0.9,
            "stream",       false
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Content-Type",  "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        if (status == 401) throw new RuntimeException(
                "Invalid Groq API key. Get a free key at console.groq.com");
        if (status == 429) throw new RuntimeException(
                "Groq rate limit hit. Wait 60 seconds and try again.");
        if (status == 413) throw new RuntimeException(
                "Resume is too long for AI processing. Try a shorter resume.");
        if (status != 200) throw new RuntimeException(
                "Groq API error " + status + ": " +
                response.body().substring(0, Math.min(300, response.body().length())));

        // Parse OpenAI-compatible response format
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (choices.isMissingNode() || !choices.isArray() || choices.isEmpty()) {
            throw new RuntimeException("Groq returned empty choices. Raw: " + response.body());
        }
        return choices.get(0).path("message").path("content").asText();
    }

    // ─── Parse AI Response ────────────────────────────────────────────────────

    private AnalysisResponse parseResponse(String rawText) {
        try {
            String s = rawText.trim();

            // Strip any markdown fences
            s = s.replaceAll("(?s)^```json\\s*", "");
            s = s.replaceAll("(?s)^```\\s*",     "");
            s = s.replaceAll("```\\s*$",          "");
            s = s.trim();

            // Extract JSON boundaries (handles extra text before/after)
            int start = s.indexOf('{');
            int end   = s.lastIndexOf('}');
            if (start < 0 || end <= start) {
                throw new RuntimeException("No JSON object found in AI response. Got: " + s.substring(0, Math.min(200, s.length())));
            }
            s = s.substring(start, end + 1);

            JsonNode j = objectMapper.readTree(s);

            AnalysisResponse r = new AnalysisResponse();
            r.setSuccess(true);
            r.setAtsScore(       clamp(j.path("atsScore").asInt(50)));
            r.setScoreLabel(     text(j, "scoreLabel",      "Average"));
            r.setOverallFeedback(text(j, "overallFeedback", "Analysis complete."));
            r.setExperienceLevel(text(j, "experienceLevel", "Fresher"));
            r.setMatchingSkills( list(j.path("matchingSkills")));
            r.setMissingSkills(  list(j.path("missingSkills")));
            r.setStrengths(      list(j.path("strengths")));
            r.setImprovements(   list(j.path("improvements")));
            r.setActionItems(    list(j.path("actionItems")));
            return r;

        } catch (Exception e) {
            log.severe("parseResponse error: " + e.getMessage() + "\nRaw: " + rawText.substring(0, Math.min(500, rawText.length())));
            return error("Could not parse AI response. Please try again.");
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private AnalysisResponse error(String msg) {
        AnalysisResponse r = new AnalysisResponse();
        r.setSuccess(false);
        r.setErrorMessage(msg);
        return r;
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }

    private String text(JsonNode j, String field, String def) {
        String v = j.path(field).asText("").trim();
        return v.isEmpty() ? def : v;
    }

    private List<String> list(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String v = item.asText("").trim();
                if (!v.isEmpty()) out.add(v);
            }
        }
        return out;
    }
}
