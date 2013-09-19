package org.neo4j.ratpack

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.test.TestGraphDatabaseFactory
import org.ratpackframework.launch.LaunchConfig

class Neo4jModule extends AbstractModule {

    static GraphDatabaseService graphDatabaseService

    @Override
    protected void configure() {

        bind(QueryRegistry).in(Singleton)
        /*JsonOutput.metaClass.'static'.toJson = { Node node ->
            def map = node.propertyKeys.collectEntries { [it, node.getProperty(it)]}
            map.id = node.id
            toJson map
        }
        JsonOutput.metaClass.'static'.toJson = { Relationship relationship ->
            def map = relationship.propertyKeys.collectEntries { [it, relationship.getProperty(it)]}
            map.id = relationship.id
            toJson map
        }*/
    }

    @Provides
    @Singleton
    GraphDatabaseService provideGraphDatabaseService(LaunchConfig launchConfig) {
        if (!graphDatabaseService) {

            def neo4jType = launchConfig.getOther("neo4j.type", "test")
            switch (neo4jType) {
                case "test":
                    graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase()
                    break
                case "embedded":
                    String storeDir = launchConfig.getOther("neo4j.storeDir", "..${File.separator}..${File.separator}build${File.separator}data")
                    graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir).loadPropertiesFromFile("neo4j.properties").newGraphDatabase()
                    break
                case "ha":
                    throw new IllegalArgumentException("neo4j.type=ha not yet implemented")
                    break
                default:
                    throw new IllegalArgumentException("invalid neo4j.type=$neo4jType")
            }
            addShutdownHook {
                graphDatabaseService.shutdown()
            }
        }
        graphDatabaseService
    }

    @Provides
    @Singleton
    ExecutionEngine provideExecutionEngine(LaunchConfig launchConfig) {
        new ExecutionEngine(provideGraphDatabaseService(launchConfig))
    }

}
