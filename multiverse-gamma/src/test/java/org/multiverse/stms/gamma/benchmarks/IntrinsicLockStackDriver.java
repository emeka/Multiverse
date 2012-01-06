package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class IntrinsicLockStackDriver extends BenchmarkDriver {

    private int pushThreadCount = 1;
    private int popThreadCount = 1;
    private int capacity;
    private PushThread[] pushThreads;
    private PopThread[] popThreads;
    private Stack stack;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Pop threadcount %s\n", pushThreadCount);
        System.out.printf("Multiverse > Push threadcount %s\n", popThreadCount);
        if (capacity == Integer.MAX_VALUE) {
            System.out.printf("Multiverse > Capacity unbound\n");
        } else {
            System.out.printf("Multiverse > Capacity %s\n", capacity);
        }

        stack = new Stack();
        pushThreads = new PushThread[pushThreadCount];
        for (int k = 0; k < pushThreadCount; k++) {
            pushThreads[k] = new PushThread(k);
        }

        popThreads = new PopThread[popThreadCount];
        for (int k = 0; k < popThreadCount; k++) {
            popThreads[k] = new PopThread(k);
        }
    }

    @Override
    public void run(TestCaseResult testCaseResult) {
        startAll(pushThreads);
        startAll(popThreads);
        joinAll(pushThreads);
        joinAll(popThreads);
    }

    @Override
    public void processResults(TestCaseResult result) {
        long pushCount = 0;
        long totalDurationMs = 0;
        for (PushThread t : pushThreads) {
            pushCount += t.count;
            totalDurationMs += t.getDurationMs();
        }

        long popCount = 0;
        for (PopThread t : popThreads) {
            popCount += t.count;
            totalDurationMs += t.getDurationMs();
        }

        int threadCount = pushThreadCount + popThreadCount;
        long count = pushCount + popCount;
        System.out.printf("Multiverse > Total number of transactions %s\n", count);
        double transactionsPerSecond = (count * 1000.0d) / totalDurationMs;
        System.out.printf("Multiverse > Performance %s transactions/second with %s threads\n",
                format(transactionsPerSecond), threadCount);

        result.put("transactionsPerSecond", transactionsPerSecond);
    }

    public class PushThread extends TestThread {
        private long count;

        public PushThread(int id) {
            super("PushThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            while (!shutdown) {
                stack.push("item");
                count++;
            }

            for (int k = 0; k < popThreadCount; k++) {
                stack.push("end");
            }
        }
    }

    public class PopThread extends TestThread {
        private long count;

        public PopThread(int id) {
            super("PopThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            boolean end = false;
            while (!end) {
                end = stack.pop().equals("end");
                count++;
            }
        }
    }

    class Stack {
        Node head;
        int size;

        public synchronized String pop() {
            while (head == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (capacity != Integer.MAX_VALUE) {
                size--;
                notifyAll();
            }

            Node node = head;
            head = node.next;
            return node.value;
        }

        public synchronized void push(String value) {
            if (capacity != Integer.MAX_VALUE) {
                while (size == capacity) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            size++;
            head = new Node(head, value);
            notifyAll();
        }
    }

    static class Node {
        final Node next;
        final String value;

        Node(Node next, String value) {
            this.next = next;
            this.value = value;
        }
    }
}
