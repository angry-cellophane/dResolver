package org.ka.graph;

import org.ka.graph.node.Node;

import java.util.List;

public interface NodeResolver {
    List<Node> getUpstreamOrderedList(Node startNode);
    List<Node> getDownstreamOrderedList(Node startNode);
}
