package com.gic.service.interestRule.impl;

import com.gic.dao.InterestRuleRepository;
import com.gic.entity.InterestRuleEntity;
import com.gic.model.InterestRuleDTO;
import com.gic.service.interestRule.InterestRuleService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InterestRuleServiceImpl implements InterestRuleService {

    private final InterestRuleRepository repository;

    private final ModelMapper modelMapper;

    public InterestRuleServiceImpl(InterestRuleRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    /**
     * @param date date
     * @param ruleId ruleId
     * @param rate interestRate
     */
    @Override
    public void saveOrUpdateRule(LocalDate date, String ruleId, double rate) {
        InterestRuleEntity rule = repository.findByDate(date)
                .map(existing -> {
                    existing.setRuleId(ruleId);
                    existing.setRate(rate);
                    return existing;
                })
                .orElse(InterestRuleEntity.builder().ruleId(ruleId)
                        .date(date).rate(rate).build());

        repository.save(rule);

    }
    @Override
    public List<InterestRuleDTO> getAllRulesSortedByDate() {
        List<InterestRuleEntity> interestRuleEntities =  repository.findAll().stream()
                .sorted(Comparator.comparing(InterestRuleEntity::getDate))
                .toList();
        List<InterestRuleDTO> interestRuleDTOS = new ArrayList<>();
        interestRuleEntities.forEach(rule -> interestRuleDTOS.add(modelMapper.map(rule, InterestRuleDTO.class)));
         return interestRuleDTOS;
    }
}
