package com.vaultpay.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

	@Autowired
	JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/api/users/login").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/users").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")

						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						.requestMatchers("/api/customer/**").hasRole("CUSTOMER")

						.requestMatchers(HttpMethod.GET, "/api/accounts").hasRole("ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/accounts/**").hasRole("ADMIN")

						.requestMatchers("/api/accounts/**").hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/transactions").hasRole("ADMIN")

						.requestMatchers("/api/transactions/**").hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers("/api/transfers/**").hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/ledger").hasRole("ADMIN")

						.requestMatchers("/api/ledger/**").hasAnyRole("CUSTOMER", "ADMIN")

						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml")
						.permitAll()

						.requestMatchers("/actuator/health", "/actuator/info").permitAll()

						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}