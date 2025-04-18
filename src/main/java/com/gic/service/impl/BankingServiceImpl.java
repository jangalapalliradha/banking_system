package com.gic.service.impl;

import com.gic.constants.TransactionType;
import com.gic.exception.TransactionValidationException;
import com.gic.model.InterestRuleDTO;
import com.gic.model.TransactionDetailsDTO;
import com.gic.service.BankingService;
import com.gic.service.interestRule.InterestRuleService;
import com.gic.service.print.PrintService;
import com.gic.service.transaction.TransactionService;
import com.gic.utils.HelperUtil;
import com.gic.validation.TransactionValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class BankingServiceImpl implements BankingService {

    private final TransactionService transactionService;
    private final InterestRuleService interestRuleService;
    private final TransactionValidator transactionValidator;
    private final PrintService printService;
    private final HelperUtil helperUtil;

    Map<String, BigDecimal> accountBalanceMap = new HashMap<>();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public BankingServiceImpl(TransactionService transactionService, InterestRuleService interestRuleService, TransactionValidator transactionValidator, PrintService printService, HelperUtil helperUtil) {
        this.transactionService = transactionService;
        this.interestRuleService = interestRuleService;
        this.transactionValidator = transactionValidator;
        this.printService = printService;
        this.helperUtil = helperUtil;
    }

    private static void accept(InterestRuleDTO ruleDTO) {
        System.out.printf("| %-8s | %-6s | %9.2f |\n",
                ruleDTO.getDate().format(dateFormatter),
                ruleDTO.getRuleId(),
                ruleDTO.getRate());
    }

    /**
     * @param scanner inputscanner
     */
    @Override
    public void processTransaction(Scanner scanner) {
        welcomeText();

        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    // Return to main menu
                    break;
                }
                // Example input: 20230626 AC001 W 100.00
                String[] parts = input.split("\\s+");
                transactionValidator.validate(parts);
                LocalDate date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
                String account = parts[1];
                char type = Character.toUpperCase(parts[2].charAt(0));
                String typeInStr = String.valueOf(type);
                BigDecimal amount = new BigDecimal(parts[3]);
                TransactionDetailsDTO transactionDetailsDTO = setTransactionDetailsDTO(account, typeInStr, amount, date);
                transactionValidator.validateAndProcessTransaction(accountBalanceMap, transactionDetailsDTO);
                transactionService.addTransaction(transactionDetailsDTO);
                printService.printFormat(account, null, null, null,false);

            }catch (TransactionValidationException e) {
                System.out.println("❌ " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.out.println("⚠️ Unexpected error: " + e.getMessage() + "\n");
            }
            endingText();
        }
    }

    /**
     * @param scanner input
     */
    @Override
    public void applyInterstRates(Scanner scanner) {
        while (true) {
            interestRuleBegText();
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                break;
            }

            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Invalid input format. Please try again.");
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(parts[0], dateFormatter);
                String ruleId = parts[1];
                double rate = Double.parseDouble(parts[2]);

                if (rate <= 0 || rate >= 100) {
                    System.out.println("Rate must be greater than 0 and less than 100.");
                    continue;
                }
                interestRuleService.saveOrUpdateRule(date, ruleId, rate);
                displayInterestRules();

            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use YYYYMMdd.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid rate format. Must be a number.");
            }
        }
    }

    /**
     * To load all the transactions into map to validate for withdraw amount should not exceed deposit
     */
    @Override
    public void loadTrasactions() {
        List<TransactionDetailsDTO> allTransactions = transactionService.getAllTransactions();

        allTransactions.forEach(transaction -> {
            BigDecimal balance = accountBalanceMap.getOrDefault(transaction.getAccount(), BigDecimal.ZERO);
            if (Objects.equals(transaction.getType(), TransactionType.D.name()) || Objects.equals(transaction.getType(), TransactionType.I.name())) {
                balance = balance.add(transaction.getAmount());
            } else if (Objects.equals(transaction.getType(), TransactionType.W.name())) {
                balance = balance.subtract(transaction.getAmount());
            }
            accountBalanceMap.put(transaction.getAccount(), balance);
        });

    }

    @Override
    public void printStatement(Scanner scanner) {
        printBeginningText("Please enter account and month to generate the statement <Account> <Year><Month>");
        endingText();
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) return;

        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Invalid input format.");
            return;
        }
        String account = parts[0];
        String yearMonth = parts[1];

        try {
            YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyyMM"));
            LocalDate fromDate = ym.atDay(1);
            LocalDate toDate = ym.atEndOfMonth();
            printService.printFormat(account, fromDate, toDate, yearMonth, true);

        } catch (DateTimeParseException e) {
            System.out.println("Invalid year and month format.");
        }
    }

    private TransactionDetailsDTO setTransactionDetailsDTO(String account, String typeInStr, BigDecimal amount, LocalDate date) {
        TransactionDetailsDTO transactionDetailsDTO = new TransactionDetailsDTO();
        transactionDetailsDTO.setAccount(account);
        transactionDetailsDTO.setType(typeInStr);
        transactionDetailsDTO.setAmount(amount);
        transactionDetailsDTO.setDate(date);
        transactionDetailsDTO.setTxnId(helperUtil.generateTransactionId(date));
        return transactionDetailsDTO;
    }

    private void displayInterestRules() {
        List<InterestRuleDTO> interestRuleDTOS = interestRuleService.getAllRulesSortedByDate() ;
        System.out.println("\nInterest rules:");
        System.out.println("| Date     | RuleId | Rate (%) |");
        interestRuleDTOS.forEach(BankingServiceImpl::accept);
    }

    private static void endingText() {
        System.out.print("> ");
        System.out.print("``` ");
    }

    private static void welcomeText() {
        printBeginningText("Please enter transaction details in <Date> <Account> <Type> <Amount> format");
        System.out.println(">");
        System.out.print("``` ");
    }

    private static void printBeginningText(String message) {
        System.out.print("``` ");
        System.out.println(message);
        System.out.println("(or enter blank to go back to main menu):");
    }

    private static void interestRuleBegText() {
        System.out.println("\nPlease enter interest rules details in <Date> <RuleId> <Rate in %> format");
        System.out.println("(or enter blank to go back to main menu):");
        System.out.print("> ");
    }

}
