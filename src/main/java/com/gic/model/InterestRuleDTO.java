package com.gic.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InterestRuleDTO {
    private LocalDate date;

    private String ruleId;

    private double rate;

}
