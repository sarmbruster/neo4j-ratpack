package org.neo4j.ratpack

import org.ratpackframework.session.internal.DefaultSessionIdGenerator
import spock.lang.Specification

class QueryRegistrySpec extends Specification {

    QueryRegistry registry
    def setup() {
        registry = new QueryRegistry(new DefaultSessionIdGenerator())

    }

    def "registering queries fills up registry"() {
        setup:

        expect:
        registry.runningQueries.size() == 0

        when:
        registry.registerQuery("start n=node(*) return n")

        then:
        registry.runningQueries.size() == 1
    }

    def "unregistering queries get removed from registry"() {
        setup:
        def key = registry.registerQuery("start n=node(*) return n")

        expect:
        registry.runningQueries.size() == 1

        when:
        registry.unregisterQuery(key)

        then:
        registry.runningQueries.size() == 0
    }

    def "registered queries are stored with date and thread"() {
        setup:
        def cypher = "start n=node(*) return n"
        def key = registry.registerQuery(cypher)

        when:
        def result = registry.runningQueries[key]

        then:
        result != null
        result.cypher == cypher
        Math.abs(result.started.time - new Date().time) < 100
        result.thread == Thread.currentThread()
    }

    def "same query can be registered multiple times"() {
        setup:
        def cypher = "start n=node(*) return n"

        when:
        def key1 = registry.registerQuery(cypher)

        then:
        registry.runningQueries.size() == 1

        when:
        sleep 1  // otherwise 'started' timestamp is the same
        def key2 = registry.registerQuery(cypher)

        then:
        key1 != key2
        registry.runningQueries.size() == 2

        when:
        def registryEntry1 = registry.runningQueries[key1]
        def registryEntry2 = registry.runningQueries[key2]

        then:
        registryEntry1 != null
        registryEntry2 != null
        registryEntry1 != registryEntry2
        registryEntry2.started.time > registryEntry1.started.time
    }

}
