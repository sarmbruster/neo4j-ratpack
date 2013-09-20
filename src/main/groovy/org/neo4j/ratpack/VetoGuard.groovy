package org.neo4j.ratpack

import groovy.transform.CompileStatic
import org.neo4j.kernel.guard.Guard
import org.neo4j.kernel.guard.GuardException

@CompileStatic
class VetoGuard implements Guard.GuardInternal {

    boolean abort = false

    @Override
    void check() {
        if (abort) {
//            abort = true
            throw new VetoGuardException("aborted")
        }
    }
}
