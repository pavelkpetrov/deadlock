package com.softteco.optimisticlock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SynchronizedMain {

    public static void main(String[] args) {
        LockTester tester = new LockTester(        new SynchronizedImplementation());
        tester.startTest();
    }

}
