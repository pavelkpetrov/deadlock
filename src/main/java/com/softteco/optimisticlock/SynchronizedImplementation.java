package com.softteco.optimisticlock;

public class SynchronizedImplementation implements SynchronizedString {

    private String sharedValue = "initial value";
    private Object mutex = new Object();

    public void modifyString(){
        synchronized(mutex){
            sharedValue="Changed by thread:" + Thread.currentThread().getId();
        }
    }

    public String getString(){
        synchronized(mutex){
            return sharedValue;
        }
    }

}
