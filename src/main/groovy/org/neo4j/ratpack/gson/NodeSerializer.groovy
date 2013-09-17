package org.neo4j.ratpack.gson

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import groovy.transform.CompileStatic
import org.neo4j.graphdb.Node

import java.lang.reflect.Type

@CompileStatic
class NodeSerializer extends PropertyContainerSerializer<Node> {

    @Override
    JsonElement serialize(Node node, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.add("id", new JsonPrimitive((Number)node.getId()))
        serializeProperties(node, result, context)
        return result
    }
}
