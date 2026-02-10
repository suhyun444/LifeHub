package com.suhyun444.lifehub.card.Repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suhyun444.lifehub.card.DTO.MerchantCategoryDto;
import com.suhyun444.lifehub.card.Entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long>{
    @Query("SELECT new com.suhyun444.lifehub.card.DTO.MerchantCategoryDto(t.merchant, t.category) " +
           "FROM Transaction t " +
           "WHERE t.merchant IN :merchants " +
           "ORDER BY t.date DESC")
    List<MerchantCategoryDto> findCategoriesByMerchantsOrderByDateDesc(@Param("merchants") List<String> merchants);

    @Query("SELECT t.transactionKey FROM Transaction t WHERE t.transactionKey IN :keys")
    Set<String> findExistingKeys(@Param("keys") List<String> keys);

    void deleteByUserId(Long userId);
    List<Transaction> findByUserIdAndIsDeletedFalse(Long userId);
}
