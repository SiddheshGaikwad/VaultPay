package com.vaultpay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaultpay.dto.TransactionRequestDTO;
import com.vaultpay.dto.TransactionResponseDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.entity.Transaction;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.LedgerEntryRepository;
import com.vaultpay.repository.TransactionRepository;
import com.vaultpay.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	TransactionRepository transactionRepository;

	@Mock
	AccountRepository accountRepository;

	@Mock
	UserRepository userRepository;

	@Mock
	LedgerEntryRepository ledgerEntryRepository;

	@InjectMocks
	TransactionService transactionService;

	User user;
	Account account;

	@BeforeEach
	void setup() {

		user = new User();
		user.setId(4L);
		user.setUsername("testuser");
		user.setRole("CUSTOMER");

		account = new Account();
		account.setId(8L);
		account.setAccountNumber("ACC1004");
		account.setBalance(new BigDecimal("1000"));
		account.setStatus("ACTIVE");
		account.setUser(user);
	}

	@Test
	void withdrawalShouldFailWhenBalanceIsInsufficient() {

		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setAccountId(8L);
		request.setAmount(new BigDecimal("2000"));
		request.setType("WITHDRAWAL");

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(account));

		BadRequestException exception = org.junit.jupiter.api.Assertions.assertThrows(BadRequestException.class,
				() -> transactionService.createTransaction(request, "testuser"));

		assertEquals("Insufficient balance", exception.getMessage());
	}

	@Test
	void transactionShouldFailForUnauthorizedAccount() {

		User anotherUser = new User();
		anotherUser.setId(5L);
		anotherUser.setUsername("admin");
		anotherUser.setRole("ADMIN");

		account.setUser(anotherUser);

		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setAccountId(8L);
		request.setAmount(new BigDecimal("100"));
		request.setType("DEPOSIT");

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(account));

		BadRequestException exception = org.junit.jupiter.api.Assertions.assertThrows(BadRequestException.class,
				() -> transactionService.createTransaction(request, "testuser"));

		assertEquals("You are not allowed to access this account", exception.getMessage());
	}

	@Test
	void transactionShouldFailWhenAccountIsInactive() {

		account.setStatus("INACTIVE");

		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setAccountId(8L);
		request.setAmount(new BigDecimal("100"));
		request.setType("DEPOSIT");

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(account));

		BadRequestException exception = org.junit.jupiter.api.Assertions.assertThrows(BadRequestException.class,
				() -> transactionService.createTransaction(request, "testuser"));

		assertEquals("Account is not active", exception.getMessage());
	}

	@Test
	void depositShouldBeSuccessful() {

		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setAccountId(8L);
		request.setAmount(new BigDecimal("100"));
		request.setType("DEPOSIT");

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(account));

		Transaction transaction = new Transaction();
		transaction.setId(20L);
		transaction.setAccount(account);
		transaction.setAmount(new BigDecimal("100"));
		transaction.setType("DEPOSIT");
		transaction.setStatus("SUCCESS");
		transaction.setTransactionReference("TXN-TEST");

		when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

		TransactionResponseDTO response = transactionService.createTransaction(request, "testuser");

		assertNotNull(response);
		assertEquals(20L, response.getId());
		assertEquals("DEPOSIT", response.getType());
		assertEquals("SUCCESS", response.getStatus());
		assertEquals(new BigDecimal("1100"), account.getBalance());
	}
}
