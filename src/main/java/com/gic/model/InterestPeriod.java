package com.gic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestPeriod {

    LocalDate start;
    LocalDate end;
    double balance;
    InterestRuleDTO rule;
}
