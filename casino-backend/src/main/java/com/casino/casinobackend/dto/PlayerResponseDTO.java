package com.casino.casinobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class PlayerResponseDTO {
    private int playerId;
    private String username;
    private BigDecimal balance;
}
