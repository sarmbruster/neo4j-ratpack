package org.neo4j.ratpack

import com.google.inject.Inject
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Log
import org.neo4j.kernel.guard.Guard
import org.ratpackframework.session.SessionIdGenerator

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
@Log
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
        log.info "registered query $cypher ($key)"
        key
    }

    public QueryMapEntry unregisterQuery(String key) {
        guard.stop()
        log.info "unregistered query ($key)"
        runningQueries.remove(key)
    }

    QueryMapEntry abortQuery(String key) {
        QueryMapEntry entry = runningQueries[key]
        if (entry==null) {
            throw new IllegalArgumentException("no query running with key $key")
        }
        entry.vetoGuard.abort = true
        log.info "aborted query ($key)"

        entry
    }

    @EqualsAndHashCode //(includes = ["cypher", "started", "thread"])
    @ToString
    static class QueryMapEntry {
        String cypher
        Date started = new Date()
        Thread thread = Thread.currentThread()
        VetoGuard vetoGuard
    }
}
