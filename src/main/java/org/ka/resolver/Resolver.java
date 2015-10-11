package org.ka.resolver;

import org.ka.resolver.node.Node;

import java.util.List;

public interface Resolver {
    List<Node> getUpstreamOrderedList(Node startNode);
    List<Node> getDownstreamOrderedList(Node startNode);
}
