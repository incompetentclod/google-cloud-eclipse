/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.resolution.ArtifactResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
// import org.eclipse.aether.transport.file.FileTransporterFactory;
// import org.eclipse.aether.transport.http.HttpTransporterFactory;


public class DependencyResolver {

  public static List<String> getTransitiveDependencies(
      String groupId, String artifactId, String version) throws DependencyResolutionException {
    
    List<String> dependencies = new ArrayList<>();
   
    Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);

    DependencyFilter filter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

    CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRoot( new Dependency(artifact, JavaScopes.COMPILE));
    
    RepositorySystem system = newRepositorySystem();
    RepositorySystemSession session = newRepositorySystemSession(system);    
    collectRequest.setRepositories(newRepositories(system, session));
    DependencyRequest request = new DependencyRequest(collectRequest, filter);

    List<ArtifactResult> artifacts =
        system.resolveDependencies(session, request).getArtifactResults();
    for (ArtifactResult result : artifacts) {
      dependencies.add(result.toString());
    }
    
    return dependencies;
  }

  static RepositorySystem newRepositorySystem() {
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    // locator.addService(TransporterFactory.class, FileTransporterFactory.class);
    // locator.addService(TransporterFactory.class, HttpTransporterFactory.class);*/

    locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
      @Override
      public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable ex) {
        ex.printStackTrace();
      }
    });
    DefaultRepositorySystem de = new DefaultRepositorySystem();
    return de;
   // return locator.getService(RepositorySystem.class);
  }

   static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
      DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

    LocalRepository localRepository = new LocalRepository("target/local-repo");
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));
/*
      session.setTransferListener(new ConsoleTransferListener() );
      session.setRepositoryListener( new ConsoleRepositoryListener() );
*/
      
      return session;
  }

  static List<RemoteRepository> newRepositories(RepositorySystem system, RepositorySystemSession session) {
    RemoteRepository.Builder builder =
        new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/");
    RemoteRepository repository = builder.build();
    List<RemoteRepository> repositories = new ArrayList<>();
    repositories.add(repository);
    return repositories;
  }
}
