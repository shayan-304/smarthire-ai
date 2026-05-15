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

    /** POST /api/analyze — plain text input */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeResume(
            @Valid @RequestBody AnalysisRequest request) {

        AnalysisResponse response = aiAnalysisService.analyzeResume(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    /** POST /api/upload-analyze — PDF or DOCX file upload */
    @PostMapping("/upload-analyze")
    public ResponseEntity<AnalysisResponse> uploadAndAnalyze(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) {

        // File validation
        if (resumeFile == null || resumeFile.isEmpty()) {
            return bad("No file received. Please select a PDF or DOCX resume file.");
        }
        if (jobDescription == null || jobDescription.trim().length() < 20) {
            return bad("Job description is too short. Please paste a full job description (at least 20 characters).");
        }
        if (resumeFile.getSize() > 10 * 1024 * 1024) {
            return bad("File is too large (max 10MB). Please upload a smaller file.");
        }

        String resumeText;
        try {
            resumeText = fileParserService.extractText(resumeFile);
        } catch (IllegalArgumentException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return bad("Could not read file: " + e.getMessage());
        }

        if (resumeText.trim().length() < 50) {
            return bad("Extracted text is too short. The file may be empty or image-based. Try the 'Paste Text' tab.");
        }

        AnalysisResponse response = aiAnalysisService.analyzeResume(
                new AnalysisRequest(resumeText.trim(), jobDescription.trim()));

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    /** GET /api/health */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "app",     "SmartHire AI",
                "version", "1.0.0",
                "ai",      "Google Gemini 1.5 Flash"
        ));
    }

    /** GET /api/info */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
                "name",        "SmartHire AI",
                "description", "AI-powered resume analyzer using Google Gemini",
                "version",     "1.0.0"
        ));
    }

    private ResponseEntity<AnalysisResponse> bad(String msg) {
        AnalysisResponse err = new AnalysisResponse();
        err.setSuccess(false);
        err.setErrorMessage(msg);
        return ResponseEntity.badRequest().body(err);
    }
}
