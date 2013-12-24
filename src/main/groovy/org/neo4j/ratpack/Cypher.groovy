package org.neo4j.ratpack

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.CompileStatic

@CompileStatic
class Cypher {

    final String query
    final Map<String, Object> params

    Cypher(@JsonProperty("query") String query, @JsonProperty("params") Map<String, Object> params) {
        this.query = query
        this.params = params?.asImmutable()
    }

}
