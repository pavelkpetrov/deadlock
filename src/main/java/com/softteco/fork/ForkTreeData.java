package com.softteco.fork;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class ForkTreeData<T> {

    private Node root;
    private List<String> ids = new ArrayList<>();
    private Random rnd = new Random();

    private int idMin;
    private int idMax;
    private int childrenCount;

    public void initVectors(int idMin, int idMax, int childrenCount){

        this.idMin = idMin;
        this.idMax = idMax;
        this.childrenCount = childrenCount;

        generateIds();
        List<Node> listChildrensI = new ArrayList<>();
        for (int i = 0; i < childrenCount; i++) {
            List<Node> listChildrensJ = new ArrayList<>();
            for (int j = 0; j < childrenCount; j++) {
                List<Node> listChildrensK = new ArrayList<>();
                for (int k = 0; k < childrenCount; k++) {
                    List<Node> listChildrensL = new ArrayList<>();
                    for (int l = 0; l < childrenCount; l++) {
                        listChildrensL.add(new NodeImpl(l, new ArrayList<>(), getIdFromPool()));
//                        listChildrensL.add(new NodeImpl(0, new ArrayList<>(), getIdFromPool()));
                    }
                    listChildrensK.add(new NodeImpl(k, listChildrensL, getIdFromPool()));
//                    listChildrensK.add(new NodeImpl(0, listChildrensL, getIdFromPool()));
                }
                listChildrensJ.add(new NodeImpl(j, listChildrensK, getIdFromPool()));
//                listChildrensJ.add(new NodeImpl(0, listChildrensK, getIdFromPool()));
            }
            listChildrensI.add(new NodeImpl(i, listChildrensJ, getIdFromPool()));
//            listChildrensI.add(new NodeImpl(1, listChildrensJ, getIdFromPool()));
        }
        root = new NodeImpl(1, listChildrensI, getIdFromPool());
//        root = new NodeImpl(0, listChildrensI, getIdFromPool());
    }

    public void generateIds(){
        for (int i = idMin - 1; i < idMax; i++) {
            String id = UUID.randomUUID().toString();
            log.info("Generated id is: {}", id);
            ids.add(id);
        }
    }

    public String getIdFromPool(){
        int r =  rnd.nextInt((idMax - idMin) + 1) + idMin;
        if (r < (idMin - 1)) {
            r = idMin - 1;
        }
        if (r > (idMax -1)) {
            r = idMax - 1;
        }
        return ids.get(r);
    }

    public Node getRootNode(){
        return root;
    }

    public void countSum(ForkJoinPool pool, RecursiveTask<T> counter, String description){
        LocalDateTime start = LocalDateTime.now();
        log.info("Start sun counter with description {} time {}", description,  new Date());
        pool.invoke(counter);
        try {
            LocalDateTime end = LocalDateTime.now();
            log.info("End sun counter with description {} value {}, time {}", description, counter.get(), end);
            Duration duration = Duration.between(start, end);
            log.info("Counter with description {} time duration {} seconds", description, (duration.toMillis() / 1000));
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
        }
    }

}
