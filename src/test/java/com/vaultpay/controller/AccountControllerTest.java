package com.vaultpay.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vaultpay.dto.AccountResponseDTO;
import com.vaultpay.service.AccountService;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

	private MockMvc mockMvc;

	@Mock
	AccountService accountService;

	@InjectMocks
	AccountController accountController;

	@BeforeEach
	void setup() {

		mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
	}

	// 1. Create account successfully

	@Test
	void createAccountShouldReturn200() throws Exception {

		AccountResponseDTO response = new AccountResponseDTO();

		response.setId(8L);
		response.setAccountNumber("ACC1004");
		response.setBalance(new BigDecimal("0"));
		response.setStatus("ACTIVE");
		response.setUserId(4L);

		when(accountService.createAccount(4L)).thenReturn(response);

		mockMvc.perform(post("/api/accounts/4")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(8))
				.andExpect(jsonPath("$.accountNumber").value("ACC1004")).andExpect(jsonPath("$.balance").value(0))
				.andExpect(jsonPath("$.status").value("ACTIVE")).andExpect(jsonPath("$.userId").value(4));
	}

	// 2. Get all accounts successfully

	@Test
	void getAllAccountsShouldReturn200() throws Exception {

		AccountResponseDTO account1 = new AccountResponseDTO();

		account1.setId(8L);
		account1.setAccountNumber("ACC1004");
		account1.setBalance(new BigDecimal("1000"));
		account1.setStatus("ACTIVE");
		account1.setUserId(4L);

		AccountResponseDTO account2 = new AccountResponseDTO();

		account2.setId(3L);
		account2.setAccountNumber("ACC1003");
		account2.setBalance(new BigDecimal("500"));
		account2.setStatus("ACTIVE");
		account2.setUserId(3L);

		when(accountService.getAllAccounts()).thenReturn(List.of(account1, account2));

		mockMvc.perform(get("/api/accounts")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(8)).andExpect(jsonPath("$[0].accountNumber").value("ACC1004"))
				.andExpect(jsonPath("$[0].userId").value(4)).andExpect(jsonPath("$[1].id").value(3))
				.andExpect(jsonPath("$[1].accountNumber").value("ACC1003")).andExpect(jsonPath("$[1].userId").value(3));
	}

	// 3. Empty account list

	@Test
	void getAllAccountsShouldReturnEmptyList() throws Exception {

		when(accountService.getAllAccounts()).thenReturn(List.of());

		mockMvc.perform(get("/api/accounts")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	// 4. Create account for another user ID

	@Test
	void createAccountShouldCallServiceWithCorrectUserId() throws Exception {

		AccountResponseDTO response = new AccountResponseDTO();

		response.setId(10L);
		response.setAccountNumber("ACC1005");
		response.setBalance(BigDecimal.ZERO);
		response.setStatus("ACTIVE");
		response.setUserId(5L);

		when(accountService.createAccount(5L)).thenReturn(response);

		mockMvc.perform(post("/api/accounts/5")).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(5))
				.andExpect(jsonPath("$.accountNumber").value("ACC1005"));
	}
}