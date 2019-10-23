package com.softteco.fork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class ValueGroupIdMapper extends RecursiveTask<Map<String, List<Long>>> {
    private Node node;

    public ValueGroupIdMapper(Node node) {
        this.node = node;
    }

    @Override
    protected Map<String, List<Long>> compute() {
        Map<String, List<Long>> result = new HashMap<>();
        String id = node.getId();
        Long value = node.getValue();
        List<Long> idData = new ArrayList<>();
        idData.add(value);
        result.put(id, idData);
        List<ValueGroupIdMapper> subTasks = new LinkedList<>();
        for(Node child : node.getChildren()) {
            ValueGroupIdMapper task = new ValueGroupIdMapper(child);
            task.fork(); // start asyncronously
            subTasks.add(task);
        }
        for(ValueGroupIdMapper task : subTasks) {
            Map<String, List<Long>> subTaskMap = task.join(); // waiting for task completed and add result
            for(String key : subTaskMap.keySet()) {
                if (result.containsKey(key)) {
                    List<Long> vals = result.get(key);
                    vals.addAll(subTaskMap.get(key));
                } else {
                    result.put(key ,subTaskMap.get(key));
                }
            }
        }
        return result;
    }

}