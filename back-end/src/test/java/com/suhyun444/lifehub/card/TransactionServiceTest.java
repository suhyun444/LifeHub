package com.suhyun444.lifehub.card;

import com.suhyun444.lifehub.User.UserRepository;
import com.suhyun444.lifehub.card.Component.SpendingAnalyzer;
import com.suhyun444.lifehub.card.Component.TransactionCategorizer;
import com.suhyun444.lifehub.card.DTO.AnalysisDto;
import com.suhyun444.lifehub.card.DTO.TransactionDto;
import com.suhyun444.lifehub.card.Entity.AnalysisHistory;
import com.suhyun444.lifehub.card.Entity.Transaction;
import com.suhyun444.lifehub.card.Entity.User;
import com.suhyun444.lifehub.card.Repository.AnalysisHistoryRepository;
import com.suhyun444.lifehub.card.Repository.TransactionRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AnalysisHistoryRepository analysisHistoryRepository;
    @Mock private TransactionCategorizer transactionCategorizer;
    @Mock private SpendingAnalyzer spendingAnalyzer;

    @InjectMocks
    private TransactionService transactionService;

    // ==========================================
    // 1. uploadAndParseExcel (엑셀 업로드)
    // ==========================================

    @Test
    @DisplayName("uploadAndParseExcel: (성공) 정상 파일 업로드 시 파싱, 분류, 저장이 수행되어야 한다.")
    void uploadAndParseExcel_Success() throws Exception {
        // given
        String email = "test@test.com";
        User user = new User(email); user.setId(1L);
        MockMultipartFile file = createMockExcelFile();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(transactionRepository.findExistingKeys(anyList())).willReturn(Collections.emptySet()); // 중복 없음
        given(transactionCategorizer.getCategory(any(), any())).willReturn("식비");
        
        // saveAll 호출 후, 결과 조회를 위한 Mock
        Transaction t = new Transaction(); t.setAmount(10000); t.setCategory("식비");
        given(transactionRepository.findByUserIdAndIsDeletedFalse(user.getId())).willReturn(List.of(t));

        // when
        List<TransactionDto> result = transactionService.uploadAndParseExcel(file, email);

        // then
        assertThat(result).hasSize(1);
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("uploadAndParseExcel: (실패) 파일이 비어있거나 null이면 예외를 던져야 한다.")
    void uploadAndParseExcel_EmptyFile() {
        // given
        String email = "test@test.com";
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> 
            transactionService.uploadAndParseExcel(emptyFile, email));
        
        assertThrows(IllegalArgumentException.class, () -> 
            transactionService.uploadAndParseExcel(null, email));
    }

    @Test
    @DisplayName("uploadAndParseExcel: (실패) 존재하지 않는 이메일로 요청 시 예외가 발생해야 한다.")
    void uploadAndParseExcel_UserNotFound() throws IOException {
        // given
        String email = "unknown@test.com";
        MockMultipartFile file = createMockExcelFile();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> 
            transactionService.uploadAndParseExcel(file, email));
    }

    // ==========================================
    // 2. getTransactions (조회)
    // ==========================================

    @Test
    @DisplayName("getTransactions: (성공) 사용자의 삭제되지 않은 거래 내역을 반환해야 한다.")
    void getTransactions_Success() {
        // given
        String email = "user@test.com";
        User user = new User(email); user.setId(1L);
        Transaction t1 = new Transaction(); t1.setId(1L);
        Transaction t2 = new Transaction(); t2.setId(2L);
        
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(transactionRepository.findByUserIdAndIsDeletedFalse(user.getId())).willReturn(List.of(t1, t2));

        // when
        List<TransactionDto> result = transactionService.getTransactions(email);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getTransactions: (실패) 사용자를 찾을 수 없으면 예외를 던져야 한다.")
    void getTransactions_UserNotFound() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> 
            transactionService.getTransactions("unknown"));
    }

    // ==========================================
    // 3. updateAmount (금액 수정)
    // ==========================================

    @Test
    @DisplayName("updateAmount: (성공) 거래 ID와 금액이 주어지면 해당 거래의 금액을 업데이트해야 한다.")
    void updateAmount_Success() {
        // given
        Long id = 1L;
        int newAmount = 50000;
        Transaction mockTx = new Transaction();
        given(transactionRepository.findById(id)).willReturn(Optional.of(mockTx));

        // when
        transactionService.updateAmount(id, newAmount);

        // then
        assertThat(mockTx.getAmount()).isEqualTo(newAmount);
    }

    @Test
    @DisplayName("updateAmount: (실패) 존재하지 않는 거래 ID면 예외가 발생해야 한다.")
    void updateAmount_NotFound() {
        // given
        given(transactionRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> 
            transactionService.updateAmount(999L, 1000));
    }

    // ==========================================
    // 4. deleteTransaction (삭제 - Soft Delete)
    // ==========================================

    @Test
    @DisplayName("deleteTransaction: (성공) 거래 삭제 시 DB 삭제가 아닌 isDeleted 플래그를 true로 변경해야 한다.")
    void deleteTransaction_Success() {
        // given
        Long id = 1L;
        Transaction mockTx = new Transaction();
        mockTx.setIsDeleted(false);
        given(transactionRepository.findById(id)).willReturn(Optional.of(mockTx));

        // when
        transactionService.deleteTransaction(id);

        // then
        assertThat(mockTx.getIsDeleted()).isTrue();
        verify(transactionRepository, never()).deleteById(anyLong()); // 하드 삭제 호출 안됨 확인
    }

    @Test
    @DisplayName("deleteTransaction: (실패) 거래 ID가 없으면 예외가 발생해야 한다.")
    void deleteTransaction_NotFound() {
        given(transactionRepository.findById(anyLong())).willReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> transactionService.deleteTransaction(1L));
    }

    // ==========================================
    // 5. updateCategory (카테고리 수정)
    // ==========================================

    @Test
    @DisplayName("updateCategory: (성공) 카테고리 수정 후 변경된 DTO를 반환해야 한다.")
    void updateCategory_Success() {
        // given
        Long id = 1L;
        String newCat = "교통";
        Transaction mockTx = new Transaction();
        mockTx.setCategory("기타");
        given(transactionRepository.findById(id)).willReturn(Optional.of(mockTx));

        // when
        TransactionDto result = transactionService.updateCategory(id, newCat);

        // then
        assertThat(mockTx.getCategory()).isEqualTo(newCat);
        assertThat(result.getCategory()).isEqualTo(newCat);
    }

    @Test
    @DisplayName("updateCategory: (실패) 거래 ID가 없으면 예외가 발생해야 한다.")
    void updateCategory_NotFound() {
        given(transactionRepository.findById(anyLong())).willReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> transactionService.updateCategory(1L, "카테고리"));
    }

    // ==========================================
    // 6. clearTransactions (전체 삭제)
    // ==========================================

    @Test
    @DisplayName("clearTransactions: (성공) 사용자의 모든 거래 내역을 삭제해야 한다.")
    void clearTransactions_Success() throws Exception {
        // given
        String email = "user@test.com";
        User user = new User(email); user.setId(10L);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        transactionService.clearTransactions(email);

        // then
        verify(transactionRepository).deleteByUserId(user.getId());
    }

    @Test
    @DisplayName("clearTransactions: (실패) 사용자 정보가 없으면 예외가 발생해야 한다.")
    void clearTransactions_UserNotFound() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> transactionService.clearTransactions("unknown"));
    }

    // ==========================================
    // 7. getMonthlyAnalysis (월별 분석 요청)
    // ==========================================

    @Test
    @DisplayName("getMonthlyAnalysis: (성공) 분석 이력이 없으면 새로 저장하고 결과를 반환한다.")
    void getMonthlyAnalysis_NewHistory() {
        // given
        String email = "test@test.com";
        String month = "2024-02";
        User user = new User(email); user.setId(1L);
        
        AnalysisDto.Request req = new AnalysisDto.Request();
        req.setMonth(month);
        req.setTransactions(List.of(new TransactionDto())); // 빈 리스트 아님

        AnalysisDto.Response res = new AnalysisDto.Response();
        res.setSummary("굿");
        res.setBudgetHealth(new AnalysisDto.BudgetHealth(80, "Good", "양호합니다")); // 필수 데이터 주입
        res.setMonth("2024-02");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(spendingAnalyzer.analyze(anyList(), eq(month))).willReturn(res);
        given(analysisHistoryRepository.findByUserIdAndMonth(user.getId(), month)).willReturn(Optional.empty());

        // when
        transactionService.getMonthlyAnalysis(email, req);

        // then
        verify(analysisHistoryRepository).save(any(AnalysisHistory.class));
    }

    @Test
    @DisplayName("getMonthlyAnalysis: (성공) 분석 이력이 이미 있으면 업데이트한다.")
    void getMonthlyAnalysis_UpdateHistory() {
        // given
        String email = "test@test.com";
        String month = "2024-02";
        User user = new User(email);
        
        AnalysisHistory mockHistory = mock(AnalysisHistory.class);
        AnalysisDto.Response res = new AnalysisDto.Response();

        AnalysisDto.Request req = new AnalysisDto.Request();
        req.setMonth(month);
        req.setTransactions(List.of(new TransactionDto()));

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(spendingAnalyzer.analyze(anyList(), eq(month))).willReturn(res);
        given(analysisHistoryRepository.findByUserIdAndMonth(any(), eq(month))).willReturn(Optional.of(mockHistory));

        // when
        transactionService.getMonthlyAnalysis(email, req);

        // then
        verify(mockHistory).update(res);
        verify(analysisHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMonthlyAnalysis: (실패) 거래 내역이 없으면 분석을 수행하지 않고 예외를 던진다.")
    void getMonthlyAnalysis_NoTransactions() {
        // given
        AnalysisDto.Request req = new AnalysisDto.Request();
        req.setTransactions(null); // or empty list

        // when & then
        assertThrows(IllegalArgumentException.class, () -> 
            transactionService.getMonthlyAnalysis("email", req));
    }

    // ==========================================
    // 8. getAnalysis (분석 내역 조회)
    // ==========================================

    @Test
    @DisplayName("getAnalysis: (성공) 사용자의 모든 분석 히스토리를 DTO로 변환하여 반환한다.")
    void getAnalysis_Success() {
        // given
        String email = "user@test.com";
        User user = new User(email); user.setId(1L);
        AnalysisDto.Response mockResponse = new AnalysisDto.Response();
        mockResponse.setBudgetHealth(new AnalysisDto.BudgetHealth(80, "Good", "양호합니다")); // 필수 데이터 주입
        mockResponse.setMonth("2024-02");
        
        // Mock History 객체 생성 (Response 필드가 필요함)
        AnalysisHistory h1 = AnalysisHistory.builder().user(user).response(mockResponse).build();
        
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(analysisHistoryRepository.findByUserId(user.getId())).willReturn(Optional.of(List.of(h1)));

        // when
        List<AnalysisDto.Response> result = transactionService.getAnalysis(email);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAnalysis: (실패) 히스토리를 찾을 수 없으면(Null 등) 예외를 던진다.")
    void getAnalysis_HistoryNotFound() {
        // given
        User user = new User("t");
        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
        given(analysisHistoryRepository.findByUserId(any())).willReturn(Optional.empty()); // orElseThrow 동작 확인

        // when & then
        assertThrows(NoSuchElementException.class, () -> transactionService.getAnalysis("t"));
    }


    // --- Helper Method ---
    private MockMultipartFile createMockExcelFile() throws IOException {
        try (Workbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet();
            for (int i = 1; i < 5; i++) sheet.createRow(i); 

            Row row = sheet.createRow(5);
            row.createCell(0).setCellValue("2024.02.15");
            row.createCell(2).setCellValue("테스트상점");
            row.createCell(4).setCellValue(10000); // 숫자형
            row.createCell(7).setCellValue("카드");

            sheet.createRow(6);
            workbook.write(bos);
            
            return new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", bos.toByteArray());
        }
    }
}