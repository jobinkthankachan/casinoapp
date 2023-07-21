package com.casino.casinobackend.dto;

import com.casino.casinobackend.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@AllArgsConstructor
@Data
public class TransactionDTO {
    private BigInteger transactionId;
    private BigDecimal balance;
    @JsonIgnore
    private TransactionType transactionType;
}
