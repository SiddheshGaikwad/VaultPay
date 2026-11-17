package com.vaultpay.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaultpay.dto.AccountResponseDTO;
import com.vaultpay.dto.ReceiverAccountDTO;
import com.vaultpay.entity.Account;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.exception.ResourceNotFoundException;
import com.vaultpay.repository.AccountRepository;
import com.vaultpay.repository.UserRepository;

@Service
public class AccountService {

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	UserRepository userRepository;

	public AccountResponseDTO createAccount(Long userId) {

		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (accountRepository.existsByUser(user)) {

			throw new BadRequestException("User already has an account");
		}

		Account account = new Account();

		account.setUser(user);
		account.setAccountNumber("ACC" + (1000 + userId));
		account.setBalance(java.math.BigDecimal.ZERO);
		account.setStatus("ACTIVE");

		Account savedAccount = accountRepository.save(account);

		AccountResponseDTO response = new AccountResponseDTO();

		response.setId(savedAccount.getId());
		response.setAccountNumber(savedAccount.getAccountNumber());
		response.setBalance(savedAccount.getBalance());
		response.setStatus(savedAccount.getStatus());
		response.setUserId(savedAccount.getUser().getId());

		return response;
	}

	public List<AccountResponseDTO> getAllAccounts() {

		List<Account> accounts = accountRepository.findAll();

		return accounts.stream().map(account -> {

			AccountResponseDTO response = new AccountResponseDTO();

			response.setId(account.getId());
			response.setAccountNumber(account.getAccountNumber());
			response.setBalance(account.getBalance());
			response.setStatus(account.getStatus());
			response.setUserId(account.getUser().getId());

			return response;

		}).collect(Collectors.toList());
	}

	public List<AccountResponseDTO> getAccountsByUserId(Long userId) {
		return accountRepository.findByUser_Id(userId).stream().map(account -> {
			AccountResponseDTO dto = new AccountResponseDTO();
			dto.setId(account.getId());
			dto.setAccountNumber(account.getAccountNumber());
			dto.setBalance(account.getBalance());
			dto.setStatus(account.getStatus());
			dto.setUserId(account.getUser().getId());
			return dto;
		}).toList();
	}

	public Long getUserIdByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"))
				.getId();
	}

	public List<ReceiverAccountDTO> getReceiverAccounts(String username) {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return accountRepository.findReceiverAccounts(user.getId());
	}
}