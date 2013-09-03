package org.neo4j.ratpack

import com.google.inject.Inject
import groovy.json.JsonSlurper
import static io.netty.handler.codec.http.HttpMethod.*
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Transaction
import org.neo4j.helpers.collection.IteratorUtil
import org.ratpackframework.handling.Context
import org.ratpackframework.handling.Handler

import static groovy.json.JsonOutput.toJson

class CypherHandler implements Handler {

    private final GraphDatabaseService graphDatabaseService
    private final ExecutionEngine executionEngine
    private final jsonSlurper = new JsonSlurper()

    @Inject
    CypherHandler(ExecutionEngine executionEngine, GraphDatabaseService graphDatabaseService) {
        this.executionEngine = executionEngine
        this.graphDatabaseService = graphDatabaseService
    }

    @Override
    void handle(Context context) {

        Transaction tx = graphDatabaseService.beginTx()
        try {

            context.with {

                String cypher
                Map<String, Object> params
                switch (request.method.name) {
                    case POST.name():
                        request.inputStream.withReader { reader ->
                            def result = jsonSlurper.parse(reader)
                            cypher = result.query
                            params = result.params
                        }
                        break
                    case GET.name():
                        cypher = request.queryParams["query"]
                        params = request.queryParams.findAll {k,v -> k!="query"}
                        break
                    default:
                        throw new IllegalArgumentException("cypher not allowed with http method $request.method.name")
                }

                assert cypher, "now cypher string passed in"
                ExecutionResult result = executionEngine.execute(cypher, params?:Collections.emptyMap())

                respond byContent.json {
                    response.send toJson([columns: result.columns(), data: IteratorUtil.asCollection(result)]) // TODO: streaming
                }
            }
            tx.success()
        } finally {
            tx.finish()
        }
    }
}
