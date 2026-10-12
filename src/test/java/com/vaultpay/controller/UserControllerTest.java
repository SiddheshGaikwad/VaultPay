package com.vaultpay.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vaultpay.dto.LoginResponseDTO;
import com.vaultpay.dto.UserResponseDTO;
import com.vaultpay.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	private MockMvc mockMvc;

	@Mock
	UserService userService;

	@InjectMocks
	UserController userController;

	@BeforeEach
	void setup() {

		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
	}

	// 1. Create user successfully

	@Test
	void createUserShouldReturn200() throws Exception {

		UserResponseDTO response = new UserResponseDTO();

		response.setId(10L);
		response.setUsername("newuser");
		response.setRole("CUSTOMER");

		when(userService.createUser(any())).thenReturn(response);

		String requestBody = """
				{
				    "username": "newuser",
				    "password": "test123",
				    "role": "CUSTOMER"
				}
				""";

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.username").value("newuser")).andExpect(jsonPath("$.role").value("CUSTOMER"));
	}

	// 2. Login successfully

	@Test
	void loginShouldReturn200() throws Exception {

		LoginResponseDTO response = new LoginResponseDTO();

		response.setMessage("Login successful");
		response.setUserId(4L);
		response.setUsername("testuser");
		response.setRole("CUSTOMER");
		response.setToken("jwt-test-token");

		when(userService.login(any())).thenReturn(response);

		String requestBody = """
				{
				    "username": "testuser",
				    "password": "test123"
				}
				""";

		mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Login successful"))
				.andExpect(jsonPath("$.userId").value(4)).andExpect(jsonPath("$.username").value("testuser"))
				.andExpect(jsonPath("$.role").value("CUSTOMER")).andExpect(jsonPath("$.token").value("jwt-test-token"));
	}

	// 3. Get all users successfully

	@Test
	void getAllUsersShouldReturn200() throws Exception {

		UserResponseDTO user1 = new UserResponseDTO();

		user1.setId(4L);
		user1.setUsername("testuser");
		user1.setRole("CUSTOMER");

		UserResponseDTO user2 = new UserResponseDTO();

		user2.setId(5L);
		user2.setUsername("admin");
		user2.setRole("ADMIN");

		when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

		mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(4)).andExpect(jsonPath("$[0].username").value("testuser"))
				.andExpect(jsonPath("$[0].role").value("CUSTOMER")).andExpect(jsonPath("$[1].id").value(5))
				.andExpect(jsonPath("$[1].username").value("admin")).andExpect(jsonPath("$[1].role").value("ADMIN"));
	}

	// 4. Empty user list

	@Test
	void getAllUsersShouldReturnEmptyList() throws Exception {

		when(userService.getAllUsers()).thenReturn(List.of());

		mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	// 5. Create user with invalid data

	@Test
	void createUserShouldFailWithInvalidData() throws Exception {

		String requestBody = """
				{
				    "username": "",
				    "password": "",
				    "role": ""
				}
				""";

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}

	// 6. Create user with missing fields

	@Test
	void createUserShouldFailWhenFieldsAreMissing() throws Exception {

		String requestBody = """
				{
				    "username": "testuser"
				}
				""";

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}
}