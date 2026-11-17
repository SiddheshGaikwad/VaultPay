package com.vaultpay.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

import com.vaultpay.dto.TransferResponseDTO;
import com.vaultpay.service.TransferService;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

	private MockMvc mockMvc;

	@Mock
	TransferService transferService;

	@Mock
	Authentication authentication;

	@InjectMocks
	TransferController transferController;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(transferController).build();
	}

	@Test
	void transferShouldReturn200() throws Exception {
		TransferResponseDTO response = new TransferResponseDTO();

		response.setTransactionId(30L);
		response.setSenderAccountId(8L);
		response.setReceiverAccountId(3L);
		response.setAmount(new BigDecimal("200"));
		response.setStatus("SUCCESS");
		response.setTransactionReference("TXN-TEST");
		response.setCreatedAt(LocalDateTime.now());

		when(authentication.getName()).thenReturn("testuser");
		when(transferService.transfer(any(), any())).thenReturn(response);

		String requestBody = """
				{
				    "senderAccountId": 8,
				    "receiverAccountNumber": "ACC1003",
				    "amount": 200
				}
				""";

		mockMvc.perform(post("/api/transfers").principal(authentication).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isOk()).andExpect(jsonPath("$.transactionId").value(30))
				.andExpect(jsonPath("$.senderAccountId").value(8)).andExpect(jsonPath("$.receiverAccountId").value(3))
				.andExpect(jsonPath("$.amount").value(200)).andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.transactionReference").value("TXN-TEST"));
	}

	@Test
	void transferShouldFailWithInvalidData() throws Exception {
		String requestBody = """
				{
				    "senderAccountId": null,
				    "receiverAccountNumber": null,
				    "amount": -100
				}
				""";

		mockMvc.perform(post("/api/transfers").principal(authentication).contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)).andExpect(status().isBadRequest());
	}

	@Test
    void transferShouldFailWhenAmountIsMissing() throws Exception {
        String requestBody = """
                {
                    "senderAccountId": 8,
                    "receiverAccountNumber": "ACC1003"
                }
                """;

        mockMvc.perform(post("/api/transfers")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}