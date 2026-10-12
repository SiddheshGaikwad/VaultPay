package com.vaultpay.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultpay.dto.TransactionHistoryResponseDTO;
import com.vaultpay.dto.TransactionRequestDTO;
import com.vaultpay.dto.TransactionResponseDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.entity.LedgerEntry;
import com.vaultpay.entity.Transaction;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.exception.ResourceNotFoundException;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.LedgerEntryRepository;
import com.vaultpay.repository.TransactionRepository;
import com.vaultpay.repository.UserRepository;
import java.util.UUID;

@Service
public class TransactionService {

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	LedgerEntryRepository ledgerEntryRepository;

	@Transactional
	public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequest, String username) {

		// Find logged-in user
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Find account
		Account account = accountRepository.findByIdForUpdate(transactionRequest.getAccountId())
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		// Check account ownership
		if (!account.getUser().getId().equals(user.getId())) {
			throw new BadRequestException("You are not allowed to access this account");
		}

		if (!account.getStatus().equals("ACTIVE")) {
			throw new BadRequestException("Account is not active");
		}
		// Validate amount
		if (transactionRequest.getAmount().signum() <= 0) {
			throw new BadRequestException("Amount must be greater than zero");
		}

		String type = transactionRequest.getType();

		// Validate transaction type
		if (!type.equals("DEPOSIT") && !type.equals("WITHDRAWAL")) {

			throw new BadRequestException("Invalid transaction type");
		}

		// DEPOSIT
		if (type.equals("DEPOSIT")) {

			account.setBalance(account.getBalance().add(transactionRequest.getAmount()));

			accountRepository.save(account);
		}

		// WITHDRAWAL
		if (type.equals("WITHDRAWAL")) {

			if (account.getBalance().compareTo(transactionRequest.getAmount()) < 0) {

				throw new BadRequestException("Insufficient balance");
			}

			account.setBalance(account.getBalance().subtract(transactionRequest.getAmount()));

			accountRepository.save(account);
		}

		// Create transaction
		Transaction transaction = new Transaction();

		transaction.setAccount(account);
		transaction.setAmount(transactionRequest.getAmount());
		transaction.setType(type);
		transaction.setTransactionReference("TXN-" + UUID.randomUUID());
		transaction.setStatus("SUCCESS");
		transaction.setCreatedAt(LocalDateTime.now());

		Transaction savedTransaction = transactionRepository.save(transaction);

		// Create ledger entry
		LedgerEntry ledgerEntry = new LedgerEntry();

		ledgerEntry.setAccount(account);
		ledgerEntry.setAmount(transactionRequest.getAmount());

		ledgerEntry.setTransactionReference(savedTransaction.getTransactionReference());

		ledgerEntry.setCreatedAt(LocalDateTime.now());

		if (type.equals("DEPOSIT")) {
			ledgerEntry.setEntryType("CREDIT");
		} else {
			ledgerEntry.setEntryType("DEBIT");
		}

		ledgerEntryRepository.save(ledgerEntry);

		// Response
		TransactionResponseDTO response = new TransactionResponseDTO();

		response.setId(savedTransaction.getId());

		response.setTransactionReference(savedTransaction.getTransactionReference());

		response.setAmount(savedTransaction.getAmount());

		response.setType(savedTransaction.getType());

		response.setStatus(savedTransaction.getStatus());

		response.setCreatedAt(savedTransaction.getCreatedAt());

		response.setSenderAccountId(savedTransaction.getAccount().getId());

		if (savedTransaction.getReceiverAccount() != null) {
			response.setReceiverAccountId(savedTransaction.getReceiverAccount().getId());
		}

		return response;
	}

	// getTransactionByAccount API
	public List<TransactionHistoryResponseDTO> getTransactionsByAccount(Long accountId, String username) {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		if (!account.getUser().getId().equals(user.getId())) {
			throw new BadRequestException("You are not allowed to access this account");
		}

		List<Transaction> transactions = transactionRepository.findByAccount(account);

		return transactions.stream().map(transaction -> {

			TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO();

			response.setTransactionId(transaction.getId());

			response.setTransactionReference(transaction.getTransactionReference());

			response.setAmount(transaction.getAmount());

			response.setType(transaction.getType());

			response.setStatus(transaction.getStatus());

			response.setCreatedAt(transaction.getCreatedAt());

			return response;

		}).collect(Collectors.toList());
	}

	// getAllTransactions API
	public List<TransactionResponseDTO> getAllTransactions() {

		List<Transaction> transactions = transactionRepository.findAll();

		return transactions.stream().map(transaction -> {

			TransactionResponseDTO response = new TransactionResponseDTO();

			response.setId(transaction.getId());

			response.setTransactionReference(transaction.getTransactionReference());

			response.setAmount(transaction.getAmount());

			response.setType(transaction.getType());

			response.setStatus(transaction.getStatus());

			response.setCreatedAt(transaction.getCreatedAt());

			if (transaction.getAccount() != null) {
				response.setSenderAccountId(transaction.getAccount().getId());
			}

			if (transaction.getReceiverAccount() != null) {
				response.setReceiverAccountId(transaction.getReceiverAccount().getId());
			}

			return response;

		}).collect(Collectors.toList());
	}
}