package com.gic.service.interestRule;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

import com.gic.dao.InterestRuleRepository;
import com.gic.entity.InterestRuleEntity;
import com.gic.model.InterestRuleDTO;
import com.gic.service.interestRule.impl.InterestRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterestRuleServiceImplTest {

    @Mock
    private InterestRuleRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private InterestRuleServiceImpl interestRuleService;

    private final LocalDate testDate = LocalDate.of(2024, 1, 1);
    private final String ruleId = "RULE01";
    private final double rate = 3.5;

    @Test
    void testSaveOrUpdateRule_InsertNew() {
        when(repository.findByDate(testDate)).thenReturn(Optional.empty());

        interestRuleService.saveOrUpdateRule(testDate, ruleId, rate);

        verify(repository, times(1)).save(argThat(rule ->
                rule.getDate().equals(testDate) &&
                        rule.getRuleId().equals(ruleId) &&
                        rule.getRate() == rate
        ));
    }

    @Test
    void testSaveOrUpdateRule_UpdateExisting() {
        InterestRuleEntity existing = InterestRuleEntity.builder()
                .ruleId("OLD")
                .rate(2.0)
                .date(testDate)
                .build();

        when(repository.findByDate(testDate)).thenReturn(Optional.of(existing));

        interestRuleService.saveOrUpdateRule(testDate, ruleId, rate);

        verify(repository).save(existing);
        assertEquals(ruleId, existing.getRuleId());
        assertEquals(rate, existing.getRate());
    }

    @Test
    void testGetAllRulesSortedByDate() {
        InterestRuleEntity rule1 = InterestRuleEntity.builder()
                .ruleId("R1").rate(2.0).date(LocalDate.of(2023, 1, 1)).build();
        InterestRuleEntity rule2 = InterestRuleEntity.builder()
                .ruleId("R2").rate(3.0).date(LocalDate.of(2024, 1, 1)).build();

        when(repository.findAll()).thenReturn(List.of(rule2, rule1));

        InterestRuleDTO dto1 = new InterestRuleDTO();
        dto1.setRuleId("R1");
        dto1.setRate(2.0);
        dto1.setDate(rule1.getDate());

        InterestRuleDTO dto2 = new InterestRuleDTO();
        dto2.setRuleId("R2");
        dto2.setRate(3.0);
        dto2.setDate(rule2.getDate());

        when(modelMapper.map(eq(rule1), eq(InterestRuleDTO.class))).thenReturn(dto1);
        when(modelMapper.map(eq(rule2), eq(InterestRuleDTO.class))).thenReturn(dto2);

        List<InterestRuleDTO> result = interestRuleService.getAllRulesSortedByDate();

        assertEquals(2, result.size());
        assertEquals("R1", result.get(0).getRuleId());
        assertEquals("R2", result.get(1).getRuleId());
    }
}