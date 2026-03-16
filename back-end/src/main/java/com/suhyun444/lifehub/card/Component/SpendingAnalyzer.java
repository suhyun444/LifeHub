package com.suhyun444.lifehub.card.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suhyun444.lifehub.card.DTO.AnalysisDto;
import com.suhyun444.lifehub.card.DTO.GroqResponse;
import com.suhyun444.lifehub.card.DTO.GroqTransactionDto;
import com.suhyun444.lifehub.card.DTO.TransactionDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SpendingAnalyzer {

    @Value("${groq_api_key}")
    private String groqApiKey;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public SpendingAnalyzer(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
    }

    // 외부 의존성 없이 순수하게 Transaction 리스트만 받아서 분석 결과 리턴
    public AnalysisDto.Response analyze(List<TransactionDto> transactions, String month) {
        try {
            String systemPrompt = """
            당신은 20년 경력의 냉철하고 독설가인 '재무 컨설턴트'입니다.
            사용자의 거래 내역을 분석하여 JSON 형식으로 응답하세요.
            
            [분석 원칙 - 반드시 지킬 것]
            1. **뻔한 소리 금지**: "지출을 줄이세요", "아껴 쓰세요" 같은 초등학생도 할 수 있는 조언은 절대 하지 마세요.
            2. **구체적 지적**: "식비가 많습니다" 대신 "식비가 n만원에 전체 지출의 n%를 차지합니다"처럼 수치을 콕 집어 말하세요.
            3. **간편결제 주의**: 기타 카테고리가 많다면 "무지성 간편 결제 중독"이나 "출처 불명의 송금 내역"을 경고하세요.
            4. **재정 모드**: 한달에 70만원 정도면 아끼고 사는겁니다. 한달 70만원을 기준으로 과소비, 적절을 판단해주세요
            
            [JSON 포맷 가이드]
            - summary: 전체적인 소비 행태에 대한 3문장 요약 (냉소적인 어조)
            - trends: 눈에 띄는 소비 변화 3가지 (구체적인 상점명 언급 필수)
            - recommendations: 실천 가능한 구체적 행동 강령 3가지 (예: "택시 앱 삭제", "커피 머신 구매 고려")
            - budgetHealth: score(0~100), status(Critical/Warning/Good/Excellent), description(한 줄 평가)
            
            [필수 JSON 응답 예시]
            {
            "summary": "숨만 쉬어도 나가는 고정비가 너무 많습니다. 특히 습관적으로 긁는 식비에 당신의 통장을 갉아먹고 있습니다. 정신 차리세요.",
            "trends": [
                { "type": "increase", "category": "식비", "change": "+45%", "description": "커피값으로만 10만원 증발" },
                { "type": "stable", "category": "교통", "change": "0%", "description": "값싼 대중교통 이용이 부족함" }
            ],
            "recommendations": [
                { "title": "배달 앱 삭제", "description": "지금 당장 배달의민족 앱을 지우고 밀키트를 주문하세요.", "priority": "high" },
                { "title": "간편결제 연동 해제", "description": "카카오페이 충전 계좌 연결을 끊으세요. 결제 과정이 귀찮아야 돈을 덜 씁니다.", "priority": "medium" }
            ],
            "budgetHealth": {
                "score": 45,
                "status": "Warning",
                "description": "이대로 가다간 다음 달 카드값 못 냅니다."
            }
            }
            """;

            List<GroqTransactionDto> maskedTransactions = transactions.stream().map(GroqTransactionDto::from).collect(Collectors.toList());
            String userPrompt = String.format("내역:\n%s", 
                     objectMapper.writeValueAsString(maskedTransactions));

            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.1
            );

            GroqResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(GroqResponse.class);

            log.info("AI Raw Response: {}", response);

            String content = response.choices().get(0).message().content();
            
            AnalysisDto.Response result = objectMapper.readValue(content, AnalysisDto.Response.class);
            result.setMonth(month);
            return result;
        } catch (Exception e) {
            log.error("AI Analysis Failed", e);
            throw new RuntimeException("Analyzer Error: " + e.getMessage());
        }
    }
}