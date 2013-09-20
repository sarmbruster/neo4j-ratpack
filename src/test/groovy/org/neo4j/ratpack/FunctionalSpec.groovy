package org.neo4j.ratpack

import org.msgpack.MessagePack
import org.msgpack.type.ValueFactory
import org.ratpackframework.groovy.test.LocalScriptApplicationUnderTest
import org.ratpackframework.groovy.test.TestHttpClient
import spock.lang.Specification

class FunctionalSpec extends Specification {

    def aut = new LocalScriptApplicationUnderTest()
    @Delegate TestHttpClient client = aut.httpClient()

    def setupSpec() {
        String.metaClass.encodeURL = {
           java.net.URLEncoder.encode(delegate, 'UTF-8')
        }
    }

    def setup() {
        client.resetRequest()
    }

    def "root shows index.html"() {
        when:
        get()

        then:
        response.statusCode == 200
        response.asString().contains("<title>neo4j-ratpack</title>")
    }


    def "cypher via HTTP GET works"() {
        when:
        request.parameters query:"start n=node(*) return n".encodeURL()
        get("cypher")
        def json = response.body.jsonPath()

        then:
        response.statusCode == 200
        json.get("columns").size() == 1
        json.get("columns[0]") == "n"
        json.get("data").size() == 1
        json.get("data[0].n.id") == 0  // the reference node
    }

    def "cypher via HTTP POST works"() {
        when: "send cypher in json as request body"
        request.content query:"start n=node(*) return n"
        post("cypher")
        def json = response.body.jsonPath()

        then:
        response.statusCode == 200
        json.get("columns").size() == 1
        json.get("columns[0]") == "n"
        json.get("data").size() == 1
        json.get("data[0].n.id") == 0  // the reference node
    }

    def "cypher result as msgpack works"() {
        when:
        request.content query:"start n=node(*) return n"
        request.header("Accept", "application/x-msgpack")
        post("cypher")
        def msgPack = new MessagePack().read(response.body.asInputStream())
        def columns = ValueFactory.createRawValue("columns")
        def data = ValueFactory.createRawValue("data")
        def n = ValueFactory.createRawValue("n")
        def id = ValueFactory.createRawValue("id")

        then:
        response.statusCode == 200
        msgPack != null
        msgPack[columns].size() == 1
        msgPack[columns][0].string == "n"
        msgPack[data].size() == 1
        msgPack[data][0][n][id].int == 0
    }

    def "verify list of running queries"() {
        setup:
        def oldExecuteQuery = CypherHandler.metaClass.getMetaMethod("executeQuery", [String, Map] as Class[])
        CypherHandler.metaClass.executeQuery = { String cypher, Map<String,Object> params ->
            sleep 60*1000
            oldExecuteQuery(cypher, params)
        }
        def cypher = "start n=node(*) return n"

        Thread.start {
            def client = aut.httpClient()
            client.resetRequest()
            client.request.content query: cypher
            client.post("cypher")
        }
        sleep 3000

        when:
        request.header("Accept", "application/json")
        get("runningQueries")
        def json = response.jsonPath()

        then:
        response.statusCode == 200
        json.get().size() == 1
        json.get()[0].cypher == cypher

        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(CypherHandler.class)
    }

}
