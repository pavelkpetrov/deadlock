package com.softteco.deadlock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

public class DeadLockMonitorMain {

    public static void main(String[] args) {
	// write your code here

        test1();
        detectDeadlock();

    }

    private static void test1() {
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        Thread thread1 = new Thread(new Runnable() {
            @Override public void run() {
                synchronized (lock1) {
                    System.out.println("Thread "+ Thread.currentThread().getId() +" acquired lock1");
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    synchronized (lock2) {
                        System.out.println("Thread "+ Thread.currentThread().getId() +" acquired lock2");
                    }
                }
            }

        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override public void run() {
                synchronized (lock2) {
                    System.out.println("Thread "+ Thread.currentThread().getId() +" acquired lock2");
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    synchronized (lock1) {
                        System.out.println("Thread "+ Thread.currentThread().getId() +" acquired lock1");
                    }
                }
            }
        });
        thread2.start();

        // Wait a little for threads to deadlock.
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException ignore) {}
    }

    private static void detectDeadlock() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadBean.findMonitorDeadlockedThreads();
        int deadlockedThreads = threadIds != null? threadIds.length : 0;
        System.out.println("Number of deadlocked threads: " + deadlockedThreads);
        for (long threadId : threadIds) {
            System.out.println("Deadlocked thread id : " + threadId);
        }
    }

}
