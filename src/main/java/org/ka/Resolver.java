package org.ka;

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
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import java.io.File;
import java.util.List;

public class Resolver {

    private static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession( RepositorySystem system )
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        return session;
    }

    public static void main(String[] args) throws DependencyCollectionException, DependencyResolutionException {
        RepositorySystem repoSystem = newRepositorySystem();

        RepositorySystemSession session = newSession( repoSystem );

        Dependency dependency =
                new Dependency( new DefaultArtifact( "org.ka:test-d:1.0-SNAPSHOT" ), "compile" );
        RemoteRepository local = new RemoteRepository.Builder( "local", "default", "file:///C:/Users/Александр/.m2/repository" ).build();

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( dependency );
        collectRequest.addRepository(local);
        DependencyNode node = repoSystem.collectDependencies( session, collectRequest ).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot( node );

        DependencyResult result = repoSystem.resolveDependencies(session, dependencyRequest);
        result.getArtifactResults().stream()
                .map(ArtifactResult::getArtifact)
                .map(Artifact::getFile)
                .map(File::getName)
                .forEach(System.out::println);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept( nlg );
        nlg.getDependencies(true).stream()
            .map(Dependency::getArtifact)
            .forEach(a -> System.out.println(a));
    }
}
