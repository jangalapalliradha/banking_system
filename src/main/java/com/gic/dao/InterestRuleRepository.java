package com.gic.dao;

import com.gic.entity.InterestRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface InterestRuleRepository extends JpaRepository<InterestRuleEntity, Long> {
    Optional<InterestRuleEntity> findByDate(LocalDate date);
}