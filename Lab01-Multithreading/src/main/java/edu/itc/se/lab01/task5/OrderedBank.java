package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;

public class OrderedBank {
    static void transfer(Bank.Account from, Bank.Account to, long amount) {
        Bank.Account first  = from.id < to.id ? from : to;
        Bank.Account second = (first == from) ? to : from;
        synchronized (first) {
            synchronized (second) {
                if (from.balance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int THREADS = 8, PER_THREAD = 10_000;

        Bank.Account[] accounts = new Bank.Account[4];
        for (int i = 0; i < accounts.length; i++) accounts[i] = new Bank.Account(i, 1_000);

        long[] done = new long[THREADS];
        Thread[] workers = new Thread[THREADS];

        long t0 = System.nanoTime();
        for (int w = 0; w < THREADS; w++) {
            final int id = w;
            workers[w] = new Thread(() -> {
                var rnd = ThreadLocalRandom.current();
                for (int i = 0; i < PER_THREAD; i++) {
                    int a = rnd.nextInt(accounts.length);
                    int b = rnd.nextInt(accounts.length - 1);
                    if (b >= a) b++;
                    transfer(accounts[a], accounts[b], rnd.nextInt(1, 50));
                    done[id]++;
                }
            }, "transfer-" + w);
            workers[w].start();
        }
        for (Thread t : workers) t.join();
        long ms = (System.nanoTime() - t0) / 1_000_000;

        long transfers = 0; for (long d : done) transfers += d;
        long total = 0;     for (Bank.Account a : accounts) total += a.balance();
        System.out.println("transfers=" + transfers + " total=" + total
                + " (expected 4000) in " + ms + " ms");
    }
}