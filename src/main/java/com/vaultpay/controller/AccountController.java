package com.vaultpay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultpay.dto.AccountResponseDTO;
import com.vaultpay.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
	
	@Autowired
	AccountService accountService;
	
	@PostMapping("/{userId}")
	public AccountResponseDTO createAccount(@PathVariable Long userId) {
		return accountService.createAccount(userId) ;
		
		
	}
	
	@GetMapping
	public List<AccountResponseDTO> getAllAccounts() {

	    return accountService.getAllAccounts();
	}

}
