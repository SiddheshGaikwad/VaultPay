package com.vaultpay.service;

import static org.junit.jupiter.api.Assertions.*;
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

import com.vaultpay.dto.TransferRequestDTO;
import com.vaultpay.dto.TransferResponseDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.entity.Transaction;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.LedgerEntryRepository;
import com.vaultpay.repository.TransactionRepository;
import com.vaultpay.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	TransactionRepository transactionRepository;

	@Mock
	LedgerEntryRepository ledgerEntryRepository;

	@Mock
	UserRepository userRepository;

	@InjectMocks
	TransferService transferService;

	User user;
	User receiverUser;
	Account sender;
	Account receiver;

	@BeforeEach
	void setup() {
		user = new User();
		user.setId(4L);
		user.setUsername("testuser");
		user.setRole("CUSTOMER");

		sender = new Account();
		sender.setId(8L);
		sender.setAccountNumber("ACC1004");
		sender.setBalance(new BigDecimal("1000"));
		sender.setStatus("ACTIVE");
		sender.setUser(user);

		receiverUser = new User();
		receiverUser.setId(3L);
		receiverUser.setUsername("sachin");
		receiverUser.setRole("CUSTOMER");

		receiver = new Account();
		receiver.setId(3L);
		receiver.setAccountNumber("ACC1003");
		receiver.setBalance(new BigDecimal("500"));
		receiver.setStatus("ACTIVE");
		receiver.setUser(receiverUser);
	}

	private TransferRequestDTO createRequest(String receiverNumber, BigDecimal amount) {
		TransferRequestDTO request = new TransferRequestDTO();
		request.setSenderAccountId(8L);
		request.setReceiverAccountNumber(receiverNumber);
		request.setAmount(amount);
		return request;
	}

	private void mockAccountLookups() {
		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findByAccountNumber("ACC1003")).thenReturn(Optional.of(receiver));

		when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(receiver));

		when(accountRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(sender));
	}

	@Test
	void transferShouldFailWhenBalanceIsInsufficient() {
		TransferRequestDTO request = createRequest("ACC1003", new BigDecimal("2000"));

		mockAccountLookups();

		BadRequestException exception = assertThrows(BadRequestException.class,
				() -> transferService.transfer(request, "testuser"));

		assertEquals("Insufficient balance", exception.getMessage());
	}

	@Test
	void transferShouldFailWhenSenderIsInactive() {
		sender.setStatus("INACTIVE");

		TransferRequestDTO request = createRequest("ACC1003", new BigDecimal("100"));

		mockAccountLookups();

		BadRequestException exception = assertThrows(BadRequestException.class,
				() -> transferService.transfer(request, "testuser"));

		assertEquals("Sender account is not active", exception.getMessage());
	}

	@Test
	void transferShouldFailWhenReceiverIsInactive() {
		receiver.setStatus("INACTIVE");

		TransferRequestDTO request = createRequest("ACC1003", new BigDecimal("100"));

		mockAccountLookups();

		BadRequestException exception = assertThrows(BadRequestException.class,
				() -> transferService.transfer(request, "testuser"));

		assertEquals("Receiver account is not active", exception.getMessage());
	}

	@Test
	void transferShouldFailForUnauthorizedSender() {
		User anotherUser = new User();
		anotherUser.setId(10L);
		anotherUser.setUsername("anotheruser");
		anotherUser.setRole("CUSTOMER");

		sender.setUser(anotherUser);

		TransferRequestDTO request = createRequest("ACC1003", new BigDecimal("100"));

		mockAccountLookups();

		BadRequestException exception = assertThrows(BadRequestException.class,
				() -> transferService.transfer(request, "testuser"));

		assertEquals("You are not allowed to transfer from this account", exception.getMessage());
	}

	@Test
	void transferShouldFailWhenSenderAndReceiverAreSame() {
		TransferRequestDTO request = new TransferRequestDTO();
		request.setSenderAccountId(8L);
		request.setReceiverAccountNumber("ACC1004");
		request.setAmount(new BigDecimal("100"));

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(accountRepository.findByAccountNumber("ACC1004")).thenReturn(Optional.of(sender));

		assertThrows(BadRequestException.class, () -> transferService.transfer(request, "testuser"));
	}

	@Test
	void transferShouldBeSuccessful() {
		TransferRequestDTO request = createRequest("ACC1003", new BigDecimal("200"));

		mockAccountLookups();

		Transaction transaction = new Transaction();
		transaction.setId(21L);
		transaction.setAccount(sender);
		transaction.setReceiverAccount(receiver);
		transaction.setAmount(new BigDecimal("200"));
		transaction.setType("TRANSFER");
		transaction.setStatus("SUCCESS");
		transaction.setTransactionReference("TXN-TEST");

		when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

		TransferResponseDTO response = transferService.transfer(request, "testuser");

		assertNotNull(response);
		assertEquals(21L, response.getTransactionId());
		assertEquals(8L, response.getSenderAccountId());
		assertEquals(3L, response.getReceiverAccountId());
		assertEquals(new BigDecimal("200"), response.getAmount());
		assertEquals("SUCCESS", response.getStatus());
		assertEquals(new BigDecimal("800"), sender.getBalance());
		assertEquals(new BigDecimal("700"), receiver.getBalance());
	}
}
