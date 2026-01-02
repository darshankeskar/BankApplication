package org.example.server.ClientBankB.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe transaction ID generator for Bank A.
 *
 * Format: TRX-YYYYMMDD-000001
 *
 * Uses
 * - AtomicLong for sequence.
 *
 */

@Component
public class TransactionIdGenerator {

    private final AtomicLong sequence = new AtomicLong(1);
    private volatile LocalDate currentDate = LocalDate.now();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.BASIC_ISO_DATE;

    public synchronized String nextId() {
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDate)) {
            currentDate = today;
            sequence.set(1);
        }
        long seq = sequence.getAndIncrement();
        return String.format("TRX-%s-%06d", currentDate.format(dateFmt), seq);
    }
}
