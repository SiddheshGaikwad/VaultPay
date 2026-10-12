package com.vaultpay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vaultpay.entity.Account;
import com.vaultpay.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	 List<Transaction> findByAccount(Account account);
}
