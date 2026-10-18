package edu.itc.se.lab01.task2;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter implements Counter {
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public void increment() {
        count.incrementAndGet();
    }

    @Override
    public long get() {
        return count.get();
    }
}
