package com.vaultpay.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vaultpay.dto.TransactionHistoryResponseDTO;
import com.vaultpay.dto.TransactionResponseDTO;
import com.vaultpay.service.TransactionService;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

	private MockMvc mockMvc;

	@Mock
	TransactionService transactionService;

	@Mock
	Authentication authentication;

	@InjectMocks
	TransactionController transactionController;

	@BeforeEach
	void setup() {

		mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
	}

	// 1. Deposit successfully

	@Test
	void createTransactionShouldReturn200() throws Exception {

		TransactionResponseDTO response = new TransactionResponseDTO();

		response.setId(20L);
		response.setTransactionReference("TXN-TEST");
		response.setAmount(new BigDecimal("500"));
		response.setType("DEPOSIT");
		response.setStatus("SUCCESS");
		response.setCreatedAt(LocalDateTime.now());
		response.setSenderAccountId(8L);

		when(authentication.getName()).thenReturn("testuser");

		when(transactionService.createTransaction(any(), any())).thenReturn(response);

		String requestBody = """
				{
				    "accountId": 8,
				    "amount": 500,
				    "type": "DEPOSIT"
				}
				""";

		mockMvc.perform(post("/api/transactions").principal(authentication).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(20))
				.andExpect(jsonPath("$.amount").value(500)).andExpect(jsonPath("$.type").value("DEPOSIT"))
				.andExpect(jsonPath("$.status").value("SUCCESS")).andExpect(jsonPath("$.senderAccountId").value(8))
				.andExpect(jsonPath("$.transactionReference").value("TXN-TEST"));
	}

	// 2. Get transactions by account

	@Test
	void getTransactionsByAccountShouldReturn200() throws Exception {

		TransactionHistoryResponseDTO transaction = new TransactionHistoryResponseDTO();

		transaction.setTransactionId(20L);
		transaction.setTransactionReference("TXN-TEST");
		transaction.setAmount(new BigDecimal("500"));
		transaction.setType("DEPOSIT");
		transaction.setStatus("SUCCESS");
		transaction.setCreatedAt(LocalDateTime.now());

		when(authentication.getName()).thenReturn("testuser");

		when(transactionService.getTransactionsByAccount(8L, "testuser")).thenReturn(List.of(transaction));

		mockMvc.perform(get("/api/transactions/account/8").principal(authentication)).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].transactionId").value(20))
				.andExpect(jsonPath("$[0].amount").value(500)).andExpect(jsonPath("$[0].type").value("DEPOSIT"))
				.andExpect(jsonPath("$[0].status").value("SUCCESS"))
				.andExpect(jsonPath("$[0].transactionReference").value("TXN-TEST"));
	}

	// 3. Empty transaction history

	@Test
	void getTransactionsByAccountShouldReturnEmptyList() throws Exception {

		when(authentication.getName()).thenReturn("testuser");

		when(transactionService.getTransactionsByAccount(8L, "testuser")).thenReturn(List.of());

		mockMvc.perform(get("/api/transactions/account/8").principal(authentication)).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	// 4. Get all transactions

	@Test
	void getAllTransactionsShouldReturn200() throws Exception {

		TransactionResponseDTO transaction1 = new TransactionResponseDTO();

		transaction1.setId(20L);
		transaction1.setTransactionReference("TXN-TEST-1");
		transaction1.setAmount(new BigDecimal("500"));
		transaction1.setType("DEPOSIT");
		transaction1.setStatus("SUCCESS");
		transaction1.setSenderAccountId(8L);

		TransactionResponseDTO transaction2 = new TransactionResponseDTO();

		transaction2.setId(21L);
		transaction2.setTransactionReference("TXN-TEST-2");
		transaction2.setAmount(new BigDecimal("200"));
		transaction2.setType("WITHDRAWAL");
		transaction2.setStatus("SUCCESS");
		transaction2.setSenderAccountId(8L);

		when(transactionService.getAllTransactions()).thenReturn(List.of(transaction1, transaction2));

		mockMvc.perform(get("/api/transactions")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(20)).andExpect(jsonPath("$[0].type").value("DEPOSIT"))
				.andExpect(jsonPath("$[1].id").value(21)).andExpect(jsonPath("$[1].type").value("WITHDRAWAL"));
	}

	// 5. Withdrawal successfully

	@Test
	void withdrawalShouldReturn200() throws Exception {

		TransactionResponseDTO response = new TransactionResponseDTO();

		response.setId(22L);
		response.setTransactionReference("TXN-WITHDRAW");
		response.setAmount(new BigDecimal("200"));
		response.setType("WITHDRAWAL");
		response.setStatus("SUCCESS");
		response.setSenderAccountId(8L);

		when(authentication.getName()).thenReturn("testuser");

		when(transactionService.createTransaction(any(), any())).thenReturn(response);

		String requestBody = """
				{
				    "accountId": 8,
				    "amount": 200,
				    "type": "WITHDRAWAL"
				}
				""";

		mockMvc.perform(post("/api/transactions").principal(authentication).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(22))
				.andExpect(jsonPath("$.amount").value(200)).andExpect(jsonPath("$.type").value("WITHDRAWAL"))
				.andExpect(jsonPath("$.status").value("SUCCESS"));
	}

	// 6. Invalid request validation

	@Test
	void createTransactionShouldFailWithInvalidData() throws Exception {

		String requestBody = """
				{
				    "accountId": null,
				    "amount": -100,
				    "type": ""
				}
				""";

		mockMvc.perform(post("/api/transactions").principal(authentication).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isBadRequest());
	}
}