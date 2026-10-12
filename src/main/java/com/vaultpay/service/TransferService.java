package com.vaultpay.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultpay.dto.TransferRequestDTO;
import com.vaultpay.dto.TransferResponseDTO;
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

@Service
public class TransferService {

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	LedgerEntryRepository ledgerEntryRepository;

	@Autowired
	UserRepository userRepository;

	@Transactional
	public TransferResponseDTO transfer(TransferRequestDTO transferRequest, String username) {

		Long senderId = transferRequest.getSenderAccountId();
		Long receiverId = transferRequest.getReceiverAccountId();

		if (senderId.equals(receiverId)) {
			throw new BadRequestException("Sender and receiver cannot be same");
		}
		if (transferRequest.getAmount() == null
		        || transferRequest.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
		    throw new BadRequestException("Transfer amount must be greater than zero");
		}
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Always lock accounts in ascending ID order
		Long firstLockId = Math.min(senderId, receiverId);
		Long secondLockId = Math.max(senderId, receiverId);

		Account firstAccount = accountRepository.findByIdForUpdate(firstLockId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		Account secondAccount = accountRepository.findByIdForUpdate(secondLockId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		// Identify sender and receiver after locking
		Account sender = senderId.equals(firstLockId) ? firstAccount : secondAccount;

		Account receiver = receiverId.equals(firstLockId) ? firstAccount : secondAccount;

		if (!sender.getUser().getId().equals(user.getId())) {
			throw new BadRequestException("You are not allowed to transfer from this account");
		}

		if (!sender.getStatus().equals("ACTIVE")) {
			throw new BadRequestException("Sender account is not active");
		}

		if (!receiver.getStatus().equals("ACTIVE")) {
			throw new BadRequestException("Receiver account is not active");
		}

		if (sender.getBalance().compareTo(transferRequest.getAmount()) < 0) {

			throw new BadRequestException("Insufficient balance");
		}

		sender.setBalance(sender.getBalance().subtract(transferRequest.getAmount()));

		receiver.setBalance(receiver.getBalance().add(transferRequest.getAmount()));

		accountRepository.save(sender);
		accountRepository.save(receiver);

		String transactionReference = "TXN-" + UUID.randomUUID();

		Transaction transaction = new Transaction();

		transaction.setAccount(sender);
		transaction.setReceiverAccount(receiver);
		transaction.setAmount(transferRequest.getAmount());
		transaction.setType("TRANSFER");
		transaction.setTransactionReference(transactionReference);
		transaction.setStatus("SUCCESS");
		transaction.setCreatedAt(LocalDateTime.now());

		Transaction savedTransaction = transactionRepository.save(transaction);

		// Debit ledger
		LedgerEntry debitEntry = new LedgerEntry();

		debitEntry.setAccount(sender);
		debitEntry.setEntryType("DEBIT");
		debitEntry.setAmount(transferRequest.getAmount());
		debitEntry.setTransactionReference(transactionReference);
		debitEntry.setCreatedAt(LocalDateTime.now());

		ledgerEntryRepository.save(debitEntry);

		// Credit ledger
		LedgerEntry creditEntry = new LedgerEntry();

		creditEntry.setAccount(receiver);
		creditEntry.setEntryType("CREDIT");
		creditEntry.setAmount(transferRequest.getAmount());
		creditEntry.setTransactionReference(transactionReference);
		creditEntry.setCreatedAt(LocalDateTime.now());

		ledgerEntryRepository.save(creditEntry);

		TransferResponseDTO response = new TransferResponseDTO();

		response.setTransactionId(savedTransaction.getId());
		response.setSenderAccountId(sender.getId());
		response.setReceiverAccountId(receiver.getId());
		response.setAmount(savedTransaction.getAmount());
		response.setStatus(savedTransaction.getStatus());
		response.setCreatedAt(savedTransaction.getCreatedAt());
		response.setTransactionReference(savedTransaction.getTransactionReference());

		return response;
	}
}