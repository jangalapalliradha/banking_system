package com.gic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailsDTO {

    private String txnId;
    private String account;
    private String type;
    private BigDecimal amount;
    private LocalDate date;

    public TransactionDetailsDTO(String txnId, LocalDate date, String type, BigDecimal amount) {
        this.txnId = txnId;
        this.date = date;
        this.type = type;
        this.amount = amount;
    }
}
