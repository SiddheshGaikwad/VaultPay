package com.vaultpay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferResponseDTO {

    private Long transactionId;

    private Long senderAccountId;

    private Long receiverAccountId;

    private BigDecimal amount;

    private String status;

    private LocalDateTime createdAt;

    private String transactionReference;
}