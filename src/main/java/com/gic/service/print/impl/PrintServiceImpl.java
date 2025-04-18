package com.gic.service.print.impl;

import com.gic.constants.TransactionType;
import com.gic.model.TransactionDetailsDTO;
import com.gic.service.print.PrintService;
import com.gic.service.transaction.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PrintServiceImpl implements PrintService {

    private final TransactionService transactionService;

    public PrintServiceImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * @param account account
     * @param fromDate fromDate
     * @param toDate endDate
     * @param yearMonth yearMonth
     * @param isFromPrintFunction boolean
     */
    @Override
    public void printFormat(String account, LocalDate fromDate, LocalDate toDate, String yearMonth, Boolean isFromPrintFunction) {
        List<TransactionDetailsDTO> transactions;
        BigDecimal balance = new BigDecimal(0);
        if(isFromPrintFunction) {
            transactions = transactionService.getTransactionsBetweenDates(account, fromDate, toDate);
            // Compute opening balance
            balance = calculateOpeningBalance(account, fromDate);
        } else {
            transactions = transactionService.getTransactionsByAcct(account);
        }
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for account: " + account + " in " + yearMonth);
            return;
        }

        System.out.println("Account: " + account);
        if(isFromPrintFunction) {
            System.out.printf("| %-10s | %-12s | %-4s | %-8s | %-8s |%n", "Date", "Txn Id", "Type", "Amount", "Balance");
        } else{
            System.out.printf("| %-10s | %-12s | %-4s | %-8s |%n", "Date", "Txn Id", "Type", "Amount");
        }
        for (TransactionDetailsDTO txn : transactions) {
            if (txn.getType().equalsIgnoreCase(TransactionType.D.toString()) || txn.getType().equalsIgnoreCase(TransactionType.I.name())) {
                balance = balance.add(txn.getAmount());
            } else if (txn.getType().equalsIgnoreCase(TransactionType.W.toString())) {
                balance = balance.subtract(txn.getAmount());
            }

            if(isFromPrintFunction){
                System.out.printf("| %-10s | %-12s | %-4s | %8.2f | %8.2f |%n",
                        txn.getDate(), txn.getTxnId(), txn.getType(), txn.getAmount(), balance);
            } else {
                System.out.printf("| %-10s | %-12s | %-4s | %8.2f |%n",
                        txn.getDate(), txn.getTxnId(), txn.getType(), txn.getAmount());
            }
        }
    }

    private BigDecimal calculateOpeningBalance(String account, LocalDate fromDate) {
        List<TransactionDetailsDTO> previousTxns = transactionService.getTransactionsDateBefore(account, fromDate);
        BigDecimal balance = BigDecimal.ZERO;

        for (TransactionDetailsDTO txn : previousTxns) {
            if (txn.getType().equalsIgnoreCase(TransactionType.D.name()) || txn.getType().equalsIgnoreCase(TransactionType.I.name())) {
                balance = balance.add(txn.getAmount());
            } else if (txn.getType().equalsIgnoreCase(TransactionType.W.name())) {
                balance = balance.subtract(txn.getAmount());
            }
        }
        return balance;
    }
}
