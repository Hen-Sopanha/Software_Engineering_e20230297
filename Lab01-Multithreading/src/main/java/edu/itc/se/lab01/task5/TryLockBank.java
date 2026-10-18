package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TryLockBank {
    static final class Account {
        final int id;
        final ReentrantLock lock = new ReentrantLock();
        private long balance;
        Account(int id, long balance) { this.id = id; this.balance = balance; }
        long balance() { return balance; }
        void withdraw(long a) { balance -= a; }
        void deposit(long a)  { balance += a; }
    }

    static final AtomicLong retries = new AtomicLong();

    static void transfer(Account from, Account to, long amount) throws InterruptedException {
        while (true) {
            if (from.lock.tryLock(10, MILLISECONDS)) {
                try {
                    if (to.lock.tryLock(10, MILLISECONDS)) {
                        try {
                            if (from.balance() >= amount) {
                                from.withdraw(amount);
                                to.deposit(amount);
                            }
                            return;
                        } finally {
                            to.lock.unlock();
                        }
                    }
                } finally {
                    from.lock.unlock();
                }
            }
            retries.incrementAndGet();
            Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int THREADS = 8, PER_THREAD = 10_000;

        Account[] accounts = new Account[4];
        for (int i = 0; i < accounts.length; i++) accounts[i] = new Account(i, 1_000);

        long[] done = new long[THREADS];
        Thread[] workers = new Thread[THREADS];

        long t0 = System.nanoTime();
        for (int w = 0; w < THREADS; w++) {
            final int id = w;
            workers[w] = new Thread(() -> {
                var rnd = ThreadLocalRandom.current();
                try {
                    for (int i = 0; i < PER_THREAD; i++) {
                        int a = rnd.nextInt(accounts.length);
                        int b = rnd.nextInt(accounts.length - 1);
                        if (b >= a) b++;
                        transfer(accounts[a], accounts[b], rnd.nextInt(1, 50));
                        done[id]++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "transfer-" + w);
            workers[w].start();
        }
        for (Thread t : workers) t.join();
        long ms = (System.nanoTime() - t0) / 1_000_000;

        long transfers = 0; for (long d : done) transfers += d;
        long total = 0;     for (Account a : accounts) total += a.balance();
        System.out.println("transfers=" + transfers + " retries=" + retries.get()
                + " total=" + total + " (expected 4000) in " + ms + " ms");
    }
}