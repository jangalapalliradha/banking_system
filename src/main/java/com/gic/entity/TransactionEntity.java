package com.gic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Transaction_Details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="txn_id")
    private String txnId;
    @Column(name="account")
    private String account;
    @Column(name="type")
    private String type;
    @Column(name="amount")
    private BigDecimal amount;
    @Column(name="date")
    private LocalDate date;


}
