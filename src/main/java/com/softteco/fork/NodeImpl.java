package com.softteco.fork;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public class NodeImpl implements Node{

    private Collection<Node> children;
    private long value;
    private String id;

    public NodeImpl(long value, Collection<Node> children, String id) {
        this.value = value;
        this.children = children;
        this.id = id;
        log.info("Created node with value {}, children size {}, id {}", value, children.size(), id);
    }

    @Override
    public Collection<Node> getChildren() {
        try {
            Thread.sleep(8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return children;
    }

    @Override
    public long getValue() {
        log.info("Get value from node with params value : {}, id {}", value, id);
        return value;
    }

    @Override
    public String getId() {
        return id;
    }
}
