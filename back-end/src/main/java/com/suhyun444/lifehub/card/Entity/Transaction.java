package com.suhyun444.lifehub.card.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.suhyun444.lifehub.card.DTO.PaymentStatus;
import com.suhyun444.lifehub.card.DTO.TransactionDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String transactionKey;
    @Column(nullable = false)
    private String date;
    @Column(nullable = false)
    private String merchant;
    @Column(nullable = false)
    private int amount;
    @Column(nullable = false)
    private String category;
    @Column(nullable = true)
    private String description;
    @Column(nullable = false)
    private PaymentStatus status;
    @Column(nullable = false)
    private String paymentMethod;
    @Builder.Default
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
}
