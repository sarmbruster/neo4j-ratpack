package org.neo4j.ratpack.gson

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.helpers.collection.IteratorUtil

import java.lang.reflect.Type

class ExecutionResultSerializer implements JsonSerializer<ExecutionResult> {
    @Override
    JsonElement serialize(ExecutionResult executionResult, Type typeOfSrc, JsonSerializationContext context) {

        def result = new JsonObject()
        result.add("columns", context.serialize(executionResult.columns()))
        result.add("data", context.serialize(IteratorUtil.asCollection(executionResult)))

        return result
    }
}
