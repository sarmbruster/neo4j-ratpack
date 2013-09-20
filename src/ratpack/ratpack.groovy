import org.neo4j.ratpack.CypherHandler
import org.neo4j.ratpack.Neo4jModule
import org.neo4j.ratpack.QueryRegistry

import static groovy.json.JsonOutput.toJson
import static org.ratpackframework.groovy.RatpackScript.ratpack
import static org.ratpackframework.groovy.Template.groovyTemplate


ratpack {

    modules {
        register new Neo4jModule()
    }

    handlers {
        get {
            render groovyTemplate("index.html")
        }

        // register handler based on path and all methods
        handler "cypher", registry.get(CypherHandler)

        // handler for runningqueries in html and json
        get('runningQueries') { QueryRegistry queryRegistry ->
            if (request.queryParams.key) {
                // TODO: terminate query
            }
            respond byContent.html {
                render groovyTemplate("runningQueries.html", runningQueries: queryRegistry.runningQueries)
            }.json {
                def listForJson = queryRegistry.runningQueries.collect { String key, QueryRegistry.QueryMapEntry value ->
                    [key: key, thread: value.thread.id, started: value.started, cypher: value.cypher]
                }
                response.send toJson(listForJson)
            }
        }

        assets "public"

    }

}


