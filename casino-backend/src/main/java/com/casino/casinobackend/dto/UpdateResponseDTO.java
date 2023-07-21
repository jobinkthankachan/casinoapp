package com.casino.casinobackend.dto;

import com.casino.casinobackend.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateResponseDTO {
    private BigInteger transactionId;
    private BigDecimal balance;
    private TransactionType transactionType;
}
