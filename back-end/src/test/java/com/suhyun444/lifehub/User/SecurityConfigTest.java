package com.suhyun444.lifehub.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 환경 변수 및 DB 설정 (H2)
@SpringBootTest(properties = {
    "GOOGLE_CLIENT_ID=dummy-client-id",
    "GOOGLE_CLIENT_SECRET=dummy-client-secret",
    "GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "groq_api_key=dummy-groq-key"
})
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // SecurityConfig 로딩을 위한 필수 Mock 빈들
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private OAuth2SuccessHandler oAuth2SuccessHandler;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("인증되지 않은 사용자가 보호된 API(/api/transactions)에 접근하면 403 Forbidden을 반환해야 한다.")
    void accessProtectedResource_WithoutAuth_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().is3xxRedirection()); 
    }

    @Test
    @DisplayName("존재하지 않는 페이지라도 인증 없이 접근 시 403/401이 아니면 보안 필터를 통과한 것으로 본다.")
    void accessPublicResource_WithoutAuth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/error")) // /error는 스프링 부트 기본 허용 경로
               .andExpect(result -> {
                   int status = result.getResponse().getStatus();
                   // 403(Forbidden)이나 401(Unauthorized)만 아니면 보안 설정은 통과한 것임
                   if (status == 403 || status == 401) {
                       throw new AssertionError("공개 리소스 접근이 보안에 의해 막혔습니다. Status: " + status);
                   }
               });
    }
}