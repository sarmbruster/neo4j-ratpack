package org.neo4j.ratpack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.Inject
import groovy.transform.CompileStatic
import org.msgpack.MessagePack
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.Transaction
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.kernel.impl.core.NodeProxy
import org.neo4j.ratpack.gson.ExecutionResultSerializer
import org.neo4j.ratpack.gson.NodeSerializer
import org.neo4j.ratpack.gson.RelationshipSerializer
import org.neo4j.ratpack.msgpack.NodeTemplate
import org.ratpackframework.handling.Context
import org.ratpackframework.handling.Handler
import org.ratpackframework.http.Request

import static io.netty.handler.codec.http.HttpMethod.GET
import static io.netty.handler.codec.http.HttpMethod.POST
import static org.ratpackframework.groovy.Template.groovyTemplate

class CypherHandler implements Handler {

    private final GraphDatabaseService graphDatabaseService
    private final ExecutionEngine executionEngine
    private final QueryRegistry queryRegistry

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
    void handle(Context context) {

        Transaction tx = graphDatabaseService.beginTx()
        try {

            context.with {
                def (cypher, params) = parseCypherAndParamsFrom(request)
                if (cypher) {
                    String queryKey = queryRegistry.registerQuery(cypher)
                    try {
                        ExecutionResult result = executeQuery(cypher, params)

                        def respondWithClone = respondWith.clone() // for thread safety
                        respondWithClone.delegate = delegate
                        respondWithClone result, cypher
                    } finally {
                        queryRegistry.unregisterQuery(queryKey)
                    }
                } else {
                    respond byContent.html {
                        render groovyTemplate("cypherResult.html", cypher: '', columns: [], data: [])
                    }
                }
            }
            tx.success()

        } finally {
            tx.finish()
        }
    }

    @CompileStatic
    private ExecutionResult executeQuery(String cypher, Map<String,Object> params) {
        executionEngine.execute(cypher, params ?: ( Map<String,Object>)Collections.emptyMap())
    }

    @CompileStatic
    def parseCypherAndParamsFrom(Request request) {
        String cypher
        Map<String, Object> params = Collections.emptyMap()
        switch (request.method.name) {
            case POST.name():
                switch (request.contentType) {
                    case "application/x-www-form-urlencoded":
                        cypher = URLDecoder.decode(request.form.query, "UTF-8")
                        break
                    default:
                        Map parsedJson = gson.fromJson(new InputStreamReader(request.inputStream), Map)
                        cypher = parsedJson.get("query")
                        params = parsedJson.get("params") as Map<String, Object>
                }
                break
            case GET.name():
                cypher = request.queryParams["query"]
                params = request.queryParams.findAll { k, v -> k != "query" } as Map<String, Object>
                break
            default:
                throw new IllegalArgumentException("cypher not allowed with http method $request.method.name")
        }
        [cypher, params]
    }

    /**
     * render response of a cypher query depending on accept header
     */
    def respondWith = { result, cypher ->
        respond byContent.json {
            response.send gson.toJson(result) //[columns: result.columns(), data: IteratorUtil.asCollection(result)])
            //  response.send toJson([columns: result.columns(), data: IteratorUtil.asCollection(result)]) // TODO: streaming
        }.html {
            render groovyTemplate("cypherResult.html", cypher: cypher, columns: result.columns(), data: IteratorUtil.asCollection(result))
        }.plainText {
            response.send result.dumpToString()
        }.type("text/csv") {
            StringBuilder sb = new StringBuilder()
            sb.append result.columns().join(",")
            sb.append "\n"
            for (row in result) {
                sb.append result.columns().collect { row[it] }.join(",")
                sb.append "\n"
            }
            response.send sb.toString()
        }.type("application/x-msgpack") {
            response.send messagePack.write(columns: result.columns(), data: org.neo4j.helpers.collection.IteratorUtil.asCollection(result),)
        }
    }
}
