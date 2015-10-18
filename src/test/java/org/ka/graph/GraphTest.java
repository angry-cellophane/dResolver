package org.ka.graph;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.ka.graph.Graph;
import org.ka.graph.node.Node;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class GraphTest {

    private static class TestNode implements Node {

        final String name;
        final List<Node> upstream;
        final List<Node> downstream;

        private TestNode(String name, List<Node> upstream, List<Node> downstream) {
            this.name = name;
            this.upstream = upstream;
            this.downstream = downstream;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<Node> getUpstream() {
            return upstream;
        }

        @Override
        public List<Node> getDownstream() {
            return downstream;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class NodeMatcher extends BaseMatcher<Node> {

        private final Predicate<Node> predicate;

        private NodeMatcher(Predicate<Node> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean matches(Object item) {
            return item instanceof Node && predicate.test((Node) item);
        }

        @Override
        public void describeTo(Description description) {}
    }

    final Map<String, Object[][]> cases = new HashMap<String, Object[][]>() {{
        put("testCase1", new Object[][] {
//            new Object[] {"Node Name",  "Dependency Node1", "Dependency Node2", ...},
            new Object[] {"A",  "B", "C"},
            new Object[] {"B",  "D"},
            new Object[] {"C"},
            new Object[] {"D",  "C"},
        });

        put("testCase2", new Object[][] {
            new Object[] {"A",  "B", "C"},
            new Object[] {"B",  "D", "E"},
            new Object[] {"C", "B", "F"},
            new Object[] {"D",  "F"},
            new Object[] {"E"},
            new Object[] {"F", "G", "E"},
            new Object[] {"G"},
        });

        put("testCase3", new Object[][] {
            new Object[] {"A",  "B", "C", "D"},
            new Object[] {"B",  "E"},
            new Object[] {"C",  "G" },
            new Object[] {"D",  "C", "G", "H"},
            new Object[] {"E",  "I", "F"},
            new Object[] {"F",  "J", "K"},
            new Object[] {"G",  "F", "K"},
            new Object[] {"H",  "K", "F"},
            new Object[] {"I",  "L"},
            new Object[] {"J",  "L"},
            new Object[] {"K"},
            new Object[] {"L"},
        });
    }};

    @Test
    public void testCase1 () {
        Graph graph = createGraphFor("testCase1");
        Node startNode = graph.get("A");
        assertNotNull(startNode);
        List<Node> nodes = graph.getUpstreamOrderedList(startNode);
        assertEquals(4, nodes.size());
        assertEquals("C", nodes.get(0).getName());
        assertEquals("D", nodes.get(1).getName());
        assertEquals("B", nodes.get(2).getName());
        assertEquals("A", nodes.get(3).getName());
    }

    @Test
    public void testCase2 () {
        Graph graph = createGraphFor("testCase2");
        Node startNode = graph.get("A");
        assertNotNull(startNode);
        List<Node> nodes = graph.getUpstreamOrderedList(startNode);
        assertEquals(7, nodes.size());
        assertThat(nodes.get(0), new NodeMatcher(n -> "E".equals(n.getName()) || "G".equals(n.getName())));
        assertThat(nodes.get(1), new NodeMatcher(n -> "E".equals(n.getName()) || "G".equals(n.getName())));
        assertThat(nodes.get(2), new NodeMatcher(n -> "F".equals(n.getName())));
        assertThat(nodes.get(3), new NodeMatcher(n -> "D".equals(n.getName())));
        assertThat(nodes.get(4), new NodeMatcher(n -> "B".equals(n.getName())));
        assertThat(nodes.get(5), new NodeMatcher(n -> "C".equals(n.getName())));
        assertThat(nodes.get(6), new NodeMatcher(n -> "A".equals(n.getName())));
    }

    @Test
    public void testCase3 () {
        Graph graph = createGraphFor("testCase3");
        Node startNode = graph.get("A");
        assertNotNull(startNode);
        List<Node> nodes = graph.getUpstreamOrderedList(startNode);
        assertEquals(12, nodes.size());

        assertThat(nodes.get(0), new NodeMatcher(n -> "L".equals(n.getName())) );
        assertThat(nodes.get(1), new NodeMatcher(n -> "J".equals(n.getName())) );
        assertThat(nodes.get(2), new NodeMatcher(n -> "K".equals(n.getName())) );
        assertThat(nodes.get(3), new NodeMatcher(n -> "F".equals(n.getName())) );
        assertThat(nodes.get(4), new NodeMatcher(n -> "G".equals(n.getName())) );
        assertThat(nodes.get(5), new NodeMatcher(n -> "I".equals(n.getName())) );
        assertThat(nodes.get(6), new NodeMatcher(n -> "C".equals(n.getName())) );
        assertThat(nodes.get(7), new NodeMatcher(n -> "E".equals(n.getName())) );
        assertThat(nodes.get(8), new NodeMatcher(n -> "H".equals(n.getName())) );
        assertThat(nodes.get(9), new NodeMatcher(n -> "B".equals(n.getName())) );
        assertThat(nodes.get(10), new NodeMatcher(n -> "D".equals(n.getName())));
        assertThat(nodes.get(11), new NodeMatcher(n -> "A".equals(n.getName())));
    }

    private Graph createGraphFor(String testCase) {
        Object[][] data = cases.get(testCase);
        if (data == null) throw new IllegalArgumentException("no such test case: "+testCase);

        Map<String, Node> nodes = new HashMap<>(data.length);
        for (Object[] nodeInfo : data) {
            String nodeName = (String) nodeInfo[0];
            nodes.put(nodeName, new TestNode(nodeName, new ArrayList<>(), new ArrayList<>()));
        }

        for (Object[] nodeInfo : data) {
            String nodeName = (String) nodeInfo[0];
            Node node = nodes.get(nodeName);
            for (int i = 1; i < nodeInfo.length; i++) {
                String depName = (String)nodeInfo[i];
                Node dep = nodes.get(depName);

                node.getUpstream().add(dep);
                dep.getDownstream().add(node);
            }
        }

        return new Graph(new ArrayList<>(nodes.values()));
    }
}