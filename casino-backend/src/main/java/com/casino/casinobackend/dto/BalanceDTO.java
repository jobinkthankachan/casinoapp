package com.casino.casinobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceDTO {
    private int playerId;
    private BigDecimal balance;

}
