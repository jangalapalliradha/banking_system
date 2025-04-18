package com.gic.service.interestRule;

import com.gic.model.InterestRuleDTO;

import java.time.LocalDate;
import java.util.List;

public interface InterestRuleService {
     void saveOrUpdateRule(LocalDate date, String ruleId, double rate);
     List<InterestRuleDTO> getAllRulesSortedByDate();
}
