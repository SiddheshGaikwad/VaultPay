package com.vaultpay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vaultpay.entity.LedgerEntry;
import com.vaultpay.entity.Account;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByAccount(Account account);
}