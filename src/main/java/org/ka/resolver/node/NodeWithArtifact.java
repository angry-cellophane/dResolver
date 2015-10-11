package org.ka.resolver.node;

import org.eclipse.aether.artifact.Artifact;

import java.util.Collections;
import java.util.List;

public class NodeWithArtifact implements Node {
    final List<Node> upstream;
    final List<Node> downstream;

    final Artifact artifact;

    public NodeWithArtifact(List<Node> upstream,
                            List<Node> downstream,
                            Artifact artifact) {
        this.upstream = Collections.unmodifiableList(upstream);
        this.downstream = Collections.unmodifiableList(downstream);
        this.artifact = artifact;
    }

    @Override
    public String getName() {
        return artifact.getArtifactId();
    }

    @Override
    public List<Node> getUpstream() {
        return upstream;
    }

    @Override
    public List<Node> getDownstream() {
        return downstream;
    }

    public Artifact getArtifact() {
        return this.artifact;
    }
}
