package edu.itc.se.lab01.task3;

import java.util.concurrent.ThreadLocalRandom;

public class Producer implements Runnable {
    private final OrderQueue queue;
    private final int firstID;
    private final int count;
    private final int consumers;

    public Producer(OrderQueue queue, int firstID, int count, int consumers) {
        this.queue = queue;
        this.firstID = firstID;
        this.count = count;
        this.consumers = consumers;
    }

    @Override
    public void run() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        try {
            for (int i = firstID; i < firstID + count; i++) {
                Order order = new Order(i, "item-" + i, rnd.nextInt(1, 6));
                queue.put(order);
                Thread.sleep(rnd.nextInt(0, 21));
            }
            for (int c = 0; c < consumers; c++) {
                queue.put(Order.poison());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}