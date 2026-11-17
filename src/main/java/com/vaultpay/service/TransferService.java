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
		String receiverAccountNumber = transferRequest.getReceiverAccountNumber();
		var amount = transferRequest.getAmount();

		if (senderId == null) {
			throw new BadRequestException("Sender account ID is required");
		}

		if (receiverAccountNumber == null || receiverAccountNumber.isBlank()) {
			throw new BadRequestException("Receiver account number is required");
		}

		if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
			throw new BadRequestException("Transfer amount must be greater than zero");
		}

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Find the receiver using the account number
		Account receiverAccount = accountRepository.findByAccountNumber(receiverAccountNumber.trim())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

		Long receiverId = receiverAccount.getId();

		if (senderId.equals(receiverId)) {
			throw new BadRequestException("Sender and receiver cannot be the same");
		}

		// Always lock accounts in ascending ID order
		Long firstLockId = Math.min(senderId, receiverId);
		Long secondLockId = Math.max(senderId, receiverId);

		Account firstAccount = accountRepository.findByIdForUpdate(firstLockId)
				.orElseThrow(() -> new ResourceNotFoundException("Sender or receiver account not found"));

		Account secondAccount = accountRepository.findByIdForUpdate(secondLockId)
				.orElseThrow(() -> new ResourceNotFoundException("Sender or receiver account not found"));

		// Identify accounts after locking
		Account sender = senderId.equals(firstLockId) ? firstAccount : secondAccount;

		Account receiver = receiverId.equals(firstLockId) ? firstAccount : secondAccount;

		// Verify sender ownership
		if (!sender.getUser().getId().equals(user.getId())) {
			throw new BadRequestException("You are not allowed to transfer from this account");
		}

		if (!"ACTIVE".equals(sender.getStatus())) {
			throw new BadRequestException("Sender account is not active");
		}

		if (!"ACTIVE".equals(receiver.getStatus())) {
			throw new BadRequestException("Receiver account is not active");
		}

		if (sender.getBalance().compareTo(amount) < 0) {
			throw new BadRequestException("Insufficient balance");
		}

		// Update account balances
		sender.setBalance(sender.getBalance().subtract(amount));
		receiver.setBalance(receiver.getBalance().add(amount));

		accountRepository.save(sender);
		accountRepository.save(receiver);

		String transactionReference = "TXN-" + UUID.randomUUID();
		LocalDateTime now = LocalDateTime.now();

		// Save transaction
		Transaction transaction = new Transaction();

		transaction.setAccount(sender);
		transaction.setReceiverAccount(receiver);
		transaction.setAmount(amount);
		transaction.setType("TRANSFER");
		transaction.setTransactionReference(transactionReference);
		transaction.setStatus("SUCCESS");
		transaction.setCreatedAt(now);

		Transaction savedTransaction = transactionRepository.save(transaction);

		// Debit ledger entry
		LedgerEntry debitEntry = new LedgerEntry();

		debitEntry.setAccount(sender);
		debitEntry.setEntryType("DEBIT");
		debitEntry.setAmount(amount);
		debitEntry.setTransactionReference(transactionReference);
		debitEntry.setCreatedAt(now);

		ledgerEntryRepository.save(debitEntry);

		// Credit ledger entry
		LedgerEntry creditEntry = new LedgerEntry();

		creditEntry.setAccount(receiver);
		creditEntry.setEntryType("CREDIT");
		creditEntry.setAmount(amount);
		creditEntry.setTransactionReference(transactionReference);
		creditEntry.setCreatedAt(now);

		ledgerEntryRepository.save(creditEntry);

		// Build response
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
