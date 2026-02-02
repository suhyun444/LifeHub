/*
 * https://v0.app/chat/payment-history-website-rrH9UZjMPzs
 * https://www.perplexity.ai/search/web-gaebalhalddae-peuronteuneu-jWN91u2mTfqV7HqciyateA
 * https://chatgpt.com/c/68c64fb3-1430-8325-968d-0f26d2b48bcc
 */
package com.suhyun444.lifehub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.suhyun444.lifehub.DTO.AmountUpdateDTO;
import com.suhyun444.lifehub.DTO.AnalysisDto;
import com.suhyun444.lifehub.DTO.CategoryUpdateDTO;
import com.suhyun444.lifehub.DTO.PaymentStatus;
import com.suhyun444.lifehub.DTO.TransactionDto;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class MainController {

    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("api/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
        
        return ResponseEntity.ok(Map.of(
            "message", "Logged in successfully",
            "user", principal // 사용자 ID 또는 정보 반환
        ));
    }
    @GetMapping("api/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactions(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(transactionService.getTransactions(email));
    }
    
    @PatchMapping("api/transactions/{id}/category")
    public ResponseEntity<TransactionDto> patchCategory(@PathVariable Long id,@RequestBody CategoryUpdateDTO request) {        
        return ResponseEntity.ok(transactionService.updateCategory(id,request.category()));
    }
    @PatchMapping("api/transactions/{id}/amount")
    public ResponseEntity<?> patchAmount(@PathVariable Long id,@RequestBody AmountUpdateDTO request) {        
        transactionService.updateAmount(id,request.amount());
        return ResponseEntity.ok(Map.of("message", "Success"));
    }
    @DeleteMapping("api/transactions/{id}/delete")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(Map.of("message", "Success"));
    }
    @PostMapping("api/transactions/upload")
    public ResponseEntity<?> uploadTransactionsFromExcel(@RequestParam("file") MultipartFile file, 
                                                        @AuthenticationPrincipal String email) {
      try {

            List<TransactionDto> transactions = transactionService.uploadAndParseExcel(file,email);
            return ResponseEntity.ok(Map.of("transactions", transactions));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to parse Excel file"));
        }
    }
    @DeleteMapping("api/transactions/clear")
    public ResponseEntity<?> clearTransactions(@AuthenticationPrincipal String email)
    {
        try
        {
            transactionService.clearTransactions(email);
            return ResponseEntity.ok(Map.of("message","Success to Clear"));
        }
        catch(Exception e)
        {
            return ResponseEntity.status(500).body(Map.of("message","Failed to Clear"));
        }
    }
    @PostMapping("api/analysis")
    public ResponseEntity<AnalysisDto.Response> analyzeSpending(@RequestBody AnalysisDto.Request request, 
                                                                @AuthenticationPrincipal String email) 
    {
        return ResponseEntity.ok(transactionService.getMonthlyAnalysis(email,request));
    }
    @GetMapping("api/analysis")
    public ResponseEntity<List<AnalysisDto.Response>> getAnalysis(@AuthenticationPrincipal String email) {        
        return ResponseEntity.ok(transactionService.getAnalysis(email));
    }
       
}