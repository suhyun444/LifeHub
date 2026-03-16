package com.suhyun444.lifehub.card.DTO;

import com.suhyun444.lifehub.card.Entity.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroqTransactionDto {
    private int amount;
    private String category;
    public static GroqTransactionDto from(TransactionDto transactiondDto) {
        return new GroqTransactionDto(
            transactiondDto.getAmount(),
            transactiondDto.getCategory()
        );
    }
}
