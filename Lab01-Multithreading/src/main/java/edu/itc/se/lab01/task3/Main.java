package edu.itc.se.lab01.task3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final int PRODUCERS = 2;
    static final int CONSUMERS = 3;
    static final int ORDERS_PER_PRODUCER = 50;
    static final int CAPACITY = 5;

    public static void main(String[] args) throws InterruptedException {
        OrderQueue queue = new BoundedBuffer(CAPACITY);
        AtomicInteger processed = new AtomicInteger();

        List<Thread> producers = new ArrayList<>();
        List<Thread> consumers = new ArrayList<>();

        for (int p = 0; p < PRODUCERS; p++) {
            int startId = p * ORDERS_PER_PRODUCER + 1;      // 1..50, 51..100
            producers.add(new Thread(
                    new Producer(queue, startId, ORDERS_PER_PRODUCER, CONSUMERS),
                    "producer-" + (p + 1)));
        }

        for (int c = 0; c < CONSUMERS; c++) {
            consumers.add(new Thread(
                    new Consumer(queue, processed),
                    "consumer-" + (c + 1)));
        }

        long start = System.nanoTime();

        consumers.forEach(Thread::start);
        producers.forEach(Thread::start);

        for (Thread t : producers) t.join();
        for (Thread t : consumers) t.join();

        long ms = (System.nanoTime() - start) / 1_000_000;
        int expected = PRODUCERS * ORDERS_PER_PRODUCER;

        System.out.println("Total processed: " + processed.get()
                + " (expected " + expected + ") in " + ms + " ms");
        assert processed.get() == expected : "lost or duplicated orders";
    }
}