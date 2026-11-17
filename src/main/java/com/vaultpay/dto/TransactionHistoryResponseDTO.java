	package com.vaultpay.dto;
	
	import java.math.BigDecimal;
	import java.time.LocalDateTime;
	
	import lombok.Getter;
	import lombok.Setter;
	
	@Getter
	@Setter
	public class TransactionHistoryResponseDTO {
	
	    private Long transactionId;
	
	    private String transactionReference;
	
	    private BigDecimal amount;
	
	    private String type;
	
	    private String status;
	
	    private LocalDateTime createdAt;
	    
	    private Long receiverAccountId;
	}