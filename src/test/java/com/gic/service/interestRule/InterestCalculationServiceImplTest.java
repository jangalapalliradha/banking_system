package com.gic.service.interestRule;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import com.gic.dao.InterestRuleRepository;
import com.gic.dao.TransactionRepositoryDao;
import com.gic.entity.InterestRuleEntity;
import com.gic.entity.TransactionEntity;
import com.gic.model.InterestRuleDTO;
import com.gic.service.interestRule.impl.InterestCalculationServiceImpl;
import com.gic.service.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class InterestCalculationServiceImplTest {

    @Mock
    private TransactionRepositoryDao transactionRepositoryDao;

    @Mock
    private InterestRuleRepository interestRuleRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private InterestCalculationServiceImpl interestCalculationService;

    private final YearMonth testMonth = YearMonth.of(2023, 6);

    private final LocalDate startDate = LocalDate.of(2023, 6, 1);
    private final LocalDate endDate = LocalDate.of(2023, 6, 30);

    private List<TransactionEntity> monthlyTxns;
    private List<TransactionEntity> previousTxns;
    private List<InterestRuleEntity> ruleEntities;

    @BeforeEach
    void setUp() {
        // Prepare transactions
        monthlyTxns = new ArrayList<>();
        previousTxns = new ArrayList<>();

        // Add one monthly transaction
        TransactionEntity txn1 = new TransactionEntity();
        txn1.setDate(startDate.plusDays(10));
        txn1.setAmount(BigDecimal.valueOf(1000));
        monthlyTxns.add(txn1);

        // Add previous deposit
        TransactionEntity prevTxn = new TransactionEntity();
        prevTxn.setDate(startDate.minusDays(5));
        prevTxn.setAmount(BigDecimal.valueOf(2000));
        previousTxns.add(prevTxn);

        // Prepare interest rule
        InterestRuleEntity ruleEntity = new InterestRuleEntity();
        ruleEntity.setDate(LocalDate.of(2023, 1, 1));
        ruleEntity.setRuleId("RULE1");
        ruleEntity.setRate(2.5);

        ruleEntities = List.of(ruleEntity);

        // Map rule entity to DTO
        when(modelMapper.map(any(InterestRuleEntity.class), eq(InterestRuleDTO.class)))
                .thenAnswer(invocation -> {
                    InterestRuleEntity entity = invocation.getArgument(0);
                    InterestRuleDTO dto = new InterestRuleDTO();
                    dto.setDate(entity.getDate());
                    dto.setRuleId(entity.getRuleId());
                    dto.setRate(entity.getRate());
                    return dto;
                });
    }

    @Test
    void testApplyInterestToAllAccounts() {
        when(transactionRepositoryDao.findDistinctAccountIds()).thenReturn(List.of("AC001"));
        when(transactionRepositoryDao.findByAccountAndDateBetween(any(), any(), any())).thenReturn(monthlyTxns);
        when(transactionRepositoryDao.findByAccountAndDateBefore(any(), any())).thenReturn(previousTxns);
        when(interestRuleRepository.findAll()).thenReturn(ruleEntities);

        interestCalculationService.applyInterestToAllAccounts(testMonth);

        verify(transactionService, times(1)).createInterestTransaction(eq("AC001"), any(), eq(endDate));
    }

    @Test
    void testCalculateMonthlyInterest() {
        when(transactionRepositoryDao.findByAccountAndDateBetween(any(), any(), any())).thenReturn(monthlyTxns);
        when(transactionRepositoryDao.findByAccountAndDateBefore(any(), any())).thenReturn(previousTxns);
        when(interestRuleRepository.findAll()).thenReturn(ruleEntities);

        BigDecimal interest = interestCalculationService.calculateMonthlyInterest("AC001", testMonth);

        assertNotNull(interest);
        assertTrue(interest.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateMonthlyInterest_NoInterestAppliedIfZeroBalance() {
        when(transactionRepositoryDao.findByAccountAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(transactionRepositoryDao.findByAccountAndDateBefore(any(), any())).thenReturn(new ArrayList<>());
        when(interestRuleRepository.findAll()).thenReturn(ruleEntities);

        BigDecimal interest = interestCalculationService.calculateMonthlyInterest("AC002", testMonth);

        assertEquals(BigDecimal.ZERO.setScale(2), interest);
    }

}
