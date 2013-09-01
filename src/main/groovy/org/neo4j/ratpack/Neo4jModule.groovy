package org.neo4j.ratpack

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.ratpackframework.guice.HandlerDecoratingModule
import org.ratpackframework.handling.Handler
import org.ratpackframework.launch.LaunchConfig

class Neo4jModule extends AbstractModule implements HandlerDecoratingModule {

    static GraphDatabaseService graphDatabaseService
    String storeDir

    @Override
    protected void configure() {
//        def graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/dummy")
//        bind(GraphDatabaseService).toInstance(graphDatabaseService)
//        bind(ExecutionEngine).toInstance(new ExecutionEngine(graphDatabaseService))
//        bind(ExecutionEngine)//.toInstance(new ExecutionEngine(graphDatabaseService))
    }

    @Provides
    @com.google.inject.Singleton
    GraphDatabaseService provideGraphDatabaseService() {
        if (!graphDatabaseService) {
            graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir)
            addShutdownHook {
                graphDatabaseService.shutdown()
            }
        }
        graphDatabaseService
    }

    @Provides
    @com.google.inject.Singleton
    ExecutionEngine provideExecutionEngine() {
        new ExecutionEngine(provideGraphDatabaseService())
    }

    @Override
    Handler decorate(Injector injector, Handler handler) {
        LaunchConfig launchConfig = injector.getInstance(LaunchConfig.class)
        storeDir = launchConfig.getOther("neo4j.storeDir", "..${File.separator}..${File.separator}build${File.separator}data")
        handler
    }
}
