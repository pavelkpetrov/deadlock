package com.softteco.blockingqueue;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.BlockingQueue;

// Consumer
@Slf4j
public class IndexerConsumer implements Runnable {

    private final BlockingQueue<File> fileQueue;
    private final File POISON;

    @Override
    public void run() {

        try {
            while (true) {
                File take = fileQueue.take();
                if (take == POISON) {
                    log.info(Thread.currentThread().getName() + " die");
                    break;
                }
                indexFile(take);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    public void indexFile(File file) {
        if (file.isFile()) {
            log.info(Thread.currentThread().getName()
                    + " [IndexerConsumer] - Indexing..." + file.getAbsoluteFile());
        }

    }

    public IndexerConsumer(BlockingQueue<File> fileQueue, File POISON) {
        this.fileQueue = fileQueue;
        this.POISON = POISON;
    }
}