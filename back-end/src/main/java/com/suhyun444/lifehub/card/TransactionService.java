package com.suhyun444.lifehub.card;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.suhyun444.lifehub.User.UserRepository;
import com.suhyun444.lifehub.card.Component.SpendingAnalyzer;
import com.suhyun444.lifehub.card.Component.TransactionCategorizer;
import com.suhyun444.lifehub.card.DTO.AnalysisDto;
import com.suhyun444.lifehub.card.DTO.CategoryUpdateDto;
import com.suhyun444.lifehub.card.DTO.MerchantCategoryDto;
import com.suhyun444.lifehub.card.DTO.TransactionDto;
import com.suhyun444.lifehub.card.Entity.AnalysisHistory;
import com.suhyun444.lifehub.card.Entity.Transaction;
import com.suhyun444.lifehub.card.Entity.User;
import com.suhyun444.lifehub.card.Parser.KookminTransactionParser;
import com.suhyun444.lifehub.card.Parser.TransactionParser;
import com.suhyun444.lifehub.card.Repository.AnalysisHistoryRepository;
import com.suhyun444.lifehub.card.Repository.TransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final TransactionCategorizer transactionCategorizer;
    private final SpendingAnalyzer spendingAnalyzer;
    private static final Set<String> AMBIGUOUS_MERCHANTS = Set.of(
        "네이버페이", "카카오페이", "토스", "PAYCO", 
        "KG이니시스", "다날", "NICE페이", "KCP"
    );

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              TransactionCategorizer transactionCategorizer,
                              SpendingAnalyzer spendingAnalyzer,
                              AnalysisHistoryRepository analysisHistoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.transactionCategorizer = transactionCategorizer;
        this.spendingAnalyzer = spendingAnalyzer;
        this.analysisHistoryRepository = analysisHistoryRepository;
    }   

    @Transactional
    public List<TransactionDto> uploadAndParseExcel(MultipartFile file, String email) throws Exception
    {
        TransactionParser parser = new KookminTransactionParser();
        List<Transaction> transactions;
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        try (InputStream is = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            transactions = parser.parse(sheet);
            categorizeTransactions(transactions);
            
            User user = userRepository.findByEmail(email).orElseThrow();
            
            importTransactions(transactions,user);
            List<TransactionDto> result = transactionRepository.findByUserIdAndIsDeletedFalse(user.getId()).stream().map(TransactionDto::from).collect(Collectors.toList());
            return result;
        }
    }
    @Transactional
    public List<TransactionDto> getTransactions(String email)
    {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Transaction> transactions = transactionRepository.findByUserIdAndIsDeletedFalse(user.getId());
        List<TransactionDto> result = transactions.stream().map(TransactionDto::from).collect(Collectors.toList());
        return result;
    } 
    @Transactional
    public void updateAmount(Long id,int amount)
    {
        Transaction transaction = transactionRepository.findById(id).orElseThrow();
        transaction.setAmount(amount);
    }
    @Transactional
    public void deleteTransaction(Long id)
    {
        Transaction transaction = transactionRepository.findById(id).orElseThrow();
        transaction.setIsDeleted(true);
    }
    @Transactional
    public TransactionDto updateCategory(Long id,String newCategory)
    {
        Transaction transaction = transactionRepository.findById(id).orElseThrow();
        transaction.setCategory(newCategory);

        return TransactionDto.from(transaction);
    }
    @Transactional
    public void clearTransactions(String email) throws Exception
    {
        User user = userRepository.findByEmail(email).orElseThrow();
        transactionRepository.deleteByUserId(user.getId());
        return;
    }

    private void importTransactions(List<Transaction> transactions,User user)
    {
        List<String> keys = transactions.stream()
            .map(Transaction::getTransactionKey)
            .collect(Collectors.toList());

        Set<String> existingKeys = transactionRepository.findExistingKeys(keys);

        List<Transaction> newTransactions = transactions.stream()
                                            .filter(transaction->!existingKeys.contains(transaction.getTransactionKey()))
                                            .collect(Collectors.toList());
        newTransactions.forEach(t->t.setUser(user));
        transactionRepository.saveAll(newTransactions);
        return ;
    }
    private void categorizeTransactions(List<Transaction> transactions) {
        List<String> uniqueMerchants = transactions.stream().map(Transaction::getMerchant).filter(m -> !AMBIGUOUS_MERCHANTS.contains(m)).distinct().collect(Collectors.toList());
        
        Map<String, String> historyMap = transactionRepository.findCategoriesByMerchantsOrderByDateDesc(uniqueMerchants).stream()
                                                                .collect(Collectors.toMap(
                                                                            MerchantCategoryDto::merchant,
                                                                            MerchantCategoryDto::category,
                                                                            (existing, replacement) -> existing ));

        transactions.forEach(t -> {
            Optional<String> historicalCategory = Optional.ofNullable(historyMap.get(t.getMerchant()));
            String finalCategory = transactionCategorizer.getCategory(t.getMerchant(), historicalCategory);
            t.setCategory(finalCategory);
        });
    }
    @Transactional
    public AnalysisDto.Response getMonthlyAnalysis(String email,AnalysisDto.Request request) {
        if (request.getTransactions() == null || request.getTransactions().isEmpty()) {
            throw new IllegalArgumentException("거래 내역이 없습니다.");
        }

        AnalysisDto.Response analysisResult = spendingAnalyzer.analyze(
                request.getTransactions(), 
                request.getMonth()
        );
        User user = userRepository.findByEmail(email).orElseThrow();


        analysisHistoryRepository.findByUserIdAndMonth(user.getId(), request.getMonth())
        .ifPresentOrElse(
            (existingHistory) -> {
                existingHistory.update(analysisResult);
            },
            () -> {
                AnalysisHistory newHistory = AnalysisHistory.builder()
                        .user(user)
                        .response(analysisResult)
                        .build();
                analysisHistoryRepository.save(newHistory);
            }
        );

        return analysisResult;
    }
    public List<AnalysisDto.Response> getAnalysis(String email)
    {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<AnalysisHistory> histories = analysisHistoryRepository.findByUserId(user.getId()).orElseThrow();
        
        return histories.stream().map(AnalysisDto.Response::from).collect(Collectors.toList());
    }
}
