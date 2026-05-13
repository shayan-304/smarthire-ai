package com.smarthire.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnalysisRequest {

    @NotBlank(message = "Resume text cannot be empty")
    @Size(min = 50, max = 10000, message = "Resume must be between 50 and 10000 characters")
    private String resumeText;

    @NotBlank(message = "Job description cannot be empty")
    @Size(min = 20, max = 5000, message = "Job description must be between 20 and 5000 characters")
    private String jobDescription;

    public AnalysisRequest() {}

    public AnalysisRequest(String resumeText, String jobDescription) {
        this.resumeText = resumeText;
        this.jobDescription = jobDescription;
    }

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
}
