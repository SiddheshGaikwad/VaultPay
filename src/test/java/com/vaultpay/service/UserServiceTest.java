package com.vaultpay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaultpay.dto.LoginRequestDTO;
import com.vaultpay.dto.LoginResponseDTO;
import com.vaultpay.dto.UserRequestDTO;
import com.vaultpay.dto.UserResponseDTO;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.repository.UserRepository;
import com.vaultpay.security.JwtService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	JwtService jwtService;

	@InjectMocks
	UserService userService;

	User user;

	@BeforeEach
	void setup() {

		user = new User();

		user.setId(4L);
		user.setUsername("testuser");
		user.setPassword("encodedPassword");
		user.setRole("CUSTOMER");
	}

	// 1. Create user successfully

	@Test
	void createUserShouldBeSuccessful() {

		UserRequestDTO request = new UserRequestDTO();

		request.setUsername("testuser");
		request.setPassword("test123");
		request.setRole("CUSTOMER");

		when(passwordEncoder.encode("test123")).thenReturn("encodedPassword");

		when(userRepository.save(any(User.class))).thenReturn(user);

		UserResponseDTO response = userService.createUser(request);

		assertNotNull(response);

		assertEquals(4L, response.getId());

		assertEquals("testuser", response.getUsername());

		assertEquals("CUSTOMER", response.getRole());
	}

	// 2. Login successfully

	@Test
	void loginShouldBeSuccessful() {

		LoginRequestDTO request = new LoginRequestDTO();

		request.setUsername("testuser");
		request.setPassword("test123");

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(passwordEncoder.matches("test123", "encodedPassword")).thenReturn(true);

		when(jwtService.generateToken("testuser", "CUSTOMER")).thenReturn("jwt-test-token");

		LoginResponseDTO response = userService.login(request);

		assertNotNull(response);

		assertEquals("Login successful", response.getMessage());

		assertEquals(4L, response.getUserId());

		assertEquals("testuser", response.getUsername());

		assertEquals("CUSTOMER", response.getRole());

		assertEquals("jwt-test-token", response.getToken());
	}

	// 3. Login fails with wrong password

	@Test
	void loginShouldFailWithWrongPassword() {

		LoginRequestDTO request = new LoginRequestDTO();

		request.setUsername("testuser");
		request.setPassword("wrongpassword");

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

		BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.login(request));

		assertEquals("Invalid username or password", exception.getMessage());
	}

	// 4. Login fails when user does not exist

	@Test
	void loginShouldFailWhenUserNotFound() {

		LoginRequestDTO request = new LoginRequestDTO();

		request.setUsername("unknown");
		request.setPassword("test123");

		when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

		BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.login(request));

		assertEquals("Invalid username or password", exception.getMessage());
	}

	// 5. Get all users successfully

	@Test
	void getAllUsersShouldBeSuccessful() {

		User user2 = new User();

		user2.setId(5L);
		user2.setUsername("admin");
		user2.setPassword("encodedPassword");
		user2.setRole("ADMIN");

		when(userRepository.findAll()).thenReturn(List.of(user, user2));

		List<UserResponseDTO> response = userService.getAllUsers();

		assertNotNull(response);

		assertEquals(2, response.size());

		assertEquals(4L, response.get(0).getId());

		assertEquals("testuser", response.get(0).getUsername());

		assertEquals("CUSTOMER", response.get(0).getRole());

		assertEquals(5L, response.get(1).getId());

		assertEquals("admin", response.get(1).getUsername());

		assertEquals("ADMIN", response.get(1).getRole());
	}

	// 6. Empty user list

	@Test
	void getAllUsersShouldReturnEmptyList() {

		when(userRepository.findAll()).thenReturn(List.of());

		List<UserResponseDTO> response = userService.getAllUsers();

		assertNotNull(response);

		assertEquals(0, response.size());
	}
}