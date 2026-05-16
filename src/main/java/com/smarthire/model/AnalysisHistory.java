package com.smarthire.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JPA Entity — every resume analysis is saved here automatically.
 * Stored in H2 embedded database (file-based, survives restarts).
 */
@Entity
@Table(name = "analysis_history")
public class AnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // When the analysis was done
    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    // ATS score result
    @Column(nullable = false)
    private int atsScore;

    @Column(length = 50)
    private String scoreLabel;

    @Column(length = 100)
    private String experienceLevel;

    // Snippet of resume (first 300 chars) — not the full text for storage efficiency
    @Column(length = 300)
    private String resumeSnippet;

    // Snippet of job description (first 200 chars)
    @Column(length = 200)
    private String jobDescSnippet;

    // Full feedback
    @Column(length = 1000)
    private String overallFeedback;

    // Comma-separated skills for easy storage
    @Column(length = 500)
    private String matchingSkills;

    @Column(length = 500)
    private String missingSkills;

    // How the resume was submitted
    @Column(length = 20)
    private String inputMethod; // "FILE_UPLOAD" or "TEXT_PASTE"

    @Column(length = 50)
    private String fileName; // original file name if uploaded

    // ── Constructors ────────────────────────────────────────────────────────

    public AnalysisHistory() {}

    public AnalysisHistory(AnalysisResponse result, String resumeText,
                           String jobDescription, String inputMethod, String fileName) {
        this.analyzedAt     = LocalDateTime.now();
        this.atsScore       = result.getAtsScore();
        this.scoreLabel     = result.getScoreLabel();
        this.experienceLevel= result.getExperienceLevel();
        this.overallFeedback= result.getOverallFeedback();
        this.inputMethod    = inputMethod;
        this.fileName       = fileName;

        // Store only snippets to keep DB light
        this.resumeSnippet  = resumeText  != null && resumeText.length()  > 300
                              ? resumeText.substring(0, 300)  + "..." : resumeText;
        this.jobDescSnippet = jobDescription != null && jobDescription.length() > 200
                              ? jobDescription.substring(0, 200) + "..." : jobDescription;

        // Join skill lists into comma-separated strings
        this.matchingSkills = result.getMatchingSkills() != null
                              ? String.join(", ", result.getMatchingSkills()) : "";
        this.missingSkills  = result.getMissingSkills()  != null
                              ? String.join(", ", result.getMissingSkills())  : "";
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public Long          getId()             { return id; }
    public LocalDateTime getAnalyzedAt()     { return analyzedAt; }
    public int           getAtsScore()       { return atsScore; }
    public String        getScoreLabel()     { return scoreLabel; }
    public String        getExperienceLevel(){ return experienceLevel; }
    public String        getResumeSnippet()  { return resumeSnippet; }
    public String        getJobDescSnippet() { return jobDescSnippet; }
    public String        getOverallFeedback(){ return overallFeedback; }
    public String        getMatchingSkills() { return matchingSkills; }
    public String        getMissingSkills()  { return missingSkills; }
    public String        getInputMethod()    { return inputMethod; }
    public String        getFileName()       { return fileName; }

    public String getFormattedDate() {
        return analyzedAt != null
               ? analyzedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
               : "";
    }
}
