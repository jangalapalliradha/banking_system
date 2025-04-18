package com.gic.dao;

import com.gic.entity.TransactionEntity;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepositoryDao extends JpaRepository<TransactionEntity, Id> {
    List<TransactionEntity> findByAccount(String account);
    List<TransactionEntity> findByAccountAndDateBetween(String account, LocalDate from, LocalDate to);
    List<TransactionEntity> findByAccountAndDateBefore(String account, LocalDate date);
    @Query("SELECT DISTINCT transaction.account FROM TransactionEntity transaction")
    List<String> findDistinctAccountIds();
}
