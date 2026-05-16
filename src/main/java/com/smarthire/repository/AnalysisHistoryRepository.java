package com.smarthire.repository;

import com.smarthire.model.AnalysisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    List<AnalysisHistory> findTop20ByOrderByAnalyzedAtDesc();
    long count();
}
