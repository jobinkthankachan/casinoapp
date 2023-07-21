package com.casino.casinobackend.controller;

import com.casino.casinobackend.dto.*;
import com.casino.casinobackend.exception.*;
import com.casino.casinobackend.service.CasinoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/casino")
public class CasinoController {

    CasinoService casinoService;

    public CasinoController(CasinoService casinoService) {
        this.casinoService = casinoService;
    }

    @GetMapping("/player/{playerId}/balance")
    public ResponseEntity<BalanceDTO> getPlayerBalance(@PathVariable int playerId) {
        try {
            BigDecimal balance = casinoService.getPlayerBalance(playerId);
            return ResponseEntity.ok(new BalanceDTO(playerId, balance));
        } catch (InvalidPlayerIdException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/player/{playerId}/balance/update")
    public ResponseEntity<UpdateResponseDTO> updatePlayerBalance(@PathVariable int playerId,
                                                                 @RequestBody PlayerTransactionDTO playerTransactionDTO) {
        try {
            UpdateResponseDTO updateResponseDTO = casinoService.updatePlayerBalance(playerId, playerTransactionDTO.getAmount(), playerTransactionDTO.getTransactionType());
            return ResponseEntity.ok(updateResponseDTO);
        } catch (InvalidPlayerIdException | NegativeAmountException e) {
            return ResponseEntity.badRequest().build();
        } catch (InsufficientAmountException e) {
            return ResponseEntity.status(418).build();
        }
    }

    @PostMapping("/admin/player/transactions")
    public ResponseEntity<TransactionsDTO> getLastTenTransactions(@RequestBody PlayerDTO playerDTO) {
        try {
            TransactionsDTO transactionsDTO = casinoService.getLastTenTransactions(playerDTO);
            return ResponseEntity.ok(transactionsDTO);
        } catch (InvalidUsernameException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/player/add")
    public ResponseEntity<PlayerResponseDTO> addPlayer(@RequestBody PlayerDTO playerDTO) {
        try {
            PlayerResponseDTO createdPlayer = casinoService.addNewPlayer(playerDTO);
            return ResponseEntity.ok(createdPlayer);
        } catch (UsernameExistException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
