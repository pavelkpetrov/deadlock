package com.softteco.optimisticlock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LockTester {
    private boolean doExit = false;
    private SynchronizedString syncString;

    public LockTester(SynchronizedString syncString){
        this.syncString = syncString;
    }

    public void startTest() {
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!doExit) {
                    log.info("Current value is:" + syncString.getString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                log.info("Exit thread:" + Thread.currentThread().getId());
            }
        });
        Thread modifyThread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                modifyString(syncString);
            }
        });
        Thread modifyThread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                modifyString(syncString);
            }
        });

        monitorThread.start();
        modifyThread1.start();
        modifyThread2.start();
        int counter = 0;
        while (counter <= 100){
            counter++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        doExit = true;
    }

    private void modifyString(SynchronizedString syncString){
        while (!doExit) {
            syncString.modifyString();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Exit thread:" + Thread.currentThread().getId());
    }


}
