package com.suhyun444.lifehub.card.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

import com.suhyun444.lifehub.card.Entity.AnalysisHistory;
import com.suhyun444.lifehub.card.Entity.Transaction;

public class AnalysisDto {

    // 1. 프론트에서 받을 요청 (Request)
    @Data
    public static class Request {
        private List<TransactionDto> transactions;
        private String month;
    }


    // 2. 프론트로 보낼 응답 (Response) - AI가 채워줄 데이터
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String month;
        private String summary;
        private List<Trend> trends;
        private List<Recommendation> recommendations;
        private BudgetHealth budgetHealth;
        public static Response from(AnalysisHistory analysisHistory) {
        return new Response(
            analysisHistory.getMonth(),
            analysisHistory.getSummary(),
            analysisHistory.getTrends(),
            analysisHistory.getRecommendations(),
            new BudgetHealth(analysisHistory.getTotalScore(),analysisHistory.getHealthStatus(),analysisHistory.getHealthDescription())
        );
    }
    }

    @Data
    public static class Trend {
        private String type; // increase, decrease, stable
        private String category;
        private String change;
        private String description;
    }

    @Data
    public static class Recommendation {
        private String title;
        private String description;
        private String priority; // high, medium, low
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetHealth {
        private int score;
        private String status;
        private String description;
    }
}