package org.neo4j.ratpack

import com.google.inject.AbstractModule
import com.google.inject.Provides
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory

class Neo4jModule extends AbstractModule {

    static GraphDatabaseService graphDatabaseService

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
            graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/dummy")
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
}
