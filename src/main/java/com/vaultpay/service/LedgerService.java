package com.vaultpay.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaultpay.dto.LedgerResponseDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.entity.LedgerEntry;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.exception.ResourceNotFoundException;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.LedgerEntryRepository;
import com.vaultpay.repository.UserRepository;

@Service
public class LedgerService {

	@Autowired
	LedgerEntryRepository ledgerEntryRepository;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	UserRepository userRepository;

	public List<LedgerResponseDTO> getLedgerByAccount(Long accountId, String username) {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		if (!account.getUser().getId().equals(user.getId())) {
			throw new BadRequestException("You are not allowed to access this account");
		}

		List<LedgerEntry> entries = ledgerEntryRepository.findByAccount(account);

		return entries.stream().map(entry -> {

			LedgerResponseDTO response = new LedgerResponseDTO();

			response.setLedgerId(entry.getId());
			response.setAccountId(entry.getAccount().getId());
			response.setEntryType(entry.getEntryType());
			response.setAmount(entry.getAmount());
			response.setTransactionReference(entry.getTransactionReference());
			response.setCreatedAt(entry.getCreatedAt());

			return response;

		}).collect(Collectors.toList());
	}

	public List<LedgerResponseDTO> getAllLedgerEntries() {

		List<LedgerEntry> entries = ledgerEntryRepository.findAll();

		return entries.stream().map(entry -> {

			LedgerResponseDTO response = new LedgerResponseDTO();

			response.setLedgerId(entry.getId());

			response.setAccountId(entry.getAccount().getId());

			response.setEntryType(entry.getEntryType());

			response.setAmount(entry.getAmount());

			response.setTransactionReference(entry.getTransactionReference());

			response.setCreatedAt(entry.getCreatedAt());

			return response;
		}).collect(Collectors.toList());
	}
}