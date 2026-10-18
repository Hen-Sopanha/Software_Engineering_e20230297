package edu.itc.se.lab01.task1;

import static java.lang.Thread.sleep;

public class Main {
    static class Countdown extends Thread {
        Countdown(String name) {
            super(name);
        }

        @Override
        public void run() {
            String name = getName();
            try {
                for (int i = 3; i >= 1; i--) {
                    System.out.println(name + ": " + i);
                    sleep(200);
                }
                System.out.println(name + ": liftoff!");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class CountdownTask implements Runnable{

        @Override
        public void run() {
            try {
                for (int i = 3; i >= 1; i--) {
                    System.out.println(Thread.currentThread().getName() + ": " + i);
                    sleep(200);
                }
                System.out.println(Thread.currentThread().getName() + ": liftoff!");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

//        Explain start() vs run():
//        - start() creates a new thread that then calls run(), so the four countdowns run concurrently and interleave.
//          “all launched” prints immediately, and join() makes main wait until all threads finish (final state TERMINATED).

//        - run() is an ordinary method call on main, so no new thread is created. With --sequential, each countdown finishes before
//          the next starts, “all launched” prints only after all four are done, thread names print as main (except apollo,
//          which uses its own getName()), and the final state is NEW because the threads were never started.

        boolean sequential = args.length > 0 && args[0].equals("--sequential");

        Thread apollo  = new Countdown("apollo");
        Thread gemini  = new Thread(new CountdownTask(), "gemini");
        Thread mercury = new Thread(() -> { new CountdownTask().run(); }, "mercury");
        Thread artemis = Thread.ofVirtual().name("artemis").unstarted(new CountdownTask());

        // TODO: start (or run) all four depending on 'sequential'
        for (Thread t : new Thread[] {apollo, gemini, mercury, artemis}) {
//            if (sequential) t.run();
//            else t.start();
            t.start();
            if (sequential) t.join();
        }
        System.out.println("Mission control: all launched");

        // TODO: join all four
        for (Thread t : new Thread[] {apollo, gemini, mercury, artemis}) {
            t.join();
        }
        System.out.println("Mission control: all landed");

        // TODO: print name, isVirtual(), getState() of each
        for (Thread t : new Thread[]{apollo, gemini, mercury, artemis}) {
            System.out.printf("%-8s virtual=%-5b state=%s%n",
                    t.getName(), t.isVirtual(), t.getState());
        }
    }
}