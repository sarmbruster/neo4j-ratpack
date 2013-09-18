package org.neo4j.ratpack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.Inject
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.Transaction
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.ratpack.gson.ExecutionResultSerializer
import org.neo4j.ratpack.gson.NodeSerializer
import org.neo4j.ratpack.gson.RelationshipSerializer
import org.ratpackframework.handling.Context
import org.ratpackframework.handling.Handler

import static io.netty.handler.codec.http.HttpMethod.GET
import static io.netty.handler.codec.http.HttpMethod.POST
import static org.ratpackframework.groovy.Template.groovyTemplate

class CypherHandler implements Handler {

    private final GraphDatabaseService graphDatabaseService
    private final ExecutionEngine executionEngine
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(ExecutionResult, new ExecutionResultSerializer())
        .registerTypeHierarchyAdapter(Node, new NodeSerializer())
        .registerTypeHierarchyAdapter(Relationship, new RelationshipSerializer())
    /*.registerTypeAdapterFactory( new TypeAdapterFactory() {
        @Override
        def <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return null  //To change body of implemented methods use File | Settings | File Templates.
        }
    })*/
        .create()

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
                    response.send gson.toJson(result) //[columns: result.columns(), data: IteratorUtil.asCollection(result)])
                    //  response.send toJson([columns: result.columns(), data: IteratorUtil.asCollection(result)]) // TODO: streaming
                }.html {
                    render groovyTemplate("cypherResult.html", cypher:cypher, columns: result.columns(), data: IteratorUtil.asCollection(result) )
                }.plainText {
                    response.send result.dumpToString()
                }.type("text/csv") {
                    StringBuilder sb = new StringBuilder()
                    sb.append result.columns().join(",")
                    sb.append "\n"
                    for (row in result) {
                        sb.append result.columns().collect { row[it]}.join(",")
                        sb.append "\n"
                    }
                    response.send sb.toString()
                }
            }
            tx.success()

        } finally {
            tx.finish()
        }
    }
}
