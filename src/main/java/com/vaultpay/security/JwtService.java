package com.vaultpay.security;

import java.util.Date;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Service
public class JwtService {

	@Value("${vaultpay.jwt.secret}")
	private String secretKey;
	private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

	private SecretKey getSigningKey() {
	    return Keys.hmacShaKeyFor(
	            secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	public String generateToken(String username, String role) {

		return Jwts.builder().subject(username).claim("role", role).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).signWith(getSigningKey()).compact();
	}

	public String extractUsername(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public String extractRole(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().get("role",
				String.class);
	}
}