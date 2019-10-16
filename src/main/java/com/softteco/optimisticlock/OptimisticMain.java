package com.softteco.optimisticlock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OptimisticMain {

    public static void main(String[] args) {
        LockTester tester = new LockTester(        new OptimisticImplementation());
        tester.startTest();
    }

}
