package org.neo4j.ratpack

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

}
