package com.casino.casinobackend.service;

import com.casino.casinobackend.dto.*;
import com.casino.casinobackend.exception.*;
import com.casino.casinobackend.model.Player;
import com.casino.casinobackend.model.Transaction;
import com.casino.casinobackend.model.TransactionType;
import com.casino.casinobackend.repository.PlayerRepository;
import com.casino.casinobackend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CasinoService {

    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    PlayerRepository playerRepository;

    public PlayerResponseDTO getPlayerByUsername(String username) {
        Player player = playerRepository.findByUsername(username).orElseThrow(() -> new InvalidUsernameException("Invalid User name-" + username));
        return new PlayerResponseDTO(player.getPlayerId(), player.getUsername(), player.getBalance());
    }

    public void deletePlayerByUsername(String username) {
        Player player = playerRepository.findByUsername(username).orElseThrow(() -> new InvalidUsernameException("Invalid User name-" + username));
        playerRepository.deleteById(player.getPlayerId());
    }

    public BigDecimal getPlayerBalance(int playerId) {
        return playerRepository.findById(playerId).orElseThrow(() -> new InvalidPlayerIdException("Invalid Player id-" + playerId)).getBalance();

    }

    public synchronized UpdateResponseDTO updatePlayerBalance(int playerId, BigDecimal amount, TransactionType transactionType) throws InsufficientAmountException {

        BigDecimal newBalance;
        if (BigDecimal.ZERO.compareTo(amount) > 0)
            throw new NegativeAmountException("Negative Amount not allowed");

        Player player = playerRepository.findById(playerId).orElseThrow(() -> new InvalidPlayerIdException("Player not found with id -" + playerId));
        synchronized (player) {
            BigDecimal currentBalance = player.getBalance();

            if (transactionType.equals(TransactionType.WAGER) && amount.compareTo(currentBalance) > 0)
                throw new InsufficientAmountException("Insufficient balance for wager. Current balance: " + currentBalance);

            if (transactionType.equals(TransactionType.WAGER))
                newBalance = currentBalance.subtract(amount);
            else
                newBalance = currentBalance.add(amount);

            player.setBalance(newBalance);
            playerRepository.save(player);

            Transaction transaction = new Transaction();
            transaction.setPlayerId(playerId);
            transaction.setAmount(amount);
            transaction.setTransactionType(transactionType);
            transaction.setTransactionDate(LocalDateTime.now());
            transactionRepository.save(transaction);

            return new UpdateResponseDTO(transaction.getTransactionId(), transaction.getAmount(), transaction.getTransactionType());
        }

    }

    public TransactionsDTO getLastTenTransactions(PlayerDTO playerDTO) {
        int playerId = playerRepository.findByUsername(playerDTO.getUsername()).orElseThrow(() -> new InvalidUsernameException("Invalid username-" + playerDTO.getUsername())).getPlayerId();
        List<Transaction> transactions = transactionRepository.findLast10ByPlayerIdOrderByTransactionDateDesc(playerId);
        List<TransactionDTO> transactionDTOSResponse = transactions.stream().
                map(transaction -> new TransactionDTO(
                        transaction.getTransactionId(),
                        transaction.getAmount(),
                        transaction.getTransactionType()))
                .collect(Collectors.toList());
        return new TransactionsDTO(transactionDTOSResponse);
    }

    public PlayerResponseDTO addNewPlayer(PlayerDTO playerDTO) {
        playerRepository.findByUsername(playerDTO.getUsername()).ifPresent(existingPlayer -> {
            throw new UsernameExistException("Username exist-" + playerDTO.getUsername());
        });

        Player player = new Player();
        player.setUsername(playerDTO.getUsername());
        player.setBalance(BigDecimal.ZERO);
        Player createdPlayer = playerRepository.save(player);

        return new PlayerResponseDTO(createdPlayer.getPlayerId(), createdPlayer.getUsername(), createdPlayer.getBalance());

    }
}
