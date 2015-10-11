package org.ka.resolver;

import org.ka.resolver.node.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Graph implements Resolver {

    private static class Edge {
        final String a;
        final String b;

        private Edge(String a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    private final Map<String, Node> nodes;
    private final List<Edge> edges;

    public Graph(List<Node> nodes) {
        this.nodes = new HashMap<>(nodes.size());
        this.edges = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            this.nodes.put(node.getName(), node);
            for (Node neighbour : node.getUpstream()) {
                edges.add(new Edge(node.getName(), neighbour.getName()));
            }
        }
    }

    @Override
    public List<Node> getUpstreamOrderedList(Node startNode) {
        Map<String, Integer> distances = initDistances(startNode);
        boolean isStopped = false;
        while (!isStopped) {
            isStopped = true;
            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                if (distances.get(edge.a) < Integer.MAX_VALUE) {
                    if (distances.get(edge.b) > distances.get(edge.a) - 1) {
                        distances.put(edge.b, distances.get(edge.a) - 1);
                        isStopped = false;
                    }
                }
            }
        }

        return distances.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()))
                .map(Map.Entry::getKey)
                .map(nodes::get)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> initDistances(Node startNode) {
        return nodes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue() == startNode ? 0 : Integer.MAX_VALUE));
    }

    @Override
    public List<Node> getDownstreamOrderedList(Node startNode) {
        return startNode.getDownstream();
    }

    public Node get(String nodeName) {
        return nodes.get(nodeName);
    }
}
