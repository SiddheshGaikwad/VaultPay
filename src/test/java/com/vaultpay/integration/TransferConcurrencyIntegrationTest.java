package com.vaultpay.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaultpay.dto.TransferRequestDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.service.TransferService;

@SpringBootTest
class TransferConcurrencyIntegrationTest {

	@Autowired
	private TransferService transferService;

	@Autowired
	private AccountRepository accountRepository;

	@Test
	void shouldHandleConcurrentTransfers() throws Exception {

		List<Account> accounts = accountRepository.findAll();

		Account sender = accounts.stream().filter(a -> a.getUser() != null).filter(a -> "ACTIVE".equals(a.getStatus()))
				.filter(a -> a.getBalance() != null)
				.filter(a -> a.getBalance().compareTo(new BigDecimal("200.00")) >= 0).findFirst()
				.orElseThrow(() -> new AssertionError("No suitable sender account found"));

		Account receiver = accounts.stream().filter(a -> a.getUser() != null)
				.filter(a -> "ACTIVE".equals(a.getStatus()))
				.filter(a -> !a.getUser().getId().equals(sender.getUser().getId())).findFirst()
				.orElseThrow(() -> new AssertionError("No receiver account found"));

		CountDownLatch startSignal = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<?> transfer1 = executor.submit(() -> {
				awaitStart(startSignal);

				TransferRequestDTO request = new TransferRequestDTO();
				request.setSenderAccountId(sender.getId());
				request.setReceiverAccountNumber(receiver.getAccountNumber());
				request.setAmount(new BigDecimal("100.00"));

				transferService.transfer(request, sender.getUser().getUsername());
			});

			Future<?> transfer2 = executor.submit(() -> {
				awaitStart(startSignal);

				TransferRequestDTO request = new TransferRequestDTO();
				request.setSenderAccountId(sender.getId());
				request.setReceiverAccountNumber(receiver.getAccountNumber());
				request.setAmount(new BigDecimal("100.00"));

				transferService.transfer(request, sender.getUser().getUsername());
			});

			startSignal.countDown();

			transfer1.get();
			transfer2.get();

			Account updatedSender = accountRepository.findById(sender.getId()).orElseThrow();

			assertTrue(updatedSender.getBalance().compareTo(BigDecimal.ZERO) >= 0,
					"Sender balance must not become negative");

		} finally {
			executor.shutdownNow();
		}
	}

	private void awaitStart(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
}