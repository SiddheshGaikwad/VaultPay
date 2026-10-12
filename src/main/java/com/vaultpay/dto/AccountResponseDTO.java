package com.vaultpay.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponseDTO {

	private Long id;
	private String accountNumber;
	private BigDecimal balance;
	private String status;
	private Long userId;
}
