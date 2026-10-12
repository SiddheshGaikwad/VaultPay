package com.vaultpay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaultpay.dto.LedgerResponseDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.entity.LedgerEntry;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.exception.ResourceNotFoundException;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.LedgerEntryRepository;
import com.vaultpay.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

	@Mock
	LedgerEntryRepository ledgerEntryRepository;

	@Mock
	AccountRepository accountRepository;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	LedgerService ledgerService;

	User user;
	User anotherUser;
	Account account;
	LedgerEntry ledgerEntry;

	@BeforeEach
	void setup() {

		user = new User();
		user.setId(4L);
		user.setUsername("testuser");
		user.setRole("CUSTOMER");

		anotherUser = new User();
		anotherUser.setId(10L);
		anotherUser.setUsername("anotheruser");
		anotherUser.setRole("CUSTOMER");

		account = new Account();
		account.setId(8L);
		account.setAccountNumber("ACC1004");
		account.setBalance(new BigDecimal("1000"));
		account.setStatus("ACTIVE");
		account.setUser(user);

		ledgerEntry = new LedgerEntry();
		ledgerEntry.setId(1L);
		ledgerEntry.setAccount(account);
		ledgerEntry.setEntryType("CREDIT");
		ledgerEntry.setAmount(new BigDecimal("500"));
		ledgerEntry.setTransactionReference("TXN-TEST");
		ledgerEntry.setCreatedAt(LocalDateTime.now());
	}

	// 1. Get ledger successfully

	@Test
	void getLedgerByAccountShouldBeSuccessful() {

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findById(8L)).thenReturn(Optional.of(account));

		when(ledgerEntryRepository.findByAccount(account)).thenReturn(List.of(ledgerEntry));

		List<LedgerResponseDTO> response = ledgerService.getLedgerByAccount(8L, "testuser");

		assertNotNull(response);

		assertEquals(1, response.size());

		LedgerResponseDTO result = response.get(0);

		assertEquals(1L, result.getLedgerId());

		assertEquals(8L, result.getAccountId());

		assertEquals("CREDIT", result.getEntryType());

		assertEquals(new BigDecimal("500"), result.getAmount());

		assertEquals("TXN-TEST", result.getTransactionReference());

		assertNotNull(result.getCreatedAt());
	}

	// 2. Account not found

	@Test
	void getLedgerByAccountShouldFailWhenAccountNotFound() {

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findById(99L)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> ledgerService.getLedgerByAccount(99L, "testuser"));

		assertEquals("Account not found", exception.getMessage());
	}

	// 3. User not found

	@Test
	void getLedgerByAccountShouldFailWhenUserNotFound() {

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> ledgerService.getLedgerByAccount(8L, "testuser"));

		assertEquals("User not found", exception.getMessage());
	}

	// 4. Unauthorized account

	@Test
	void getLedgerByAccountShouldFailForUnauthorizedUser() {

		account.setUser(anotherUser);

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findById(8L)).thenReturn(Optional.of(account));

		BadRequestException exception = assertThrows(BadRequestException.class,
				() -> ledgerService.getLedgerByAccount(8L, "testuser"));

		assertEquals("You are not allowed to access this account", exception.getMessage());
	}

	// 5. Get all ledger entries successfully

	@Test
	void getAllLedgerEntriesShouldBeSuccessful() {

		LedgerEntry secondEntry = new LedgerEntry();

		secondEntry.setId(2L);
		secondEntry.setAccount(account);
		secondEntry.setEntryType("DEBIT");
		secondEntry.setAmount(new BigDecimal("200"));
		secondEntry.setTransactionReference("TXN-TEST-2");
		secondEntry.setCreatedAt(LocalDateTime.now());

		when(ledgerEntryRepository.findAll()).thenReturn(List.of(ledgerEntry, secondEntry));

		List<LedgerResponseDTO> response = ledgerService.getAllLedgerEntries();

		assertNotNull(response);

		assertEquals(2, response.size());

		assertEquals(1L, response.get(0).getLedgerId());

		assertEquals("CREDIT", response.get(0).getEntryType());

		assertEquals(new BigDecimal("500"), response.get(0).getAmount());

		assertEquals(2L, response.get(1).getLedgerId());

		assertEquals("DEBIT", response.get(1).getEntryType());

		assertEquals(new BigDecimal("200"), response.get(1).getAmount());
	}

	// 6. Empty ledger

	@Test
	void getAllLedgerEntriesShouldReturnEmptyList() {

		when(ledgerEntryRepository.findAll()).thenReturn(List.of());

		List<LedgerResponseDTO> response = ledgerService.getAllLedgerEntries();

		assertNotNull(response);

		assertEquals(0, response.size());
	}
}