package com.vaultpay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultpay.dto.TransactionHistoryResponseDTO;
import com.vaultpay.dto.TransactionRequestDTO;
import com.vaultpay.dto.TransactionResponseDTO;
import com.vaultpay.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	@Autowired
	TransactionService transactionService;

	@PostMapping
	public TransactionResponseDTO createTransaction(@Valid @RequestBody TransactionRequestDTO transactionRequest,
			Authentication authentication) {

		String username = authentication.getName();

		return transactionService.createTransaction(transactionRequest, username);
	}

	@GetMapping("/account/{accountId}")
	public List<TransactionHistoryResponseDTO> getTransactionsByAccount(@PathVariable Long accountId,
			Authentication authentication) {

		String username = authentication.getName();

		return transactionService.getTransactionsByAccount(accountId, username);
	}

	@GetMapping
	public List<TransactionResponseDTO> getAllTransactions() {
		return transactionService.getAllTransactions();
	}
}