package com.gic.service.print;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.gic.constants.TransactionType;
import com.gic.model.TransactionDetailsDTO;
import com.gic.service.print.impl.PrintServiceImpl;
import com.gic.service.transaction.TransactionService;
import org.junit.jupiter.api.*;
import org.mockito.*;

class PrintServiceImplTest {

    @Mock
    private TransactionService transactionService;

    private PrintServiceImpl printService;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        printService = new PrintServiceImpl(transactionService);
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testPrintFormat_WithTransactions_FromPrintFunction() {
        String account = "12345";
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 1, 31);
        String yearMonth = "2024-01";

        List<TransactionDetailsDTO> before = List.of(
                new TransactionDetailsDTO("T1", LocalDate.of(2023, 12, 25), TransactionType.D.name(), new BigDecimal("100"))
        );

        List<TransactionDetailsDTO> during = List.of(
                new TransactionDetailsDTO("T2", LocalDate.of(2024, 1, 5), TransactionType.W.name(), new BigDecimal("30")),
                new TransactionDetailsDTO("T3", LocalDate.of(2024, 1, 15), TransactionType.D.name(), new BigDecimal("50"))
        );

        when(transactionService.getTransactionsDateBefore(account, fromDate)).thenReturn(before);
        when(transactionService.getTransactionsBetweenDates(account, fromDate, toDate)).thenReturn(during);

        printService.printFormat(account, fromDate, toDate, yearMonth, true);

        String output = outContent.toString();
        assertTrue(output.contains("Account: 12345"));
        assertTrue(output.contains("T2"));
        assertTrue(output.contains("T3"));
        assertTrue(output.contains("Balance"));
    }

    @Test
    void testPrintFormat_WithTransactions_NotFromPrintFunction() {
        String account = "999";
        String yearMonth = "2024-01";

        List<TransactionDetailsDTO> txns = List.of(
                new TransactionDetailsDTO("T10", LocalDate.of(2024, 1, 10), TransactionType.D.name(), new BigDecimal("100")),
                new TransactionDetailsDTO("T11", LocalDate.of(2024, 1, 11), TransactionType.W.name(), new BigDecimal("20"))
        );

        when(transactionService.getTransactionsByAcct(account)).thenReturn(txns);

        printService.printFormat(account, null, null, yearMonth, false);

        String output = outContent.toString();
        assertTrue(output.contains("T10"));
        assertTrue(output.contains("T11"));
        assertFalse(output.contains("Balance"));
    }

    @Test
    void testPrintFormat_NoTransactions() {
        String account = "888";
        String yearMonth = "2024-02";
        LocalDate fromDate = LocalDate.of(2024, 2, 1);
        LocalDate toDate = LocalDate.of(2024, 2, 28);

        when(transactionService.getTransactionsBetweenDates(account, fromDate, toDate)).thenReturn(List.of());

        printService.printFormat(account, fromDate, toDate, yearMonth, true);

        String output = outContent.toString();
        assertTrue(output.contains("No transactions found for account: 888 in 2024-02"));
    }
}
