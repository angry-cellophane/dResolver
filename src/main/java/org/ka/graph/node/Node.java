package org.ka.graph.node;


import java.util.List;

public interface Node {
    String getName();
    List<Node> getUpstream();
    List<Node> getDownstream();
}
