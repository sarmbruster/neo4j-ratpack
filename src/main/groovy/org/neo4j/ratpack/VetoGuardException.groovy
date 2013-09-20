package org.neo4j.ratpack

import groovy.transform.CompileStatic
import org.neo4j.kernel.guard.GuardException

@CompileStatic
class VetoGuardException extends GuardException {
    VetoGuardException(String message) {
        super(message)
    }
}
