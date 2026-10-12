package com.vaultpay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LedgerResponseDTO {

    private Long ledgerId;
    private Long accountId;
    private String entryType;
    private BigDecimal amount;
    private String transactionReference;
    private LocalDateTime createdAt;
}