package org.neo4j.ratpack

import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.neo4j.kernel.guard.Guard

@CompileStatic
@Log
class VetoGuard implements Guard.GuardInternal {

    boolean abort = false

    @Override
    void check() {
        if (abort) {
            def msg = "aborted query for thread ${Thread.currentThread()}"
            log.info msg
            throw new VetoGuardException(msg)
        }
    }
}
