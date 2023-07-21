package com.casino.casinobackend.dto;

import com.casino.casinobackend.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTransactionDTO {
    private BigDecimal amount;
    private TransactionType transactionType;
}
