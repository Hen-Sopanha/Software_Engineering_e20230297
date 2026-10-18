package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLongArray;

public class GracefulBank {

    public static void main(String[] args) throws InterruptedException {
        final int THREADS = 8;

        TryLockBank.Account[] accounts = new TryLockBank.Account[4];
        for (int i = 0; i < accounts.length; i++) accounts[i] = new TryLockBank.Account(i, 1_000);

        AtomicLongArray done = new AtomicLongArray(THREADS);
        Thread[] workers = new Thread[THREADS];

        for (int w = 0; w < THREADS; w++) {
            final int id = w;
            workers[w] = new Thread(() -> {
                var rnd = ThreadLocalRandom.current();
                while (!Thread.currentThread().isInterrupted()) {
                    int a = rnd.nextInt(accounts.length);
                    int b = rnd.nextInt(accounts.length - 1);
                    if (b >= a) b++;
                    try {
                        TryLockBank.transfer(accounts[a], accounts[b], rnd.nextInt(1, 50));
                        done.incrementAndGet(id);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "transfer-" + w);
            workers[w].start();
        }

        Thread.sleep(2000);
        System.out.println("[main] interrupting " + THREADS + " workers after 2000 ms");
        for (Thread t : workers) t.interrupt();

        boolean allStopped = true;
        for (Thread t : workers) {
            t.join(1000);
            if (t.isAlive()) allStopped = false;
        }

        for (int w = 0; w < THREADS; w++)
            System.out.println(workers[w].getName() + " stopped after " + done.get(w) + " transfers");

        long total = 0;
        for (TryLockBank.Account a : accounts) total += a.balance();
        System.out.println("all workers stopped=" + allStopped
                + " total=" + total + " (expected 4000)");
    }
}