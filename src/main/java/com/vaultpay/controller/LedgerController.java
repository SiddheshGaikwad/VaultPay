package com.vaultpay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultpay.dto.LedgerResponseDTO;
import com.vaultpay.service.LedgerService;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

	@Autowired
	LedgerService ledgerService;

	@GetMapping
	public List<LedgerResponseDTO> getAllLedgerEntries() {
		return ledgerService.getAllLedgerEntries();
	}

	@GetMapping("/account/{accountId}")
	public List<LedgerResponseDTO> getLedgerByAccount(@PathVariable Long accountId, Authentication authentication) {

		String username = authentication.getName();

		return ledgerService.getLedgerByAccount(accountId, username);
	}
}