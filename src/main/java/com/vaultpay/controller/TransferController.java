package com.vaultpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultpay.dto.TransferRequestDTO;
import com.vaultpay.dto.TransferResponseDTO;
import com.vaultpay.service.TransferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

	@Autowired
	TransferService transferService;

	@PostMapping
	public TransferResponseDTO transfer(@Valid @RequestBody TransferRequestDTO transferRequest,
			Authentication authentication) {

		String username = authentication.getName();

		return transferService.transfer(transferRequest, username);
	}
}