package org.ka.repo;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalArtifactRepository implements ArtifactRepository {

    private URI repoUri = suppressException(() -> new URI("file:///C:/Users/Александр/.m2/repository"));

    @Override
    public Collection<Dependency> getDependencies(String artifactName, Scope scope) {
        return requestToRepo((repoSystem, session) -> {
            DependencyNode node = getDependencyNode(repoSystem, session, artifactName, scope);

            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            node.accept( nlg );
            return nlg.getDependencies(true);
        });
    }

    @Override
    public Collection<Artifact> getDependentArtifacts(String artifactName, Scope scope) {
        return getArtifacts(artifactName, scope, dependencyNode -> {
            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setRoot(dependencyNode);
            dependencyRequest.setFilter((node1, parents) -> !parents.isEmpty() && parents.get(0).equals(dependencyNode));
            return dependencyRequest;
        });
    }

    @Override
    public Collection<Artifact> getTransitiveDependentArtifacts(String artifactName, Scope scope) {
        return getArtifacts(artifactName, scope, dependencyNode -> {
            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setRoot(dependencyNode);
            return dependencyRequest;
        });
    }

    private Collection<Artifact> getArtifacts(String artifactName,
                                              Scope scope,
                                              Function<DependencyNode,
                                                      DependencyRequest> requestCreator) {
        return requestToRepo((repoSystem, session) -> {
            DependencyNode dependencyNode = getDependencyNode(repoSystem, session, artifactName, scope);

            DependencyResult result = suppressException(() -> repoSystem.resolveDependencies(session, requestCreator.apply(dependencyNode)));
            return result.getArtifactResults()
                    .stream()
                    .map(ArtifactResult::getArtifact)
                    .collect(Collectors.toList());
        });
    }

    private <T> Collection<T> requestToRepo(BiFunction<RepositorySystem, RepositorySystemSession, Collection<T>> fun) {
        RepositorySystem repoSystem = newRepositorySystem();
        RepositorySystemSession session = newSession(repoSystem);

        return fun.apply(repoSystem, session);
    }

    private DependencyNode getDependencyNode(RepositorySystem repoSystem, RepositorySystemSession session,
                                             String artifactName, Scope scope) {
        Dependency dependency =
                new Dependency( new DefaultArtifact( artifactName ), scope.toString() );
        RemoteRepository local = new RemoteRepository.Builder( "local", "default", getRepoUri().toString()).build();

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( dependency );
        collectRequest.addRepository(local);

        try {
            return repoSystem.collectDependencies( session, collectRequest ).getRoot();
        } catch (DependencyCollectionException e) {
            throw new RuntimeException(e);
        }
    }

    private static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession(RepositorySystem system ) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        return session;
    }

    private static <T> T suppressException(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public URI getRepoUri() {
        return repoUri;
    }

    public void setRepoUri(URI repoUri) {
        this.repoUri = repoUri;
    }
}
