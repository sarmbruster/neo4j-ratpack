package org.neo4j.ratpack.gson

import com.google.gson.*
import groovy.transform.CompileStatic
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.PropertyContainer

import java.lang.reflect.Type

@CompileStatic
abstract class PropertyContainerSerializer<T extends PropertyContainer> implements JsonSerializer<T> {

    protected serializeProperties(PropertyContainer propertyContainer, JsonObject result, JsonSerializationContext context) {
        for (String key in propertyContainer.propertyKeys) {
            result.add(key, context.serialize(propertyContainer.getProperty(key)))
        }
    }
}
