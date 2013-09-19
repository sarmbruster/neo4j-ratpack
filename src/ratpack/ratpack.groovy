import org.neo4j.ratpack.CypherHandler
import org.neo4j.ratpack.Neo4jModule
import org.neo4j.ratpack.QueryRegistry

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

        get('runningQueries') { QueryRegistry queryRegistry ->
            if (request.queryParams.key) {
                // TODO: terminate query
            }
            render groovyTemplate("runningQueries.html", runningQueries: queryRegistry.runningQueries)
        }

        assets "public"

    }

}


