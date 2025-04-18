package com.gic.service;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import com.gic.model.TransactionDetailsDTO;
import com.gic.service.impl.BankingServiceImpl;
import com.gic.service.interestRule.InterestRuleService;
import com.gic.service.print.PrintService;
import com.gic.service.transaction.TransactionService;
import com.gic.utils.HelperUtil;
import com.gic.validation.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankingServiceImplTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private InterestRuleService interestRuleService;

    @Mock
    private TransactionValidator transactionValidator;

    @Mock
    private PrintService printService;

    @Mock
    private HelperUtil helperUtil;

    @InjectMocks
    private BankingServiceImpl bankingService;

    private TransactionDetailsDTO transactionDTO;

    @BeforeEach
    void setUp() {
        transactionDTO = new TransactionDetailsDTO();
        transactionDTO.setAccount("AC001");
        transactionDTO.setType("D");
        transactionDTO.setAmount(new BigDecimal("100.00"));
        transactionDTO.setDate(LocalDate.of(2023, 6, 26));
        transactionDTO.setTxnId("TXN001");
    }

    @Test
    void testLoadTransactions() {
        List<TransactionDetailsDTO> transactions = new ArrayList<>();
        transactions.add(transactionDTO);

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        bankingService.loadTrasactions();

    }

    @Test
    void testPrintStatement_ValidInput() {
        Scanner scanner = new Scanner("AC001 202306\n");
        doNothing().when(printService).printFormat(anyString(), any(), any(), any(), eq(true));

        bankingService.printStatement(scanner);

        verify(printService).printFormat(eq("AC001"),
                eq(LocalDate.of(2023, 6, 1)),
                eq(LocalDate.of(2023, 6, 30)),
                eq("202306"),
                eq(true));
    }

    @Test
    void testApplyInterestRates_ValidInput() {
        Scanner scanner = new Scanner("20230626 RULE01 2.50\n\n");

        when(interestRuleService.getAllRulesSortedByDate()).thenReturn(Collections.emptyList());

        bankingService.applyInterstRates(scanner);

        verify(interestRuleService).saveOrUpdateRule(eq(LocalDate.of(2023, 6, 26)), eq("RULE01"), eq(2.5));
    }

    @Test
    void testProcessTransaction_ValidDeposit() {
        Scanner scanner = new Scanner("20230626 AC001 D 100.00\n\n");

        when(helperUtil.generateTransactionId(any())).thenReturn("TXN123");

        doNothing().when(transactionValidator).validate(any());
        doNothing().when(transactionValidator).validateAndProcessTransaction(any(), any());
        doNothing().when(transactionService).addTransaction(any());
        doNothing().when(printService).printFormat(any(), any(), any(), any(), eq(false));

        bankingService.processTransaction(scanner);

        verify(transactionService).addTransaction(any());
    }
}