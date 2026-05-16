package com.smarthire.service;

import com.smarthire.model.AnalysisHistory;
import com.smarthire.model.AnalysisResponse;
import com.smarthire.repository.AnalysisHistoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.logging.Logger;

@Service
public class HistoryService {

    private static final Logger log = Logger.getLogger(HistoryService.class.getName());
    private final AnalysisHistoryRepository repo;

    public HistoryService(AnalysisHistoryRepository repo) {
        this.repo = repo;
    }

    /** Save every analysis result to the database */
    public void save(AnalysisResponse result, String resumeText,
                     String jobDescription, String inputMethod, String fileName) {
        try {
            AnalysisHistory record = new AnalysisHistory(
                result, resumeText, jobDescription, inputMethod, fileName);
            repo.save(record);
            log.info("Analysis saved to DB — ID: " + record.getId()
                   + " | Score: " + result.getAtsScore()
                   + " | Total records: " + repo.count());
        } catch (Exception e) {
            // Never crash the main flow if DB save fails
            log.warning("Could not save to DB: " + e.getMessage());
        }
    }

    /** Get last 20 analyses for the history page */
    public List<AnalysisHistory> getRecent() {
        return repo.findTop20ByOrderByAnalyzedAtDesc();
    }

    /** Total analyses count */
    public long getTotalCount() {
        return repo.count();
    }
}
