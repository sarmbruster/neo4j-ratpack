package org.neo4j.ratpack.gson

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import groovy.transform.CompileStatic
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship

import java.lang.reflect.Type

@CompileStatic
class RelationshipSerializer extends PropertyContainerSerializer<Relationship> {

    @Override
    JsonElement serialize(Relationship relationship, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.add("id", new JsonPrimitive((Number)relationship.getId()))
        serializeProperties(relationship, result, context)
        return result
    }
}
