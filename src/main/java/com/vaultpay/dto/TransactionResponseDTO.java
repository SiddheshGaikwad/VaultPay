package com.vaultpay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionResponseDTO {

    private Long id;

    private String transactionReference;

    private BigDecimal amount;

    private String type;

    private String status;

    private LocalDateTime createdAt;
    
    private Long senderAccountId;
    private Long receiverAccountId;
}
