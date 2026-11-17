package com.vaultpay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiverAccountDTO {

    private Long id;
    private String accountNumber;

    public ReceiverAccountDTO(Long id, String accountNumber) {
        this.id = id;
        this.accountNumber = accountNumber;
    }
}
