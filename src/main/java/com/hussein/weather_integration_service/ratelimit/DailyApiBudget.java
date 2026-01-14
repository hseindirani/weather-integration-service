package com.hussein.weather_integration_service.ratelimit;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DailyApiBudget {

    private static final int MAX_CALLS_PER_DAY = 10_000;

    private LocalDate currentDayUtc = LocalDate.now(ZoneOffset.UTC);
    private final AtomicInteger callsToday = new AtomicInteger(0);

    public synchronized boolean tryConsume() {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        // Reset if day changed
        if (!todayUtc.equals(currentDayUtc)) {
            currentDayUtc = todayUtc;
            callsToday.set(0);
        }

        // If budget exhausted, do not allow call
        if (callsToday.get() >= MAX_CALLS_PER_DAY) {
            return false;
        }

        // Consume one call from the budget
        callsToday.incrementAndGet();
        return true;
    }

    public synchronized int getCallsToday() {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        if (!todayUtc.equals(currentDayUtc)) {
            currentDayUtc = todayUtc;
            callsToday.set(0);
        }
        return callsToday.get();
    }
}
