package com.softteco.synchronizers;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Phaser;

@Slf4j
public class PhaserDemo {

    public static void main(String[] args) {
        Phaser ph = new Phaser(1){
            protected boolean onAdvance(int phase, int parties) {
                log.info("onAdvance phase {}, parties {}", phase, parties);
                return super.onAdvance(phase, parties);
            }
        };
        int curPhase;
        curPhase = ph.getPhase();
        log.info("Phase {} in Main started", curPhase);
        // Threads for first phase
        new FileReaderThread("thread-1", "file-1", ph);
        new FileReaderThread("thread-2", "file-2", ph);
        new FileReaderThread("thread-3", "file-3", ph);
        //For main thread
        ph.arriveAndAwaitAdvance();
        log.info("New phase {} started", ph.getPhase());
        // Threads for second phase
        new QueryThread("thread-1", 40, ph);
        new QueryThread("thread-2", 40, ph);
        curPhase = ph.getPhase();
        ph.arriveAndAwaitAdvance();
        log.info("Phase {} completed", curPhase);
        // deregistering the main thread
        ph.arriveAndDeregister();
    }

    static class FileReaderThread implements Runnable {
        private String threadName;
        private String fileName;
        private Phaser ph;

        FileReaderThread(String threadName, String fileName, Phaser ph){
            this.threadName = threadName;
            this.fileName = fileName;
            this.ph = ph;
            ph.register();
            new Thread(this).start();
        }
        @Override
        public void run() {
            try {
                Thread.sleep(20);
                log.info("Reading file {} thread {}, phase {} parsing and storing to DB ", fileName, threadName, ph.getPhase());
                // Using await and advance so that all thread wait here
                ph.arriveAndAwaitAdvance();
                log.info("Reading file Continue after arriveAndAwaitAdvance {} thread {}, phase {} parsing and storing to DB ", fileName, threadName, ph.getPhase());
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            ph.arriveAndDeregister();
        }
    }

    static class QueryThread implements Runnable {
        private String threadName;
        private int param;
        private Phaser ph;

        QueryThread(String threadName, int param, Phaser ph){
            this.threadName = threadName;
            this.param = param;
            this.ph = ph;
            ph.register();
            new Thread(this).start();
        }

        @Override
        public void run() {
            log.info("Querying DB using param {} Thread {}, phase {}", param, threadName, ph.getPhase());
            ph.arriveAndAwaitAdvance();
            log.info("Querying DB Continue after arriveAndAwaitAdvance using param {} Thread {}, phase {}", param, threadName, ph.getPhase());
            log.info("Threads finished");
            ph.arriveAndDeregister();
        }
    }
}