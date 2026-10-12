package com.vaultpay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vaultpay.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

}
