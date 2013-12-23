package org.neo4j.ratpack

import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.handling.Context
import ratpack.http.RequestBody
import ratpack.parse.ParserSupport

import javax.inject.Inject

class CypherJsonParser extends ParserSupport<Cypher, CypherParse> {

    private final ObjectMapper mapper;

    @Inject
    CypherJsonParser(ObjectMapper mapper) {
        this.mapper = mapper
    }

    @Override
    String getContentType() {
        "application/json"
    }

    @Override
    Cypher parse(Context context, RequestBody requestBody, CypherParse parse) {
        mapper.readValue(requestBody.text, Cypher)
    }

}
