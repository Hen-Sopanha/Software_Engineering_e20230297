package edu.itc.se.lab01.task4;

import java.util.concurrent.Callable;

import static edu.itc.se.lab01.task4.Main.countPrimes;

public class PrimeCounter implements Callable<Long> {
    private final long from, to;

    PrimeCounter(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Long call() {
        return countPrimes(from, to);
    }
}
