package edu.itc.se.lab01.task4;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    static long countPrimes(long from, long to) {
        long count = 0;
        for (long n = Math.max(from, 2); n < to; n++) {
            boolean prime = true;
            for (long d = 2; d * d <= n; d++) if (n % d == 0) { prime = false; break; }
            if (prime) count++;
        }
        return count;
    }

    static void runDownloads(String label, ExecutorService executor) throws Exception {
        long t0 = System.nanoTime();

        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            boolean fail = (i + 1) % 50 == 0;
            long delay = (i == 123) ? 5000 : 200;
            futures.add(executor.submit(new FakeDownload("http://example.com/file" + i, fail, delay)));
        }

        int ok = 0, failed = 0, timedOut = 0;
        for (Future<String> f : futures) {
            try {
                f.get(1, TimeUnit.SECONDS);
                ok++;
            } catch (ExecutionException e) {
                failed++;
            } catch (TimeoutException e) {
                f.cancel(true);
                timedOut++;
            }
        }

        System.out.printf("%-40s: ok=%d failed=%d timedOut=%d in %d ms%n",
                label, ok, failed, timedOut, (System.nanoTime() - t0) / 1_000_000);
    }

    public static void main(String[] args) throws Exception {
        final long LIMIT = 4_000_000, CHUNKS = 16;

        long t0 = System.nanoTime();
        long single = countPrimes(0, LIMIT);
        System.out.printf("single-threaded: %d primes in %d ms%n", single, (System.nanoTime() - t0) / 1_000_000);

        t0 = System.nanoTime();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            List<Future<Long>> futures = new ArrayList<>();
            // TODO: submit CHUNKS PrimeCounter callables covering [0, LIMIT)
            long chunkSize = LIMIT / CHUNKS;
            for (int i = 0; i < CHUNKS; i++) {
                long from = i * chunkSize;
                long to = (i == CHUNKS - 1) ? LIMIT : from + chunkSize;
                futures.add(pool.submit(new PrimeCounter(from, to)));
            }

            long total = 0;
            // TODO: sum futures
            for (Future<Long> f : futures) {
                total += f.get();
            }
            System.out.printf("pool: %d primes in %d ms%n", total, (System.nanoTime() - t0) / 1_000_000);
        }

        // TODO: I/O-bound part with 500 FakeDownload tasks on platform pool vs virtual threads
        try (ExecutorService platform = Executors.newFixedThreadPool(20)) {
            runDownloads("downloads on fixed(20) platform threads", platform);
        }

        try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
            runDownloads("downloads on virtual threads", virtual);
        }

        // TODO: failures every 50th task, one 5-second task with a 1-second timeout
    }
}
