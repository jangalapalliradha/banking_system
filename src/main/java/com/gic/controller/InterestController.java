package com.gic.controller;

import com.gic.service.interestRule.InterestCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/interest")
public class InterestController {

    private final InterestCalculationService interestCalculationService;

    public InterestController(InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;
    }

    /**
     * It is used to call manually to apply interest rates and check
     * @param year year
     * @param month month
     * @return success msg
     */
    @PostMapping("/apply/{year}/{month}")
    public ResponseEntity<String> applyInterest(@PathVariable int year, @PathVariable int month) {
        YearMonth ym = YearMonth.of(year, month);
        interestCalculationService.applyInterestToAllAccounts(ym);
        return ResponseEntity.ok("Interest applied for " + ym);
    }
}
