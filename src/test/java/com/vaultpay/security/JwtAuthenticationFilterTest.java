package com.vaultpay.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthenticationFilterTest {

	JwtService jwtService;

	JwtAuthenticationFilter filter;

	@BeforeEach
	void setup() {

		jwtService = new JwtService();

		filter = new JwtAuthenticationFilter();

		filter.jwtService = jwtService;

		SecurityContextHolder.clearContext();

		ReflectionTestUtils.setField(jwtService, "secretKey", "VaultPaySecretKeyForJWTAuthentication123456789");
	}

	@Test
	void validCustomerTokenShouldAuthenticate() throws Exception {

		String token = jwtService.generateToken("testuser", "CUSTOMER");

		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		request.addHeader("Authorization", "Bearer " + token);

		FilterChain filterChain = Mockito.mock(FilterChain.class);

		filter.doFilter(request, response, filterChain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);

		assertEquals("testuser", authentication.getName());

		assertEquals(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER")),
				authentication.getAuthorities());
	}

	@Test
	void validAdminTokenShouldAuthenticate() throws Exception {

		String token = jwtService.generateToken("admin", "ADMIN");

		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		request.addHeader("Authorization", "Bearer " + token);

		FilterChain filterChain = Mockito.mock(FilterChain.class);

		filter.doFilter(request, response, filterChain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);

		assertEquals("admin", authentication.getName());

		assertEquals(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")),
				authentication.getAuthorities());
	}

	@Test
	void requestWithoutTokenShouldNotAuthenticate() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		FilterChain filterChain = Mockito.mock(FilterChain.class);

		filter.doFilter(request, response, filterChain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		assertEquals(null, authentication);
	}
}