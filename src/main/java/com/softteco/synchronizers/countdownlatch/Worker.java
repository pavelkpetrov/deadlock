package com.softteco.synchronizers.countdownlatch;

import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class Worker implements Runnable {

    private final List<String> outputScraper;

    private final CountDownLatch countDownLatch;



    Worker(final List<String> outputScraper, final CountDownLatch countDownLatch) {

        this.outputScraper = outputScraper;

        this.countDownLatch = countDownLatch;

    }



    @Override

    public void run() {

        // Do some work

        log.info("Doing some logic");

        outputScraper.add("Counted down");

        countDownLatch.countDown();

    }


    public static void main(String[] args)
            throws InterruptedException {

        List<String> outputScraper = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(5);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new Worker(outputScraper, countDownLatch)))
                .limit(5)
                .collect(toList());

        workers.forEach(Thread::start);
        countDownLatch.await();
        log.info("Reach all jobs are finished");
        outputScraper.add("Latch released");

        Assert.assertTrue(outputScraper.contains("Counted down") && outputScraper.contains("Latch released"));

    }
}