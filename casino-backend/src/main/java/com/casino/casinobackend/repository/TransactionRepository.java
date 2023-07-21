package com.casino.casinobackend.repository;

import com.casino.casinobackend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, BigInteger> {
    List<Transaction> findLast10ByPlayerIdOrderByTransactionDateDesc(int playerId);
}
