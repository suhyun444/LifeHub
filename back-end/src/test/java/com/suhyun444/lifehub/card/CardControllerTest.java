package com.suhyun444.lifehub.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suhyun444.lifehub.User.CustomOAuth2UserService;
import com.suhyun444.lifehub.User.JwtTokenProvider;
import com.suhyun444.lifehub.User.OAuth2SuccessHandler;
import com.suhyun444.lifehub.card.DTO.AmountUpdateDto;
import com.suhyun444.lifehub.card.DTO.AnalysisDto;
import com.suhyun444.lifehub.card.DTO.CategoryUpdateDto;
import com.suhyun444.lifehub.card.DTO.TransactionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class CardControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // SecurityConfig 로드를 위한 필수 Mock 빈들
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private OAuth2SuccessHandler oAuth2SuccessHandler;

    // 테스트 대상 서비스 Mock
    @MockitoBean private TransactionService transactionService;

    // --- 1. GET /api/transactions (목록 조회) ---
    @Test
    @DisplayName("getTransactions: 사용자의 거래 내역 목록을 조회한다.")
    @WithMockUser(username = "test@test.com")
    void getTransactions() throws Exception {
        // given
        TransactionDto t1 = new TransactionDto();
        t1.setMerchant("Starbucks");
        t1.setAmount(5000);
        List<TransactionDto> list = List.of(t1);

        // any()를 사용하여 Principal 객체 타입 불일치 문제 해결
        given(transactionService.getTransactions(any())).willReturn(list);

        // when & then
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].merchant").value("Starbucks"))
                .andExpect(jsonPath("$[0].amount").value(5000));
    }

    // --- 2. PATCH /api/transactions/{id}/category (카테고리 수정) ---
    @Test
    @DisplayName("patchCategory: 거래 내역의 카테고리를 수정한다.")
    @WithMockUser
    void patchCategory() throws Exception {
        // given
        Long txId = 1L;
        CategoryUpdateDto request = new CategoryUpdateDto("식비");
        
        TransactionDto response = new TransactionDto();
        response.setId(txId);
        response.setCategory("식비");

        given(transactionService.updateCategory(eq(txId), eq("식비"))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/transactions/{id}/category", txId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("식비"));
    }

    // --- 3. PATCH /api/transactions/{id}/amount (금액 수정) ---
    @Test
    @DisplayName("patchAmount: 거래 내역의 금액을 수정한다.")
    @WithMockUser
    void patchAmount() throws Exception {
        // given
        Long txId = 1L;
        AmountUpdateDto request = new AmountUpdateDto(10000);

        // when & then
        mockMvc.perform(patch("/api/transactions/{id}/amount", txId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));

        verify(transactionService).updateAmount(eq(txId), eq(10000));
    }

    // --- 4. DELETE /api/transactions/{id}/delete (삭제) ---
    @Test
    @DisplayName("deleteTransaction: 거래 내역을 삭제(Soft Delete)한다.")
    @WithMockUser
    void deleteTransaction() throws Exception {
        // given
        Long txId = 1L;

        // when & then
        mockMvc.perform(delete("/api/transactions/{id}/delete", txId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));

        verify(transactionService).deleteTransaction(txId);
    }

    // --- 5. POST /api/transactions/upload (엑셀 업로드) ---
    @Test
    @DisplayName("uploadTransactionsFromExcel: 엑셀 파일을 업로드하고 파싱된 결과를 반환한다.")
    @WithMockUser(username = "test@test.com")
    void uploadTransactionsFromExcel() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.xls", 
                "application/vnd.ms-excel", 
                "dummy content".getBytes(StandardCharsets.UTF_8)
        );

        TransactionDto t1 = new TransactionDto();
        t1.setMerchant("UploadedItem");
        List<TransactionDto> parsedList = List.of(t1);

        // any()로 파일과 이메일 파라미터 처리
        given(transactionService.uploadAndParseExcel(any(), any())).willReturn(parsedList);

        // when & then
        mockMvc.perform(multipart("/api/transactions/upload")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                // 응답 구조가 Map.of("transactions", list) 이므로 $.transactions로 접근
                .andExpect(jsonPath("$.transactions[0].merchant").value("UploadedItem"));
    }

    // --- 6. DELETE /api/transactions/clear (전체 삭제) ---
    @Test
    @DisplayName("clearTransactions: 사용자의 모든 거래 내역을 삭제한다.")
    @WithMockUser
    void clearTransactions() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/transactions/clear")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success to Clear"));

        verify(transactionService).clearTransactions(any());
    }

    // --- 7. POST /api/analysis (월별 AI 분석 요청) ---
    @Test
    @DisplayName("analyzeSpending: 월별 소비 분석을 요청하고 결과를 반환한다.")
    @WithMockUser
    void analyzeSpending() throws Exception {
        // given
        AnalysisDto.Request request = new AnalysisDto.Request();
        request.setMonth("2024-02");
        request.setTransactions(List.of(new TransactionDto()));

        AnalysisDto.Response response = new AnalysisDto.Response();
        response.setMonth("2024-02");
        response.setSummary("분석 완료");

        given(transactionService.getMonthlyAnalysis(any(), any(AnalysisDto.Request.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/analysis")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("분석 완료"));
    }

    // --- 8. GET /api/analysis (분석 히스토리 조회) ---
    @Test
    @DisplayName("getAnalysis: 과거 분석 내역 목록을 조회한다.")
    @WithMockUser
    void getAnalysis() throws Exception {
        // given
        AnalysisDto.Response h1 = new AnalysisDto.Response();
        h1.setSummary("과거 기록");
        List<AnalysisDto.Response> history = List.of(h1);

        given(transactionService.getAnalysis(any())).willReturn(history);

        // when & then
        mockMvc.perform(get("/api/analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].summary").value("과거 기록"));
    }
}