import org.neo4j.ratpack.*
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context
import ratpack.jackson.Jackson
import ratpack.jackson.JacksonModule
import ratpack.session.SessionModule

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {

    modules {
        register new SessionModule()
        register new Neo4jModule()
        register new JacksonModule()
        bind CypherFormParser
        bind CypherJsonParser
    }

    handlers {
        get {
            render groovyTemplate("index.html")
        }

        // register handler based on path and all methods
        handler "cypher", registry.get(CypherHandler)

        // handler for runningqueries in html and json
        get('runningQueries') { QueryRegistry queryRegistry ->
            String flash = ''
            if (request.queryParams.key) {
                def queryMapEntry = queryRegistry.abortQuery(request.queryParams.key)
                if (queryMapEntry != null) {
                    flash = "query $queryMapEntry has been aborted."
                }
            }

            byContent {
                html {
                    render groovyTemplate("runningQueries.html", runningQueries: queryRegistry.runningQueries, flash: flash)
                }
                json {
                    def listForJson = queryRegistry.runningQueries.collect { String key, QueryRegistry.QueryMapEntry value ->
                        [key: key, thread: value.thread.id, started: value.started, cypher: value.cypher]
                    }
                    render Jackson.json(listForJson)
                }
            }
        }

        assets "public"
    }
}


