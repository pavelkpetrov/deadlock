package com.softteco.fork;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class ForkRecursiveGroupIdMain {

    final static int ID_MIN = 1;
    final static int ID_MAX = 5;
    final static int CHILDREN_COUNT = 10;

    public static void main(String[] args) {
        log.info("Before init vectors");
        ForkTreeData fData = new ForkTreeData<Map<String, List<Long>>>(){
            public void countSum(ForkJoinPool pool, RecursiveTask<Map<String, List<Long>>> counter, String description){
                LocalDateTime start = LocalDateTime.now();
                log.info("Start sun mapper with description {} time {}", description,  new Date());
                pool.invoke(counter);
                try {
                    LocalDateTime end = LocalDateTime.now();
                    log.info("End mapper with description {} time {}", description, counter.get(), end);
                    log.info("Mapper with description {} value:", description);
                    Map<String, List<Long>> mappedData = counter.get();
                    for (String key : mappedData.keySet()) {
                        log.info("Mapper id {} :", key);
                        List<Long> values = mappedData.get(key);
                        for (Long val : values) {
                            log.info("Mapper id {} value : {}", key, val);
                        }
                    }
                    Duration duration = Duration.between(start, end);
                    log.info("Mapper with description {} time duration {} seconds", description, (duration.toMillis() / 1000));
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
        fData.initVectors(ID_MIN, ID_MAX, CHILDREN_COUNT);
        log.info("After init vectors");
        Node root = fData.getRootNode();
        ForkJoinPool pool = new ForkJoinPool();
        ValueGroupIdMapper counter = new ValueGroupIdMapper(root);
        fData.countSum(pool, counter, "GROUP DEFAULT PARALELIZM");
    }

}
