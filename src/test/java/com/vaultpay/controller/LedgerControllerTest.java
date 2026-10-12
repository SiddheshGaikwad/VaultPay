package com.vaultpay.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vaultpay.dto.LedgerResponseDTO;
import com.vaultpay.service.LedgerService;

import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class LedgerControllerTest {

	private MockMvc mockMvc;

	@Mock
	LedgerService ledgerService;

	@Mock
	Authentication authentication;

	@InjectMocks
	LedgerController ledgerController;

	@BeforeEach
	void setup() {

		mockMvc = MockMvcBuilders.standaloneSetup(ledgerController).build();
	}

	@Test
	void getLedgerByAccountShouldReturn200() throws Exception {

		LedgerResponseDTO response = new LedgerResponseDTO();

		response.setLedgerId(1L);
		response.setAccountId(8L);
		response.setEntryType("CREDIT");
		response.setAmount(new BigDecimal("500"));
		response.setTransactionReference("TXN-TEST");
		response.setCreatedAt(LocalDateTime.now());

		when(authentication.getName()).thenReturn("testuser");

		when(ledgerService.getLedgerByAccount(8L, "testuser")).thenReturn(List.of(response));

		mockMvc.perform(get("/api/ledger/account/8").principal(authentication)).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].ledgerId").value(1))
				.andExpect(jsonPath("$[0].accountId").value(8)).andExpect(jsonPath("$[0].entryType").value("CREDIT"))
				.andExpect(jsonPath("$[0].amount").value(500))
				.andExpect(jsonPath("$[0].transactionReference").value("TXN-TEST"));
	}

	@Test
	void getLedgerByAccountShouldReturnEmptyList() throws Exception {

		when(authentication.getName()).thenReturn("testuser");

		when(ledgerService.getLedgerByAccount(8L, "testuser")).thenReturn(List.of());

		mockMvc.perform(get("/api/ledger/account/8").principal(authentication)).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void getAllLedgerEntriesShouldReturn200() throws Exception {

		LedgerResponseDTO entry1 = new LedgerResponseDTO();

		entry1.setLedgerId(1L);
		entry1.setAccountId(8L);
		entry1.setEntryType("CREDIT");
		entry1.setAmount(new BigDecimal("500"));
		entry1.setTransactionReference("TXN-1");

		LedgerResponseDTO entry2 = new LedgerResponseDTO();

		entry2.setLedgerId(2L);
		entry2.setAccountId(8L);
		entry2.setEntryType("DEBIT");
		entry2.setAmount(new BigDecimal("200"));
		entry2.setTransactionReference("TXN-2");

		when(ledgerService.getAllLedgerEntries()).thenReturn(List.of(entry1, entry2));

		mockMvc.perform(get("/api/ledger")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].entryType").value("CREDIT"))
				.andExpect(jsonPath("$[1].entryType").value("DEBIT"));
	}

	@Test
	void getAllLedgerEntriesShouldReturnEmptyList() throws Exception {

		when(ledgerService.getAllLedgerEntries()).thenReturn(List.of());

		mockMvc.perform(get("/api/ledger")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}
}