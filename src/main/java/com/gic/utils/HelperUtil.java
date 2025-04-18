package com.gic.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public  class HelperUtil {

    private final Map<String, AtomicInteger> sequenceMap = new ConcurrentHashMap<>();

    public String generateTransactionId(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = sequenceMap.computeIfAbsent(datePart, k -> new AtomicInteger(0)).incrementAndGet();
        return String.format("%s-%02d", datePart, seq);
    }
}
