package com.softteco.fork;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class ValueSumCounter extends RecursiveTask<Long> {
    private final Node node;

    public ValueSumCounter(Node node) {
        this.node = node;
    }

    @Override
    protected Long compute() {
        long sum = node.getValue();
        List<ValueSumCounter> subTasks = new LinkedList<>();

        for(Node child : node.getChildren()) {
            ValueSumCounter task = new ValueSumCounter(child);
            task.fork(); // start asyncronously
            subTasks.add(task);
        }

        for(ValueSumCounter task : subTasks) {
            sum += task.join(); // waiting for task completed and add result
        }

        return sum;
    }

}