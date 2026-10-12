package com.vaultpay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultpay.dto.LoginRequestDTO;
import com.vaultpay.dto.LoginResponseDTO;
import com.vaultpay.dto.UserRequestDTO;
import com.vaultpay.dto.UserResponseDTO;
import com.vaultpay.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	UserService userService;

	@PostMapping
	public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO userRequest) {

		return userService.createUser(userRequest);
	}

	@PostMapping("/login")
	public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequest) {

		return userService.login(loginRequest);
	}

	@GetMapping
	public List<UserResponseDTO> getAllUsers() {

		return userService.getAllUsers();
	}

}
