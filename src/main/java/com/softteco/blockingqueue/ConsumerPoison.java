package com.softteco.blockingqueue;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class ConsumerPoison implements Runnable {

    private final BlockingQueue<Integer> queue;
    private final Integer POISON;

    @Override
    public void run() {

        try {
            while (true) {
                Integer take = queue.take();
                process(take);

                // if this is a poison pill, break, exit
                if (take == POISON) {
                    log.info("[Consumer] Take poison pill will try to stop thread execution");
                    break;
                }

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    private void process(Integer take) throws InterruptedException {
        log.info("[Consumer] Take : " + take);
        Thread.sleep(500);
    }

    public ConsumerPoison(BlockingQueue<Integer> queue, Integer POISON) {
        this.queue = queue;
        this.POISON = POISON;
    }
}