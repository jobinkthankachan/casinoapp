package com.casino.casinobackend.controller;

import com.casino.casinobackend.dto.PlayerDTO;
import com.casino.casinobackend.dto.PlayerResponseDTO;
import com.casino.casinobackend.dto.UpdateResponseDTO;
import com.casino.casinobackend.exception.UsernameExistException;
import com.casino.casinobackend.model.TransactionType;
import com.casino.casinobackend.service.CasinoService;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class CasinoConcurrencyTest {

    private static final Logger LOG = LoggerFactory.getLogger(CasinoConcurrencyTest.class);


    @Autowired
    private CasinoService casinoService;

    @RepeatedTest(50)
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testConcurrentBalanceUpdates() throws InterruptedException {

        String username = "test";
        BigDecimal initialBalance = new BigDecimal(100);
        BigDecimal wagerAmount = new BigDecimal(10);

        // Set the initial balance for the player
        PlayerDTO playerDTO = new PlayerDTO(username);
        PlayerResponseDTO playerResponseDTO;
        try {
            //create user
            playerResponseDTO = casinoService.addNewPlayer(playerDTO);
        } catch (UsernameExistException e) {
            //delete the user if exist to test the remaining iterations
            casinoService.deletePlayerByUsername(username);
            //create user
            playerResponseDTO = casinoService.addNewPlayer(playerDTO);

        }


        int playerId = playerResponseDTO.getPlayerId();
        LOG.debug("playerId value - {}", playerId);
        UpdateResponseDTO u = casinoService.updatePlayerBalance(playerResponseDTO.getPlayerId(), initialBalance, TransactionType.WIN);
        LOG.debug("Player Balance -{}", u.getBalance());
        // Create two concurrent threads to update the player's balance
        CountDownLatch latch = new CountDownLatch(2);

        Thread thread1 = new Thread(() -> {
            try {
                casinoService.updatePlayerBalance(playerId, wagerAmount, TransactionType.WAGER);
                LOG.debug("Updated Balance Thread 1 -{}", casinoService.getPlayerBalance(playerId));
            } finally {
                latch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                casinoService.updatePlayerBalance(playerId, wagerAmount, TransactionType.WAGER);
                LOG.debug("Updated Balance Thread 2 -{}", casinoService.getPlayerBalance(playerId));
            } finally {
                latch.countDown();
            }
        });

        // Start the concurrent threads
        thread1.start();
        thread2.start();

        // Wait for both threads to finish
        latch.await();

        // Fetch the player's updated balance from the database
        BigDecimal updatedPlayerBalance = casinoService.getPlayerBalance(playerId);
        // Calculate the expected final balance after concurrent wagers
        BigDecimal expectedFinalBalance = initialBalance.subtract(wagerAmount).subtract(wagerAmount);

        // Assert that the final balance matches the expected balance
        assertEquals(expectedFinalBalance.floatValue(), updatedPlayerBalance.floatValue());

    }
}
