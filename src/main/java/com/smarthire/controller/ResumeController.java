package com.smarthire.controller;

import com.smarthire.model.AnalysisRequest;
import com.smarthire.model.AnalysisResponse;
import com.smarthire.service.AiAnalysisService;
import com.smarthire.service.FileParserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final AiAnalysisService aiAnalysisService;
    private final FileParserService fileParserService;

    public ResumeController(AiAnalysisService aiAnalysisService,
                            FileParserService fileParserService) {
        this.aiAnalysisService = aiAnalysisService;
        this.fileParserService = fileParserService;
    }

    /**
     * Analyze resume from plain text (existing endpoint)
     * POST /api/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeResume(
            @Valid @RequestBody AnalysisRequest request) {
        AnalysisResponse response = aiAnalysisService.analyzeResume(request);
        if (!response.isSuccess()) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Upload resume file (PDF or DOCX) and analyze against job description
     * POST /api/upload-analyze
     * Multipart form: resumeFile (PDF/DOCX) + jobDescription (text)
     */
    @PostMapping("/upload-analyze")
    public ResponseEntity<AnalysisResponse> uploadAndAnalyze(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) {

        // Validate file
        if (resumeFile.isEmpty()) {
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage("No file uploaded. Please attach your resume PDF or DOCX.");
            return ResponseEntity.badRequest().body(err);
        }

        // Validate job description
        if (jobDescription == null || jobDescription.trim().length() < 20) {
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage("Job description is too short. Please provide more detail.");
            return ResponseEntity.badRequest().body(err);
        }

        // File size check (5MB max)
        if (resumeFile.getSize() > 5 * 1024 * 1024) {
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage("File too large. Maximum size is 5MB.");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            // Extract text from PDF/DOCX
            String resumeText = fileParserService.extractText(resumeFile);

            // Run AI analysis
            AnalysisRequest analysisRequest = new AnalysisRequest(
                resumeText, jobDescription.trim());
            AnalysisResponse response = aiAnalysisService.analyzeResume(analysisRequest);

            if (!response.isSuccess()) {
                return ResponseEntity.internalServerError().body(response);
            }
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage(e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            AnalysisResponse err = new AnalysisResponse();
            err.setSuccess(false);
            err.setErrorMessage("Failed to process file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    /** Health check — GET /api/health */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "app", "SmartHire AI",
            "version", "1.0.0"
        ));
    }

    /** App info — GET /api/info */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
            "name", "SmartHire AI - Resume Analyzer",
            "description", "AI-powered resume analysis and ATS scoring tool",
            "ai", "Google Gemini 1.5 Flash",
            "version", "1.0.0"
        ));
    }
}
