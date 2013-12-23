package org.neo4j.ratpack

import groovy.transform.CompileStatic
import ratpack.handling.Context
import ratpack.http.RequestBody
import ratpack.parse.ParserSupport

import static ratpack.form.Forms.form

@CompileStatic
class CypherFormParser extends ParserSupport<Cypher, CypherParse> {

    @Override
    String getContentType() {
        "application/x-www-form-urlencoded"
    }

    @Override
    Cypher parse(Context context, RequestBody requestBody, CypherParse parse) {
        def form = context.parse(form())
        new Cypher(form.query.toString() ?: "", [:])
    }

}
