import org.neo4j.ratpack.CypherHandler
import org.neo4j.ratpack.Neo4jModule

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

        assets "public"

    }

}


