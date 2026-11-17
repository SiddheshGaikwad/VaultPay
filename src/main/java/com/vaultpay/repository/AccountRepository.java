package com.vaultpay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vaultpay.dto.ReceiverAccountDTO;
import com.vaultpay.entity.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

	boolean existsByUser(com.vaultpay.entity.User user);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM Account a WHERE a.id = :id")
	Optional<Account> findByIdForUpdate(@Param("id") Long id);

	List<Account> findByUser_Id(Long userId);

	Optional<Account> findByAccountNumber(String accountNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
	Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

	@Query("""
			SELECT new com.vaultpay.dto.ReceiverAccountDTO(
			    a.id, a.accountNumber
			)
			FROM Account a
			WHERE a.status = 'ACTIVE'
			  AND a.user.id <> :userId
			ORDER BY a.accountNumber
			""")
	List<ReceiverAccountDTO> findReceiverAccounts(@Param("userId") Long userId);
}
