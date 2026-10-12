package com.vaultpay.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDTO {

    @NotNull(message = "Sender account ID is required")
    private Long senderAccountId;

    @NotNull(message = "Receiver account ID is required")
    private Long receiverAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;
}