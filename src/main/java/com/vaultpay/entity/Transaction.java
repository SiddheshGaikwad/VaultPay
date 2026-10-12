package com.vaultpay.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
public class Transaction {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionReference;

    private BigDecimal amount;

    private String type;

    private String status;

    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;
    
    @ManyToOne
    @JoinColumn(name = "receiver_account_id")
    private Account receiverAccount;
}
