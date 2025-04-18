package com.gic.service.transaction.impl;

import com.gic.constants.TransactionType;
import com.gic.dao.TransactionRepositoryDao;
import com.gic.entity.TransactionEntity;
import com.gic.model.TransactionDetailsDTO;
import com.gic.service.transaction.TransactionService;
import com.gic.utils.HelperUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all actions related to transactions
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepositoryDao transactionRepositoryDao;

    private final ModelMapper modelMapper;

    private final HelperUtil helperUtil;

    public TransactionServiceImpl(TransactionRepositoryDao transactionRepositoryDao, ModelMapper modelMapper, HelperUtil helperUtil) {
        this.transactionRepositoryDao = transactionRepositoryDao;
        this.modelMapper = modelMapper;
        this.helperUtil = helperUtil;
    }

    /**
     * to add transaction to DB
     * @param transactionDetails input
     */
    @Override
    public void addTransaction(TransactionDetailsDTO transactionDetails) {
        TransactionEntity transactionEntity =  TransactionEntity.builder()
                .type(transactionDetails.getType())
                .txnId(transactionDetails.getTxnId())
                .date(transactionDetails.getDate())
                .amount(transactionDetails.getAmount())
                .account(transactionDetails.getAccount()).build();
        transactionRepositoryDao.save(transactionEntity);
    }

    /**
     * to load all transactions
     */
    @Override
    public List<TransactionDetailsDTO> getAllTransactions() {
        List<TransactionEntity> transactionEntities = transactionRepositoryDao.findAll();
        return getTransactionDetailsDTOS(transactionEntities);
    }

    private List<TransactionDetailsDTO> getTransactionDetailsDTOS(List<TransactionEntity> transactionEntities) {
        List<TransactionDetailsDTO> transactionDetailsDTOList = new ArrayList<>();
        transactionEntities.forEach(transactionEntity -> transactionDetailsDTOList.add(modelMapper.map(transactionEntity, TransactionDetailsDTO.class)));
        return transactionDetailsDTOList;
    }

    /**
     * @param account accountNumber
     * @param fromDate fromDate
     * @param toDate endDate
     * @return list
     */
    @Override
    public List<TransactionDetailsDTO> getTransactionsBetweenDates(String account, LocalDate fromDate, LocalDate toDate) {
       List<TransactionEntity> transactionEntities = transactionRepositoryDao.findByAccountAndDateBetween(account, fromDate, toDate);
        return getTransactionDetailsDTOS(transactionEntities);
    }

    /**
     * @param account accountDate
     * @param toDate endDate
     * @return list
     */
    @Override
    public List<TransactionDetailsDTO> getTransactionsDateBefore(String account, LocalDate toDate) {
       List<TransactionEntity> transactionEntities = transactionRepositoryDao.findByAccountAndDateBefore(account, toDate);
        return getTransactionDetailsDTOS(transactionEntities);
    }

    /**
     * @param account accountNbr
     * @return list
     */
    @Override
    public List<TransactionDetailsDTO> getTransactionsByAcct(String account) {
        List<TransactionEntity> transactionEntities = transactionRepositoryDao.findByAccount(account);
        return getTransactionDetailsDTOS(transactionEntities);
    }

    public void createInterestTransaction(String accountId, BigDecimal amount, LocalDate date) {
        TransactionEntity tx = new TransactionEntity();
        tx.setAccount(accountId);
        tx.setAmount(amount);
        tx.setDate(date);
        tx.setType(TransactionType.I.name());
        tx.setTxnId(helperUtil.generateTransactionId(date));
        transactionRepositoryDao.save(tx);
    }
}
