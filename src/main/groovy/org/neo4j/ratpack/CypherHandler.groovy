package org.neo4j.ratpack

import com.google.gson.Gson
import com.google.inject.Inject

import static io.netty.handler.codec.http.HttpMethod.*
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Transaction
import org.neo4j.helpers.collection.IteratorUtil
import org.ratpackframework.handling.Context
import org.ratpackframework.handling.Handler

class CypherHandler implements Handler {

    private final GraphDatabaseService graphDatabaseService
    private final ExecutionEngine executionEngine
    private final Gson gson = new Gson();

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
                        Map parsedJson = gson.fromJson(new InputStreamReader(request.inputStream), Map)
                        cypher = parsedJson.get("query")
                        params = parsedJson.get("params")
                        break
                    case GET.name():
                        cypher = request.queryParams["query"]
                        params = request.queryParams.findAll {k,v -> k!="query"}
                        break
                    default:
                        throw new IllegalArgumentException("cypher not allowed with http method $request.method.name")
                }

                assert cypher, "no cypher string passed in"
                ExecutionResult result = executionEngine.execute(cypher, params?:Collections.emptyMap())

                respond byContent.json {
                    response.send gson.toJson([columns: result.columns(), data: IteratorUtil.asCollection(result)])
                    //  response.send toJson([columns: result.columns(), data: IteratorUtil.asCollection(result)]) // TODO: streaming
                }.html {

                }.xml {

                }.plainText {

                }.type("text/csv") {

                }
            }
            tx.success()

        } finally {
            tx.finish()
        }
    }
}
