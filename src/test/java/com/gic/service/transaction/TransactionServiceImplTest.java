package com.gic.service.transaction;

import com.gic.dao.TransactionRepositoryDao;
import com.gic.entity.TransactionEntity;
import com.gic.model.TransactionDetailsDTO;
import com.gic.service.transaction.impl.TransactionServiceImpl;
import com.gic.utils.HelperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private TransactionRepositoryDao transactionRepositoryDao;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private HelperUtil helperUtil;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionDetailsDTO sampleDTO;
    private TransactionEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDTO = new TransactionDetailsDTO();
        sampleDTO.setTxnId("TXN001");
        sampleDTO.setAccount("AC001");
        sampleDTO.setAmount(BigDecimal.valueOf(100));
        sampleDTO.setDate(LocalDate.of(2023, 6, 1));
        sampleDTO.setType("D");

        sampleEntity = TransactionEntity.builder()
                .txnId("TXN001")
                .account("AC001")
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.of(2023, 6, 1))
                .type("D")
                .build();
    }

    @Test
    void testAddTransaction() {
        transactionService.addTransaction(sampleDTO);
        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);

        verify(transactionRepositoryDao, times(1)).save(captor.capture());
        TransactionEntity saved = captor.getValue();

        assertEquals("AC001", saved.getAccount());
        assertEquals("TXN001", saved.getTxnId());
        assertEquals(BigDecimal.valueOf(100), saved.getAmount());
        assertEquals("D", saved.getType());
    }

    @Test
    void testGetAllTransactions() {
        List<TransactionEntity> entityList = List.of(sampleEntity);
        when(transactionRepositoryDao.findAll()).thenReturn(entityList);
        when(modelMapper.map(any(TransactionEntity.class), eq(TransactionDetailsDTO.class))).thenReturn(sampleDTO);

        List<TransactionDetailsDTO> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
        assertEquals("AC001", result.get(0).getAccount());
    }

    @Test
    void testCreateInterestTransaction() {
        LocalDate date = LocalDate.of(2023, 6, 30);
        BigDecimal interest = BigDecimal.valueOf(12.34);

        when(helperUtil.generateTransactionId(date)).thenReturn("INT-20230630-001");

        transactionService.createInterestTransaction("AC001", interest, date);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionRepositoryDao).save(captor.capture());

        TransactionEntity saved = captor.getValue();
        assertEquals("AC001", saved.getAccount());
        assertEquals(interest, saved.getAmount());
        assertEquals("I", saved.getType());
        assertEquals(date, saved.getDate());
    }
}
