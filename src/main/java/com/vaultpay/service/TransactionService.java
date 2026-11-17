package com.vaultpay.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
    public TransactionResponseDTO createTransaction(
            TransactionRequestDTO transactionRequest, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Account account = accountRepository
                .findByIdForUpdate(transactionRequest.getAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(
                    "You are not allowed to access this account");
        }

        if (!account.getStatus().equals("ACTIVE")) {
            throw new BadRequestException("Account is not active");
        }

        if (transactionRequest.getAmount().signum() <= 0) {
            throw new BadRequestException(
                    "Amount must be greater than zero");
        }

        String type = transactionRequest.getType();

        if (!type.equals("DEPOSIT") && !type.equals("WITHDRAWAL")) {
            throw new BadRequestException("Invalid transaction type");
        }

        if (type.equals("DEPOSIT")) {
            account.setBalance(
                    account.getBalance().add(transactionRequest.getAmount()));
        } else {
            if (account.getBalance()
                    .compareTo(transactionRequest.getAmount()) < 0) {
                throw new BadRequestException("Insufficient balance");
            }

            account.setBalance(
                    account.getBalance().subtract(transactionRequest.getAmount()));
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setType(type);
        transaction.setTransactionReference("TXN-" + UUID.randomUUID());
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setAccount(account);
        ledgerEntry.setAmount(transactionRequest.getAmount());
        ledgerEntry.setTransactionReference(
                savedTransaction.getTransactionReference());
        ledgerEntry.setCreatedAt(LocalDateTime.now());
        ledgerEntry.setEntryType(
                type.equals("DEPOSIT") ? "CREDIT" : "DEBIT");

        ledgerEntryRepository.save(ledgerEntry);

        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setId(savedTransaction.getId());
        response.setTransactionReference(
                savedTransaction.getTransactionReference());
        response.setAmount(savedTransaction.getAmount());
        response.setType(savedTransaction.getType());
        response.setStatus(savedTransaction.getStatus());
        response.setCreatedAt(savedTransaction.getCreatedAt());
        response.setSenderAccountId(savedTransaction.getAccount().getId());

        if (savedTransaction.getReceiverAccount() != null) {
            response.setReceiverAccountId(
                    savedTransaction.getReceiverAccount().getId());
        }

        return response;
    }

    // Get transaction history for an account
    public List<TransactionHistoryResponseDTO> getTransactionsByAccount(
            Long accountId, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(
                    "You are not allowed to access this account");
        }

        List<Transaction> transactions = transactionRepository
                .findByAccountOrReceiverAccountOrderByCreatedAtDesc(
                        account, account);

        return transactions.stream().map(transaction -> {

            TransactionHistoryResponseDTO response =
                    new TransactionHistoryResponseDTO();

            response.setTransactionId(transaction.getId());
            response.setTransactionReference(
                    transaction.getTransactionReference());
            response.setAmount(transaction.getAmount());

            // Incoming transfer appears as DEPOSIT for the receiver.
            // Sender continues to see TRANSFER.
            if ("TRANSFER".equals(transaction.getType())
                    && transaction.getReceiverAccount() != null
                    && transaction.getReceiverAccount().getId()
                            .equals(account.getId())) {

                response.setType("DEPOSIT");

            } else {
                response.setType(transaction.getType());
            }

            if (transaction.getReceiverAccount() != null) {
                response.setReceiverAccountId(
                        transaction.getReceiverAccount().getId());
            }

            response.setStatus(transaction.getStatus());
            response.setCreatedAt(transaction.getCreatedAt());

            return response;

        }).collect(Collectors.toList());
    }

    // Get all transactions (admin)
    public List<TransactionResponseDTO> getAllTransactions() {

        List<Transaction> transactions = transactionRepository.findAll();

        return transactions.stream().map(transaction -> {

            TransactionResponseDTO response = new TransactionResponseDTO();

            response.setId(transaction.getId());
            response.setTransactionReference(
                    transaction.getTransactionReference());
            response.setAmount(transaction.getAmount());
            response.setType(transaction.getType());
            response.setStatus(transaction.getStatus());
            response.setCreatedAt(transaction.getCreatedAt());

            if (transaction.getAccount() != null) {
                response.setSenderAccountId(
                        transaction.getAccount().getId());
            }

            if (transaction.getReceiverAccount() != null) {
                response.setReceiverAccountId(
                        transaction.getReceiverAccount().getId());
            }

            return response;

        }).collect(Collectors.toList());
    }
}