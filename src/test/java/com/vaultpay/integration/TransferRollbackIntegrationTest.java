package com.vaultpay.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaultpay.entity.Account;
import com.vaultpay.entity.Transaction;
import com.vaultpay.entity.LedgerEntry;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.TransactionRepository;
import com.vaultpay.repository.LedgerEntryRepository;

@SpringBootTest
class TransferRollbackIntegrationTest {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private LedgerEntryRepository ledgerEntryRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void shouldRollbackAllDatabaseChanges() {

		TransactionTemplate template = new TransactionTemplate(transactionManager);

		Account account = accountRepository.findAll().stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("Test account not found"));

		Long accountId = account.getId();
		BigDecimal originalBalance = account.getBalance();

		long originalTransactions = transactionRepository.count();
		long originalLedgerEntries = ledgerEntryRepository.count();

		assertThrows(RuntimeException.class, () -> template.executeWithoutResult(status -> {

			Account managedAccount = accountRepository.findByIdForUpdate(accountId).orElseThrow();

			managedAccount.setBalance(new BigDecimal("123.45"));
			accountRepository.saveAndFlush(managedAccount);

			Transaction transaction = new Transaction();
			transaction.setAccount(managedAccount);
			transaction.setTransactionReference("ROLLBACK-TEST-" + System.nanoTime());
			transaction.setAmount(new BigDecimal("10.00"));
			transaction.setType("DEBIT");
			transaction.setStatus("SUCCESS");
			transaction.setCreatedAt(LocalDateTime.now());

			transactionRepository.saveAndFlush(transaction);

			LedgerEntry entry = new LedgerEntry();
			entry.setAccount(managedAccount);
			entry.setEntryType("DEBIT");
			entry.setAmount(new BigDecimal("10.00"));
			entry.setTransactionReference(transaction.getTransactionReference());
			entry.setCreatedAt(LocalDateTime.now());

			ledgerEntryRepository.saveAndFlush(entry);

			throw new RuntimeException("Intentional rollback");
		}));

		Account reloadedAccount = accountRepository.findById(accountId).orElseThrow();

		assertEquals(0, originalBalance.compareTo(reloadedAccount.getBalance()));

		assertEquals(originalTransactions, transactionRepository.count());
		assertEquals(originalLedgerEntries, ledgerEntryRepository.count());
	}
}
