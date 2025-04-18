package com.gic.service.transaction;

import com.gic.model.TransactionDetailsDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
     void addTransaction(TransactionDetailsDTO transactionDetails);
     List<TransactionDetailsDTO> getAllTransactions();

     List<TransactionDetailsDTO> getTransactionsBetweenDates(String account, LocalDate fromDate, LocalDate toDate);
     List<TransactionDetailsDTO> getTransactionsDateBefore(String account, LocalDate fromDate);
     List<TransactionDetailsDTO> getTransactionsByAcct(String account);
     void createInterestTransaction(String accountId, BigDecimal amount, LocalDate date);
}
