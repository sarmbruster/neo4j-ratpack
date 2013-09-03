import org.neo4j.ratpack.CypherHandler
import org.neo4j.ratpack.Neo4jModule

import static org.ratpackframework.groovy.RatpackScript.ratpack

ratpack {

    modules {
//        register new ScriptExecutionModule()
        register new Neo4jModule()
    }

    handlers {
//        get {
//            render groovyTemplate("skin.html", title: "Groovy Web Console")
//        }

//        post("execute") { ScriptExecutor scriptExecutor ->
//            def script = request.form.script
//            render scriptExecutor.execute(script)
//        }

//        get("reloadexample") {
//            response.send new ReloadingThing().toString()
//        }


        // register handler based on path and all methods
        handler "", registry.get(CypherHandler)

//        assets "public"

    }

}


