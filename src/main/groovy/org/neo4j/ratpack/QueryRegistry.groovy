package org.neo4j.ratpack

import com.google.inject.Inject
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.ratpackframework.session.SessionIdGenerator

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class QueryRegistry {

    private final ConcurrentHashMap<String, QueryMapEntry> runningQueries = new ConcurrentHashMap<String, QueryMapEntry>()
    private final SessionIdGenerator sessionIdGenerator

    @Inject
    public QueryRegistry(SessionIdGenerator sessionIdGenerator) {
        this.sessionIdGenerator = sessionIdGenerator
    }

    public String registerQuery(String cypher) {
        String key = sessionIdGenerator.generateSessionId(null)
        runningQueries.put(key, new QueryMapEntry(cypher: cypher))
        key
    }

    public QueryMapEntry unregisterQuery(String key) {
        runningQueries.remove(key)
    }

    @EqualsAndHashCode //(includes = ["cypher", "started", "thread"])
    static class QueryMapEntry {
        String cypher
        Date started = new Date()
        Thread thread = Thread.currentThread()
    }
}
