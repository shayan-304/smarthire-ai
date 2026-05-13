package com.smarthire.controller;

import com.smarthire.model.AnalysisRequest;
import com.smarthire.model.AnalysisResponse;
import com.smarthire.service.AiAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final AiAnalysisService aiAnalysisService;

    public ResumeController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * Analyze resume against a job description
     * POST /api/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeResume(@Valid @RequestBody AnalysisRequest request) {
        AnalysisResponse response = aiAnalysisService.analyzeResume(request);

        if (!response.isSuccess()) {
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "SmartHire AI",
                "version", "1.0.0"
        ));
    }

    /**
     * App info endpoint
     * GET /api/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
                "name", "SmartHire AI - Resume Analyzer",
                "description", "AI-powered resume analysis and ATS scoring tool",
                "author", "Built with Spring Boot + OpenAI",
                "version", "1.0.0"
        ));
    }
}
