package com.suhyun444.lifehub;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suhyun444.lifehub.DTO.MerchantCategoryDto;
import com.suhyun444.lifehub.Entity.AnalysisHistory;
import com.suhyun444.lifehub.Entity.Transaction;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory,Long>{
    Optional<List<AnalysisHistory>> findByUserId(Long userId);
    Optional<AnalysisHistory> findByUserIdAndMonth(Long userId, String month);
}
