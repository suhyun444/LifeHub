package com.suhyun444.lifehub.card;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suhyun444.lifehub.card.Component.SpendingAnalyzer;
import com.suhyun444.lifehub.card.DTO.AnalysisDto;
import com.suhyun444.lifehub.card.DTO.TransactionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.http.HttpMethod.POST;

class SpendingAnalyzerTest {

    private SpendingAnalyzer spendingAnalyzer;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 1. 실제 Builder 생성
        RestClient.Builder builder = RestClient.builder();

        // 2. [핵심] MockServer를 이 Builder에 바인딩합니다.
        // 이렇게 하면 이 builder로 만들어지는 RestClient는 가짜 통신을 합니다.
        mockServer = MockRestServiceServer.bindTo(builder).build();

        // 3. 조작된 builder를 주입
        spendingAnalyzer = new SpendingAnalyzer(objectMapper, builder);
    }

    @Test
    @DisplayName("analyze: Groq API 호출 성공 시 JSON을 파싱하여 결과를 반환해야 한다.")
    void analyze_Success() throws JsonProcessingException {
        // given
        String month = "2024-02";
        List<TransactionDto> transactions = List.of(new TransactionDto()); // 더미 데이터

        // Groq API가 반환할 가짜 JSON 응답 구조 (LLM의 응답)
        // 주의: GroqResponse 구조에 맞춰야 함 (choices -> message -> content -> 실제 분석 JSON)
        String llmAnalysisResult = """
            {
                "summary": "절약 좀 하세요",
                "trends": [],
                "recommendations": [],
                "budgetHealth": {
                    "score": 50,
                    "status": "Warning",
                    "description": "위험함"
                }
            }
            """;
        
        // JSON 안에 JSON 문자열이 들어가는 이중 구조를 모사
        // GroqResponse 클래스의 구조: { "choices": [ { "message": { "content": "..." } } ] }
        String mockApiResponse = """
            {
                "choices": [
                    {
                        "message": {
                            "content": %s
                        }
                    }
                ]
            }
            """.formatted(objectMapper.writeValueAsString(llmAnalysisResult)); 
            // writeValueAsString을 써서 내부 JSON을 이스케이프 처리 (" -> \")

        // Mock Server 설정: 특정 URL로 POST 요청이 오면 위 JSON을 뱉어라
        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(method(POST))
                .andRespond(withSuccess(mockApiResponse, MediaType.APPLICATION_JSON));

        // when
        AnalysisDto.Response result = spendingAnalyzer.analyze(transactions, month);

        // then
        mockServer.verify();
        assertThat(result.getMonth()).isEqualTo(month);
        assertThat(result.getSummary()).isEqualTo("절약 좀 하세요");
        assertThat(result.getBudgetHealth().getScore()).isEqualTo(50);
    }

    @Test
    @DisplayName("analyze: 외부 API 호출 실패(500 에러) 시 예외를 던져야 한다.")
    void analyze_ApiError() {
        // given
        String month = "2024-02";
        List<TransactionDto> transactions = Collections.emptyList();

        // Mock Server 설정: 요청이 오면 500 에러를 뱉어라
        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withServerError());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            spendingAnalyzer.analyze(transactions, month);
        });
        
        mockServer.verify();
    }
}