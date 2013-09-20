package org.neo4j.ratpack

import com.google.inject.Inject
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.GraphDatabaseAPI
import org.neo4j.kernel.guard.Guard
import org.ratpackframework.session.SessionIdGenerator

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class QueryRegistry {

    private final ConcurrentHashMap<String, QueryMapEntry> runningQueries = new ConcurrentHashMap<String, QueryMapEntry>()
    private final SessionIdGenerator sessionIdGenerator
    private final Guard guard

    @Inject
    public QueryRegistry(SessionIdGenerator sessionIdGenerator, Guard guard) {
        this.sessionIdGenerator = sessionIdGenerator
        this.guard = guard
    }

    public String registerQuery(String cypher) {
        String key = sessionIdGenerator.generateSessionId(null)
        VetoGuard vetoGuard = new VetoGuard()
        guard.start(vetoGuard)
        runningQueries.put(key, new QueryMapEntry(cypher: cypher, vetoGuard: vetoGuard))
        key
    }

    public QueryMapEntry unregisterQuery(String key) {
        guard.stop()
        runningQueries.remove(key)
    }

    QueryMapEntry abortQuery(String key) {
        QueryMapEntry entry = runningQueries[key]
        entry.vetoGuard.abort = true
        entry
    }

    @EqualsAndHashCode //(includes = ["cypher", "started", "thread"])
    static class QueryMapEntry {
        String cypher
        Date started = new Date()
        Thread thread = Thread.currentThread()
        VetoGuard vetoGuard
    }
}
