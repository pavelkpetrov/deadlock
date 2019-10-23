package com.softteco.synchronizers;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

import java.util.Collections;

import java.util.List;

import java.util.Random;

import java.util.concurrent.BrokenBarrierException;

import java.util.concurrent.CyclicBarrier;


@Slf4j
public class CyclicBarrierDemo {



    private CyclicBarrier cyclicBarrier;

    private List<List<Integer>> partialResults = Collections.synchronizedList(new ArrayList<>());

    private Random random = new Random();

    private int NUM_PARTIAL_RESULTS;

    private int NUM_WORKERS;



    private void runSimulation(int numWorkers, int numberOfPartialResults) {

        NUM_PARTIAL_RESULTS = numberOfPartialResults;

        NUM_WORKERS = numWorkers;



        cyclicBarrier = new CyclicBarrier(NUM_WORKERS, new AggregatorThread());

        log.info("Spawning " + NUM_WORKERS + " worker threads to compute " + NUM_PARTIAL_RESULTS + " partial results each");

        for (int i = 0; i < NUM_WORKERS; i++) {

            Thread worker = new Thread(new NumberCruncherThread());

            worker.setName("Thread " + i);

            worker.start();

        }

    }



    class NumberCruncherThread implements Runnable {



        @Override

        public void run() {

            String thisThreadName = Thread.currentThread().getName();

            List<Integer> partialResult = new ArrayList<>();

            for (int i = 0; i < NUM_PARTIAL_RESULTS; i++) {

                Integer num = random.nextInt(10);

                log.info(thisThreadName + ": Crunching some numbers! Final result - " + num);

                partialResult.add(num);

            }

            partialResults.add(partialResult);

            try {

                log.info(thisThreadName + " waiting for others to reach barrier.");

                cyclicBarrier.await();

            } catch (InterruptedException | BrokenBarrierException e) {

                e.printStackTrace();

            }

        }

    }



    class AggregatorThread implements Runnable {



        @Override

        public void run() {

            String thisThreadName = Thread.currentThread().getName();

            log.info(thisThreadName + ": Computing final sum of " + NUM_WORKERS + " workers, having " + NUM_PARTIAL_RESULTS + " results each.");

            int sum = 0;

            for (List<Integer> threadResult : partialResults) {

//                log.info("Adding ");
                String bStr = "Adding ";

                for (Integer partialResult : threadResult) {

                    bStr += partialResult + " ";

                    sum += partialResult;

                }

                log.info(bStr);

            }

            log.info(Thread.currentThread().getName() + ": Final result = " + sum);

        }



    }


    public static void main(String[] args) {

        CyclicBarrierDemo play = new CyclicBarrierDemo();

        play.runSimulation(5, 3);

    }


}