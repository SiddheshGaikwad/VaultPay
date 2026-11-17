package com.vaultpay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.vaultpay.dto.AccountResponseDTO;
import com.vaultpay.dto.ReceiverAccountDTO;
import com.vaultpay.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	@Autowired
	AccountService accountService;

	@PostMapping("/{userId}")
	public AccountResponseDTO createAccount(@PathVariable Long userId) {
		return accountService.createAccount(userId);

	}

	@GetMapping
	public List<AccountResponseDTO> getAllAccounts() {

		return accountService.getAllAccounts();
	}

	@GetMapping("/user/{userId}")
	public List<AccountResponseDTO> getAccountsByUserId(@PathVariable Long userId,
			org.springframework.security.core.Authentication authentication) {

		boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin) {
			Long loggedInUserId = accountService.getUserIdByUsername(authentication.getName());

			if (!loggedInUserId.equals(userId)) {
				throw new org.springframework.security.access.AccessDeniedException(
						"You cannot access another user's accounts");
			}
		}

		return accountService.getAccountsByUserId(userId);
	}

	@GetMapping("/receivers")
	public List<ReceiverAccountDTO> getReceiverAccounts(Authentication authentication) {

		return accountService.getReceiverAccounts(authentication.getName());
	}
}
