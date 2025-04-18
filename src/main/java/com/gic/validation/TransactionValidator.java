package com.gic.validation;

import com.gic.exception.TransactionValidationException;
import com.gic.model.TransactionDetailsDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class TransactionValidator {

    /**
     * It validates the input as per required format
     * @param parts input values
     */
    public  void validate(String[] parts) {
        if (parts.length != 4) {
            throw new TransactionValidationException("Invalid input. Format: <Date> <Account> <Type> <Amount>");
        }
        try {
            LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            throw new TransactionValidationException("Invalid date format. Expected YYYYMMdd.");
        }

        String account = parts[1];
        if (account.isEmpty()) {
            throw new TransactionValidationException("Account cannot be empty.");
        }

        char type = Character.toUpperCase(parts[2].charAt(0));
        if (type != 'D' && type != 'W') {
            throw new TransactionValidationException("Transaction type must be D or W.");
        }

        try {
            BigDecimal amount = new BigDecimal(parts[3]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2) {
                throw new TransactionValidationException("Amount must be > 0 and up to 2 decimal places.");
            }
        } catch (NumberFormatException e) {
            throw new TransactionValidationException("Invalid amount. Must be a valid decimal number.");
        }
    }

    public void validateAndProcessTransaction(Map<String, BigDecimal> accountBalanceMap, TransactionDetailsDTO transaction) {
        BigDecimal balance = accountBalanceMap.getOrDefault(transaction.getAccount(), BigDecimal.ZERO);

        // First transaction must not be a withdrawal
        if (!accountBalanceMap.containsKey(transaction.getAccount()) && transaction.getType().equalsIgnoreCase("W")) {
            throw new TransactionValidationException("First transaction for an account cannot be a withdrawal.");
        }
        // Withdrawal should not make balance negative
        if (transaction.getType().equalsIgnoreCase("W")) {
            if (balance.compareTo(transaction.getAmount()) < 0) {
                throw new TransactionValidationException("Insufficient balance for withdrawal.");
            }
            balance = balance.subtract(transaction.getAmount());
        } else if (transaction.getType().equalsIgnoreCase("D")) {
            balance = balance.add(transaction.getAmount());
        }

        accountBalanceMap.put(transaction.getAccount(), balance);
    }

}
