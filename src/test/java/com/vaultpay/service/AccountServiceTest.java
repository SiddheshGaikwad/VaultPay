package com.vaultpay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaultpay.dto.AccountResponseDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.exception.ResourceNotFoundException;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	AccountService accountService;

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

	// 1. Create account successfully

	@Test
	void createAccountShouldBeSuccessful() {

		when(userRepository.findById(4L)).thenReturn(Optional.of(user));

		when(accountRepository.existsByUser(user)).thenReturn(false);

		when(accountRepository.save(any(Account.class))).thenReturn(account);

		AccountResponseDTO response = accountService.createAccount(4L);

		assertNotNull(response);

		assertEquals(8L, response.getId());

		assertEquals("ACC1004", response.getAccountNumber());

		assertEquals(new BigDecimal("1000"), response.getBalance());

		assertEquals("ACTIVE", response.getStatus());

		assertEquals(4L, response.getUserId());
	}

	// 2. User not found

	@Test
	void createAccountShouldFailWhenUserNotFound() {

		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> accountService.createAccount(99L));

		assertEquals("User not found", exception.getMessage());
	}

	// 3. User already has account

	@Test
	void createAccountShouldFailWhenUserAlreadyHasAccount() {

		when(userRepository.findById(4L)).thenReturn(Optional.of(user));

		when(accountRepository.existsByUser(user)).thenReturn(true);

		BadRequestException exception = assertThrows(BadRequestException.class, () -> accountService.createAccount(4L));

		assertEquals("User already has an account", exception.getMessage());
	}

	// 4. Get all accounts successfully

	@Test
	void getAllAccountsShouldBeSuccessful() {

		User user2 = new User();
		user2.setId(3L);
		user2.setUsername("sachin");
		user2.setRole("CUSTOMER");

		Account account2 = new Account();
		account2.setId(3L);
		account2.setAccountNumber("ACC1003");
		account2.setBalance(new BigDecimal("500"));
		account2.setStatus("ACTIVE");
		account2.setUser(user2);

		when(accountRepository.findAll()).thenReturn(List.of(account, account2));

		List<AccountResponseDTO> response = accountService.getAllAccounts();

		assertNotNull(response);

		assertEquals(2, response.size());

		assertEquals(8L, response.get(0).getId());

		assertEquals("ACC1004", response.get(0).getAccountNumber());

		assertEquals(3L, response.get(1).getId());

		assertEquals("ACC1003", response.get(1).getAccountNumber());
	}

	// 5. Empty account list

	@Test
	void getAllAccountsShouldReturnEmptyList() {

		when(accountRepository.findAll()).thenReturn(List.of());

		List<AccountResponseDTO> response = accountService.getAllAccounts();

		assertNotNull(response);

		assertEquals(0, response.size());
	}
}