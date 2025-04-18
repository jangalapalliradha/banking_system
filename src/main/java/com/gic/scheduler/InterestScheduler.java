package com.gic.scheduler;

import com.gic.service.interestRule.InterestCalculationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

/**
 * This will run automatically on last day of month @2 AM to apply interest rates
 */
@Service
public class InterestScheduler {

    private final InterestCalculationService interestCalculationService;

    public InterestScheduler(InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;
    }

    // Run at 2 AM on the last day of each month
    @Scheduled(cron = "0 0 2 L * ?") // L = last day of month
    public void runMonthlyInterestCalculation() {
        YearMonth currentMonth = YearMonth.now();
        interestCalculationService.applyInterestToAllAccounts(currentMonth);
    }
}
