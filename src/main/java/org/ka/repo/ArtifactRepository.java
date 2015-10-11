package org.ka.repo;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;

import java.util.Collection;

public interface ArtifactRepository {
    Collection<Dependency> getDependencies(String artifactName, Scope scope);
    Collection<Artifact> getDependentArtifacts(String artifactName, Scope scope);
    Collection<Artifact> getTransitiveDependentArtifacts(String artifactName, Scope scope);
}
