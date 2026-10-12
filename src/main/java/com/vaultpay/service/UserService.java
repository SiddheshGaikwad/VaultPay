package com.vaultpay.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vaultpay.dto.LoginRequestDTO;
import com.vaultpay.dto.LoginResponseDTO;
import com.vaultpay.dto.UserRequestDTO;
import com.vaultpay.dto.UserResponseDTO;
import com.vaultpay.entity.User;
import com.vaultpay.exception.BadRequestException;
import com.vaultpay.repository.UserRepository;
import com.vaultpay.security.JwtService;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtService jwtService;

	public UserResponseDTO createUser(UserRequestDTO userRequest) {

		User user = new User();

		user.setUsername(userRequest.getUsername());

		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

		user.setRole(userRequest.getRole());

		User savedUser = userRepository.save(user);

		UserResponseDTO response = new UserResponseDTO();

		response.setId(savedUser.getId());
		response.setUsername(savedUser.getUsername());
		response.setRole(savedUser.getRole());

		return response;
	}

	public LoginResponseDTO login(LoginRequestDTO loginRequest) {

		User user = userRepository.findByUsername(loginRequest.getUsername())
				.orElseThrow(() -> new BadRequestException("Invalid username or password"));

		if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

			throw new BadRequestException("Invalid username or password");
		}

		LoginResponseDTO response = new LoginResponseDTO();

		response.setMessage("Login successful");
		response.setUserId(user.getId());
		response.setUsername(user.getUsername());
		response.setRole(user.getRole());

		String token = jwtService.generateToken(user.getUsername(), user.getRole());

		response.setToken(token);

		return response;

	}

	public List<UserResponseDTO> getAllUsers() {

		List<User> users = userRepository.findAll();

		return users.stream().map(user -> {

			UserResponseDTO response = new UserResponseDTO();

			response.setId(user.getId());
			response.setUsername(user.getUsername());
			response.setRole(user.getRole());

			return response;

		}).collect(Collectors.toList());
	}

}
