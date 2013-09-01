import groovywebconsole.ReloadingThing
import groovywebconsole.ScriptExecutionModule
import groovywebconsole.ScriptExecutor
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.ratpack.Neo4jModule

import static org.ratpackframework.groovy.RatpackScript.ratpack
import static org.ratpackframework.groovy.Template.groovyTemplate

ratpack {

    modules {
        register new ScriptExecutionModule()
        register new Neo4jModule()
    }

    handlers {
        get {
            render groovyTemplate("skin.html", title: "Groovy Web Console")
        }

        post("execute") { ScriptExecutor scriptExecutor ->
            def script = request.form.script
            render scriptExecutor.execute(script)
        }

        get("reloadexample") {
            response.send new ReloadingThing().toString()
        }

        get("cypher") { ExecutionEngine e ->
            getResponse().send e.execute("start n=node(*) return n").dumpToString()

        }

        assets "public"

    }

}


