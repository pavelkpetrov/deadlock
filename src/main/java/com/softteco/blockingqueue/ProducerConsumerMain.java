package com.softteco.blockingqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumerMain {

    public static void main(String[] args) {

        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(10);

        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();

    }
}
