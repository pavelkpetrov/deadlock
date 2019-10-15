package com.softteco.blockingqueue;

import com.softteco.readwritelock.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileIndexingMain {

    private static final File POISON = new File("This is a POISON PILL");

    public static void main(String[] args) throws IOException {

        int N_PRODUCERS = 1;
        int N_CONSUMERS = 2;//Runtime.getRuntime().availableProcessors();
        int N_POISON_PILL_PER_PRODUCER = N_CONSUMERS / N_PRODUCERS;
        int N_POISON_PILL_REMAIN = N_CONSUMERS % N_PRODUCERS;

        System.out.println("N_PRODUCERS : " + N_PRODUCERS);
        System.out.println("N_CONSUMERS : " + N_CONSUMERS);
        System.out.println("N_POISON_PILL_PER_PRODUCER : " + N_POISON_PILL_PER_PRODUCER);
        System.out.println("N_POISON_PILL_REMAIN : " + N_POISON_PILL_REMAIN);

        //unbound queue, no limit
        BlockingQueue<File> queue = new LinkedBlockingQueue<>();

        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return true;
            }
        };

        String indexingPath = FileUtils.loadProperties().getProperty("file.indexing.path");

        File root = new File(indexingPath);

        for (int i = 0; i < N_PRODUCERS - 1; i++) {
            new Thread(new FileCrawlerProducer(queue, filter, root,
                    POISON, N_POISON_PILL_PER_PRODUCER)).start();
        }
        new Thread(new FileCrawlerProducer(queue, filter, root, POISON,
                N_POISON_PILL_PER_PRODUCER + N_POISON_PILL_REMAIN)).start();

        for (int i = 0; i < N_CONSUMERS; i++) {
            new Thread(new IndexerConsumer(queue, POISON)).start();
        }

    }
}
