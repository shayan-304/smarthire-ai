package com.smarthire.model;

import java.util.List;

public class AnalysisResponse {

    private int atsScore;
    private String scoreLabel;
    private String overallFeedback;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private List<String> strengths;
    private List<String> improvements;
    private List<String> actionItems;
    private String experienceLevel;
    private boolean success;
    private String errorMessage;

    private com.fasterxml.jackson.databind.JsonNode interviewPrep;
    private com.fasterxml.jackson.databind.JsonNode keywordGapAnalysis;
    private com.fasterxml.jackson.databind.JsonNode resumeEnhancement;
    private com.fasterxml.jackson.databind.JsonNode atsSimulator;
    private com.fasterxml.jackson.databind.JsonNode profileInsights;
    private com.fasterxml.jackson.databind.JsonNode learningRoadmap;

    public AnalysisResponse() {}

    // Getters and Setters
    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public String getScoreLabel() { return scoreLabel; }
    public void setScoreLabel(String scoreLabel) { this.scoreLabel = scoreLabel; }

    public String getOverallFeedback() { return overallFeedback; }
    public void setOverallFeedback(String overallFeedback) { this.overallFeedback = overallFeedback; }

    public List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<String> matchingSkills) { this.matchingSkills = matchingSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getImprovements() { return improvements; }
    public void setImprovements(List<String> improvements) { this.improvements = improvements; }

    public List<String> getActionItems() { return actionItems; }
    public void setActionItems(List<String> actionItems) { this.actionItems = actionItems; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public com.fasterxml.jackson.databind.JsonNode getInterviewPrep() { return interviewPrep; }
    public void setInterviewPrep(com.fasterxml.jackson.databind.JsonNode interviewPrep) { this.interviewPrep = interviewPrep; }

    public com.fasterxml.jackson.databind.JsonNode getKeywordGapAnalysis() { return keywordGapAnalysis; }
    public void setKeywordGapAnalysis(com.fasterxml.jackson.databind.JsonNode keywordGapAnalysis) { this.keywordGapAnalysis = keywordGapAnalysis; }

    public com.fasterxml.jackson.databind.JsonNode getResumeEnhancement() { return resumeEnhancement; }
    public void setResumeEnhancement(com.fasterxml.jackson.databind.JsonNode resumeEnhancement) { this.resumeEnhancement = resumeEnhancement; }

    public com.fasterxml.jackson.databind.JsonNode getAtsSimulator() { return atsSimulator; }
    public void setAtsSimulator(com.fasterxml.jackson.databind.JsonNode atsSimulator) { this.atsSimulator = atsSimulator; }

    public com.fasterxml.jackson.databind.JsonNode getProfileInsights() { return profileInsights; }
    public void setProfileInsights(com.fasterxml.jackson.databind.JsonNode profileInsights) { this.profileInsights = profileInsights; }

    public com.fasterxml.jackson.databind.JsonNode getLearningRoadmap() { return learningRoadmap; }
    public void setLearningRoadmap(com.fasterxml.jackson.databind.JsonNode learningRoadmap) { this.learningRoadmap = learningRoadmap; }
}
