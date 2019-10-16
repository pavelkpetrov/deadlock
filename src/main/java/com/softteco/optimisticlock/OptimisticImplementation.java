package com.softteco.optimisticlock;

import java.util.concurrent.atomic.AtomicReference;

public class OptimisticImplementation implements SynchronizedString {

    private String sharedValue = "initial value";

    private AtomicReference<String> shared = new AtomicReference<>();

    public OptimisticImplementation(){
        shared.set(sharedValue);
    }

    public void modifyString(){
        Long threadId = Thread.currentThread().getId();
        boolean success=false;
        while(!success){
            String prevValue=shared.get();
            // do all the work you need to
            String newValue="Changed by thread:" + threadId;
            // Compare and set
            success=shared.compareAndSet(prevValue,newValue);
        }
    }

    public String getString(){
        return shared.get();
    }

}
