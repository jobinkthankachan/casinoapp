package com.casino.casinobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TransactionsDTO {
    List<TransactionDTO> transactions;
}
