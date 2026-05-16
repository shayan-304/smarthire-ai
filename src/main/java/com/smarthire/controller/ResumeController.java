package com.smarthire.controller;

import com.smarthire.model.AnalysisHistory;
import com.smarthire.model.AnalysisRequest;
import com.smarthire.model.AnalysisResponse;
import com.smarthire.service.AiAnalysisService;
import com.smarthire.service.FileParserService;
import com.smarthire.service.HistoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final AiAnalysisService  aiService;
    private final FileParserService  fileParser;
    private final HistoryService     historyService;

    public ResumeController(AiAnalysisService aiService,
                            FileParserService fileParser,
                            HistoryService historyService) {
        this.aiService      = aiService;
        this.fileParser     = fileParser;
        this.historyService = historyService;
    }

    /** POST /api/analyze — plain text input */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeResume(
            @Valid @RequestBody AnalysisRequest request) {

        AnalysisResponse response = aiService.analyzeResume(request);

        if (response.isSuccess()) {
            historyService.save(response,
                request.getResumeText(), request.getJobDescription(),
                "TEXT_PASTE", null);
        }

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    /** POST /api/upload-analyze — PDF or DOCX file upload */
    @PostMapping("/upload-analyze")
    public ResponseEntity<AnalysisResponse> uploadAndAnalyze(
            @RequestParam("resumeFile")    MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) {

        if (resumeFile == null || resumeFile.isEmpty())
            return bad("No file received. Please select a PDF or DOCX file.");
        if (jobDescription == null || jobDescription.trim().length() < 20)
            return bad("Job description too short. Please add more detail.");
        if (resumeFile.getSize() > 10 * 1024 * 1024)
            return bad("File too large (max 10MB).");

        String resumeText;
        try {
            resumeText = fileParser.extractText(resumeFile);
        } catch (IllegalArgumentException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return bad("Could not read file: " + e.getMessage());
        }

        if (resumeText.trim().length() < 50)
            return bad("Extracted text too short. File may be empty or image-based. Try 'Paste Text' tab.");

        AnalysisResponse response = aiService.analyzeResume(
                new AnalysisRequest(resumeText.trim(), jobDescription.trim()));

        if (response.isSuccess()) {
            historyService.save(response,
                resumeText, jobDescription,
                "FILE_UPLOAD", resumeFile.getOriginalFilename());
        }

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    /** GET /api/history — returns last 20 analyses */
    @GetMapping("/history")
    public ResponseEntity<List<AnalysisHistory>> getHistory() {
        return ResponseEntity.ok(historyService.getRecent());
    }

    /** GET /api/stats — total analyses count */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalAnalyses", historyService.getTotalCount(),
            "app",           "SmartHire AI",
            "version",       "2.1.0"
        ));
    }

    /** GET /api/health */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status",  "UP",
            "app",     "SmartHire AI",
            "version", "2.1.0",
            "ai",      "Groq LLaMA 3.3 70B"
        ));
    }

    private ResponseEntity<AnalysisResponse> bad(String msg) {
        AnalysisResponse e = new AnalysisResponse();
        e.setSuccess(false); e.setErrorMessage(msg);
        return ResponseEntity.badRequest().body(e);
    }
}
