package com.suhyun444.lifehub.card.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.suhyun444.lifehub.card.Component.Converter.RecommendationListConverter;
import com.suhyun444.lifehub.card.Component.Converter.TrendListConverter;
import com.suhyun444.lifehub.card.DTO.AnalysisDto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
// createdDate 자동 주입을 위해 리스너 추가 (중요!)
@EntityListeners(AuditingEntityListener.class) 
@Table(name = "analysis_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "month"}))
public class AnalysisHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String month; // "2026-01"

    @Column(columnDefinition = "TEXT") 
    private String summary;

    // --- BudgetHealth ---
    private int totalScore;
    private String healthStatus;
    private String healthDescription;

    // --- JSON 데이터 ---
    @Convert(converter = TrendListConverter.class)
    @Column(columnDefinition = "json") // MySQL이 아니면 "TEXT"로 변경하세요
    private List<AnalysisDto.Trend> trends;

    @Convert(converter = RecommendationListConverter.class)
    @Column(columnDefinition = "json") // MySQL이 아니면 "TEXT"로 변경하세요
    private List<AnalysisDto.Recommendation> recommendations;

    // 🌟 BaseTimeEntity 대신 직접 추가한 생성일자
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AnalysisHistory(User user, AnalysisDto.Response response) {
        this.user = user;
        this.month = response.getMonth();
        this.summary = response.getSummary();
        this.totalScore = response.getBudgetHealth().getScore();
        this.healthStatus = response.getBudgetHealth().getStatus();
        this.healthDescription = response.getBudgetHealth().getDescription();
        this.trends = response.getTrends();
        this.recommendations = response.getRecommendations();
    }

    public void update(AnalysisDto.Response response) {
    this.summary = response.getSummary();
    this.totalScore = response.getBudgetHealth().getScore();
    this.healthStatus = response.getBudgetHealth().getStatus();
    this.healthDescription = response.getBudgetHealth().getDescription();
    this.trends = response.getTrends();
    this.recommendations = response.getRecommendations();
    }
}