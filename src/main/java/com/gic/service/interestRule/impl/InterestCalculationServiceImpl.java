package com.gic.service.interestRule.impl;

import com.gic.dao.InterestRuleRepository;
import com.gic.dao.TransactionRepositoryDao;
import com.gic.entity.InterestRuleEntity;
import com.gic.entity.TransactionEntity;
import com.gic.model.InterestPeriod;
import com.gic.model.InterestRuleDTO;
import com.gic.service.interestRule.InterestCalculationService;
import com.gic.service.transaction.TransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterestCalculationServiceImpl implements InterestCalculationService {
    private final TransactionRepositoryDao transactionRepositoryDao;
    private final InterestRuleRepository interestRuleRepository;
    private final TransactionService transactionService;

    private final ModelMapper modelMapper;

    public InterestCalculationServiceImpl(TransactionRepositoryDao transactionRepositoryDao, InterestRuleRepository interestRuleRepository, TransactionService transactionService, ModelMapper modelMapper) {
        this.transactionRepositoryDao = transactionRepositoryDao;
        this.interestRuleRepository = interestRuleRepository;
        this.transactionService = transactionService;
        this.modelMapper = modelMapper;
    }

    /**
     * @param month month
     */
    @Override
    public void applyInterestToAllAccounts(YearMonth month) {
        List<String> accountIds = transactionRepositoryDao.findDistinctAccountIds();

        for (String accountId : accountIds) {
            BigDecimal interest = calculateMonthlyInterest(accountId, month);
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                LocalDate creditDate = month.atEndOfMonth();
                transactionService.createInterestTransaction(accountId, interest, creditDate);
            }
        }
    }

    public BigDecimal calculateMonthlyInterest(String accountId, YearMonth month) {
        List<TransactionEntity> monthlyTxns = transactionRepositoryDao.findByAccountAndDateBetween(
                accountId,
                month.atDay(1),
                month.atEndOfMonth()
        );

        List<TransactionEntity> previousTxns = transactionRepositoryDao.findByAccountAndDateBefore(accountId, month.atDay(1));

        List<InterestRuleEntity> rules = interestRuleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(InterestRuleEntity::getDate))
                .collect(Collectors.toList());

        List<InterestPeriod> periods = buildInterestPeriods(month, previousTxns, monthlyTxns, rules);

        BigDecimal totalInterest = BigDecimal.ZERO;

        for (InterestPeriod period : periods) {
            long days = ChronoUnit.DAYS.between(period.getStart(), period.getEnd().plusDays(1));

            BigDecimal interest = BigDecimal.valueOf(period.getBalance())
                    .multiply(BigDecimal.valueOf(period.getRule().getRate()))
                    .multiply(BigDecimal.valueOf(days))
                    .divide(BigDecimal.valueOf(100 * 365), 10, RoundingMode.HALF_UP);

            totalInterest = totalInterest.add(interest);
        }

        return totalInterest.setScale(2, RoundingMode.HALF_UP);
    }

    private List<InterestPeriod> buildInterestPeriods(
                                                      YearMonth month,
                                                      List<TransactionEntity> previousTxns,
                                                      List<TransactionEntity> monthlyTxns,
                                                      List<InterestRuleEntity> rules) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<InterestRuleDTO> rulesDTO = rules.stream().map(rule -> modelMapper.map(rule, InterestRuleDTO.class)).collect(Collectors.toList());

        // Calculate opening balance before the month
        double balance = previousTxns.stream()
                .map(TransactionEntity::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        List<TransactionEntity> effectiveTxns = new ArrayList<>();
        effectiveTxns.addAll(monthlyTxns);
        effectiveTxns.sort(Comparator.comparing(TransactionEntity::getDate));

        List<InterestPeriod> periods = new ArrayList<>();
        LocalDate currentStart = start;

        for (int i = 1; i < effectiveTxns.size(); i++) {
            LocalDate periodEnd = effectiveTxns.get(i).getDate().minusDays(1);
            InterestRuleDTO rule = findLatestRule(rulesDTO, currentStart);
            periods.add(new InterestPeriod(currentStart, periodEnd, balance, rule));

            balance += effectiveTxns.get(i).getAmount().doubleValue();
            currentStart = effectiveTxns.get(i).getDate();
        }

        // Final period till end of month
        InterestRuleDTO lastRule = findLatestRule(rulesDTO, currentStart);
        periods.add(new InterestPeriod(currentStart, end, balance, lastRule));

        return periods;
    }

    private InterestRuleDTO findLatestRule(List<InterestRuleDTO> rules, LocalDate date) {
        return rules.stream()
                .filter(rule -> !rule.getDate().isAfter(date))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new RuntimeException("No interest rule found for " + date));
    }
}
