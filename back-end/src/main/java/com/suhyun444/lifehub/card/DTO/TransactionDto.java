package com.suhyun444.lifehub.card.DTO;

import com.suhyun444.lifehub.card.Entity.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private String id;
    private String date;
    private String merchant;
    private int amount;
    private String category;
    private String description;
    private PaymentStatus status;
    private String paymentMethod;
    public static TransactionDto from(Transaction transaction) {
        return new TransactionDto(
            String.valueOf(transaction.getId()),
            transaction.getDate(),
            transaction.getMerchant(),
            transaction.getAmount(),
            transaction.getCategory(),
            transaction.getDescription(),
            transaction.getStatus(),
            transaction.getPaymentMethod()
        );
    }
}
