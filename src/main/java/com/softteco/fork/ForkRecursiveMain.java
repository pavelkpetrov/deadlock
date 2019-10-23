package com.softteco.fork;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;

@Slf4j
public class ForkRecursiveMain {

    final static int ID_MIN = 1;
    final static int ID_MAX = 1000;
    final static int CHILDREN_COUNT = 10;

    public static void main(String[] args) {
        log.info("Before init vectors");
        ForkTreeData fData = new ForkTreeData();
        fData.initVectors(ID_MIN, ID_MAX, CHILDREN_COUNT);
        log.info("After init vectors");
        Node root = fData.getRootNode();
        ForkJoinPool pool = new ForkJoinPool();
        ValueSumCounter counter = new ValueSumCounter(root);
        fData.countSum(pool, counter, "DEFAULT PARALELIZM");
    }

}
