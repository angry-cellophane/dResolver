package org.ka.artifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.util.Collection;

public interface ArtifactRepository {
    Collection<Dependency> getDependencies(String artifactName, Scope scope);
    Collection<Artifact> getDependentArtifacts(String artifactName, Scope scope);
    Collection<Artifact> getTransitiveDependentArtifacts(String artifactName, Scope scope);
}
