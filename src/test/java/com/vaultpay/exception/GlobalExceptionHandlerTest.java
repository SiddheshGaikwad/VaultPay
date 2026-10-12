package com.vaultpay.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

	GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void resourceNotFoundShouldReturn404() {

		ResourceNotFoundException exception = new ResourceNotFoundException("Account not found");

		ResponseEntity<Map<String, Object>> response = handler.handleNotFound(exception);

		assertEquals(404, response.getStatusCode().value());

		assertEquals("Not Found", response.getBody().get("error"));

		assertEquals("Account not found", response.getBody().get("message"));
	}

	@Test
	void badRequestShouldReturn400() {

		BadRequestException exception = new BadRequestException("Insufficient balance");

		ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(exception);

		assertEquals(400, response.getStatusCode().value());

		assertEquals("Bad Request", response.getBody().get("error"));

		assertEquals("Insufficient balance", response.getBody().get("message"));
	}
}