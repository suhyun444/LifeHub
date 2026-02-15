package com.suhyun444.lifehub.card;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SpendingAnalyzerTest {

    private SpendingAnalyzer spendingAnalyzer;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // RestClient.Builder를 사용하여 테스트용 Client 생성
        RestClient.Builder builder = RestClient.builder();
        spendingAnalyzer = new SpendingAnalyzer(objectMapper, builder);
        
        // MockServer 바인딩 (생성된 RestClient에 가짜 응답을 주입하기 위함)
        // 주의: 실제로는 SpendingAnalyzer 내부에서 build()를 호출하므로, 
        // 테스트 용이성을 위해 RestClient 자체를 주입받도록 수정하는 것이 가장 베스트입니다.
        // 여기서는 빌더 패턴 리팩토링 버전을 가정합니다.
        
        // *더 쉬운 테스트를 위한 팁*: SpendingAnalyzer 생성자에서 RestClient 자체를 받게 수정하세요.
        // 이 테스트 코드는 RestClient 주입 방식을 가정하고 작성합니다.
    }

    // (참고: RestClient.Builder 패턴 테스트는 설정이 복잡하므로, 
    //  SpendingAnalyzer 생성자를 public SpendingAnalyzer(ObjectMapper om, RestClient client) 로
    //  바꾸시는 걸 강력 추천합니다. 아래는 그 가정 하의 테스트입니다.)
    
    /* @Test
    @DisplayName("analyze: Groq API 호출 성공 시 분석 결과를 반환해야 한다.")
    void analyze_Success() {
        // ... MockServer 설정 및 검증 로직 ...
        // (이 부분은 SpendingAnalyzer 리팩토링 방향 확정 후 제공하는 것이 정확합니다.)
    }
    */
}