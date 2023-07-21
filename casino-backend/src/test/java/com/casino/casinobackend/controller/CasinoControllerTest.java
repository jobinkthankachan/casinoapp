package com.casino.casinobackend.controller;

import com.casino.casinobackend.dto.*;
import com.casino.casinobackend.exception.*;
import com.casino.casinobackend.model.Player;
import com.casino.casinobackend.model.TransactionType;
import com.casino.casinobackend.repository.PlayerRepository;
import com.casino.casinobackend.service.CasinoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(CasinoControllerTest.class)
class CasinoControllerTest {
    @MockBean
    private CasinoService casinoService;

    @MockBean
    private PlayerRepository playerRepository;
    CasinoController casinoController;
    MockMvc mockMvc;

    private static String toJsonString(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }


    @BeforeEach
    void setUp() {
        casinoController = new CasinoController(casinoService);
        mockMvc = MockMvcBuilders.standaloneSetup(casinoController).build();
    }

    @Test
    void getPlayerBalance_Success() throws Exception {

        int playerId = 1;
        BigDecimal balance = new BigDecimal(100);
        //mocking service response
        when(casinoService.getPlayerBalance(playerId)).thenReturn(balance);

        mockMvc.perform(MockMvcRequestBuilders.get("/casino/player/{playerId}/balance", playerId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(balance));

    }

    @Test
    void getPlayerBalance_InvalidPlayerId_BadRequest() throws Exception {
        int playerId = 1;
        BigDecimal balance = new BigDecimal(100);

        when(casinoService.getPlayerBalance(playerId)).thenThrow(InvalidPlayerIdException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/casino/player/{playerId}/balance", playerId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updatePlayerBalance_Success() throws Exception, InsufficientAmountException {
        int playerId = 1;
        BigDecimal balance = new BigDecimal(0);
        Player player = new Player(playerId, "tst1", balance);
        BigDecimal newBalance = new BigDecimal(100);
        PlayerTransactionDTO playerTransactionDTO = new PlayerTransactionDTO(newBalance, TransactionType.WIN);
        UpdateResponseDTO updateResponseDTO = new UpdateResponseDTO(BigInteger.ONE, newBalance, TransactionType.WIN);

        when(casinoService.updatePlayerBalance(playerId, newBalance, TransactionType.WIN)).thenReturn(updateResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/player/{playerId}/balance/update", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(playerTransactionDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(toJsonString(updateResponseDTO)));
    }

    @Test
    void updatePlayerBalance_InvalidPlayer_BadRequest() throws Exception, InsufficientAmountException {
        int unknownPlayerId = 999;
        BigDecimal newBalance = new BigDecimal(100);
        PlayerTransactionDTO playerTransactionDTO = new PlayerTransactionDTO(newBalance, TransactionType.WIN);

        when(casinoService.updatePlayerBalance(unknownPlayerId, newBalance, TransactionType.WIN)).thenThrow(InvalidPlayerIdException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/player/{playerId}/balance/update", unknownPlayerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(playerTransactionDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updatePlayerBalance_NegativeAmount_BadRequest() throws InsufficientAmountException, Exception {
        int playerId = 1;
        BigDecimal newBalance = new BigDecimal(100);
        PlayerTransactionDTO playerTransactionDTO = new PlayerTransactionDTO(newBalance, TransactionType.WIN);

        when(casinoService.updatePlayerBalance(playerId, newBalance, TransactionType.WIN)).thenThrow(NegativeAmountException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/player/{playerId}/balance/update", playerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(playerTransactionDTO))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updatePlayerBalance_WagerGreaterThanCurrentBalance() throws InsufficientAmountException, Exception {
        int playerId = 1;
        BigDecimal newBalance = new BigDecimal(100);
        PlayerTransactionDTO playerTransactionDTO = new PlayerTransactionDTO(newBalance, TransactionType.WAGER);

        when(casinoService.updatePlayerBalance(playerId, newBalance, TransactionType.WAGER)).thenThrow(InsufficientAmountException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/player/{playerId}/balance/update", playerId).contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(playerTransactionDTO))).andExpect(MockMvcResultMatchers.status().isIAmATeapot());
    }

    List<TransactionDTO> prepareTransactionDTOTest() {
        List<TransactionDTO> transactionDTOList = new ArrayList<TransactionDTO>();
        Integer counter = 0;
        BigDecimal balance = new BigDecimal(100);
        while (counter < 10) {
            TransactionDTO transactionDTO = new TransactionDTO(BigInteger.valueOf(counter.intValue()), balance, TransactionType.WIN);
            transactionDTOList.add(transactionDTO);
            counter++;
            balance.add(new BigDecimal(100));
        }
        return transactionDTOList;
    }

    @Test
    void getLastTenTransactions_Success() throws Exception {
        String username = "test";
        PlayerDTO playerDTO = new PlayerDTO(username);
        TransactionsDTO transactionsDTO = new TransactionsDTO(prepareTransactionDTOTest());

        when(casinoService.getLastTenTransactions(playerDTO)).thenReturn(transactionsDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/admin/player/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(playerDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(toJsonString(transactionsDTO)));
    }

    @Test
    void getLastTenTransactions_InvalidUsername_BadRequest() throws Exception {
        String username = "test";
        PlayerDTO playerDTO = new PlayerDTO(username);
        when(casinoService.getLastTenTransactions(playerDTO)).thenThrow(InvalidUsernameException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/admin/player/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(playerDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void addPlayer_Success() throws Exception {
        String username = "test";
        int playerId = 1;
        BigDecimal initialBalance = new BigDecimal(0);
        PlayerDTO playerDTO = new PlayerDTO(username);
        PlayerResponseDTO playerResponseDTO = new PlayerResponseDTO(playerId, username, initialBalance);

        when(casinoService.addNewPlayer(playerDTO)).thenReturn(playerResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/admin/player/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(playerDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(toJsonString(playerResponseDTO)));
    }

    @Test
    void addPlayer_UserExist_BadRequest() throws Exception {
        String username = "test";
        PlayerDTO playerDTO = new PlayerDTO(username);
        when(casinoService.addNewPlayer(playerDTO)).thenThrow(UsernameExistException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/casino/admin/player/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(playerDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}