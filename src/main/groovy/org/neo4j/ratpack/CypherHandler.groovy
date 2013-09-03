package org.neo4j.ratpack

import com.google.inject.Inject
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
    private final ExecutionEngine exectionEngine

    @Inject
    CypherHandler(ExecutionEngine executionEngine, GraphDatabaseService graphDatabaseService) {
        this.exectionEngine = executionEngine
        this.graphDatabaseService = graphDatabaseService
    }

    @Override
    void handle(Context context) {

        Transaction tx = graphDatabaseService.beginTx()
        try {

            context.with {

                String cypher = request.queryParams["cypher"]
                def params = request.queryParams.findAll {k,v -> k!="cypher"}

                ExecutionResult result = exectionEngine.execute(cypher, params)

                respond byContent.json {
                    response.send toJson([columns: result.columns(), data: IteratorUtil.asCollection(result)])

                }

            }
            tx.success()
        } finally {
            tx.finish()
        }


    }
}
