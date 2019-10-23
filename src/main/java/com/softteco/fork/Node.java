package com.softteco.fork;

import java.util.Collection;

public interface Node {
    Collection<Node> getChildren();

    long getValue();

    String getId();
}