package org.neo4j.ratpack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.Inject
import groovy.transform.CompileStatic
import org.msgpack.MessagePack
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.Transaction
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.kernel.impl.core.NodeProxy
import org.neo4j.ratpack.gson.ExecutionResultSerializer
import org.neo4j.ratpack.gson.NodeSerializer
import org.neo4j.ratpack.gson.RelationshipSerializer
import org.neo4j.ratpack.msgpack.NodeTemplate
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.groovy.Groovy.groovyTemplate

class CypherHandler extends GroovyHandler {

    private final GraphDatabaseService graphDatabaseService
    private final ExecutionEngine executionEngine
    private final QueryRegistry queryRegistry

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ExecutionResult, new ExecutionResultSerializer())
            .registerTypeHierarchyAdapter(Node, new NodeSerializer())
            .registerTypeHierarchyAdapter(Relationship, new RelationshipSerializer())
            .create()

    private final MessagePack messagePack

    @Inject
    CypherHandler(ExecutionEngine executionEngine, GraphDatabaseService graphDatabaseService, QueryRegistry queryRegistry) {
        this.executionEngine = executionEngine
        this.graphDatabaseService = graphDatabaseService
        this.queryRegistry = queryRegistry
        messagePack = new MessagePack()
        messagePack.register(NodeProxy, new NodeTemplate())
    }

    @Override
    void handle(GroovyContext context) {
        context.with {
            Cypher cypher
            if (request.method.post) {
                cypher = context.parse(new CypherParse())
            } else if (request.method.get) {
                def query = request.queryParams["query"]
                def params = request.queryParams.findAll { k, v -> k != "query" } as Map<String, Object>
                cypher = new Cypher(query, params)
            } else {
                context.clientError(405)
                return
            }

            if (cypher) {
                Transaction tx = graphDatabaseService.beginTx()
                try {
                    String queryKey = queryRegistry.registerQuery(cypher.query)
                    try {
                        ExecutionResult result = executeQuery(cypher.query, cypher.params)

                        context.byContent {
                            json {
                                render gson.toJson(result)
                            }
                            html {
                                render groovyTemplate("cypherResult.html", cypher: cypher.query, columns: result.columns(), data: IteratorUtil.asCollection(result))
                            }
                            plainText {
                                render result.dumpToString()
                            }
                            type("text/csv") {
                                StringBuilder sb = new StringBuilder()
                                sb.append result.columns().join(",")
                                sb.append "\n"
                                for (row in result) {
                                    sb.append result.columns().collect { row[it] }.join(",")
                                    sb.append "\n"
                                }
                                render sb.toString()
                            }
                            type("application/x-msgpack") {
                                response.send messagePack.write(columns: result.columns(), data: IteratorUtil.asCollection(result))
                            }
                        }
                        tx.success()
                    } finally {
                        queryRegistry.unregisterQuery(queryKey)
                    }
                } finally {
                    tx.finish()
                }
            } else {
                byContent.html {
                    render groovyTemplate("cypherResult.html", cypher: '', columns: [], data: [])
                }
            }
        }
    }

    @CompileStatic
    private ExecutionResult executeQuery(String cypher, Map<String, Object> params) {
        executionEngine.execute(cypher, params ?: (Map<String, Object>) Collections.emptyMap())
    }

}