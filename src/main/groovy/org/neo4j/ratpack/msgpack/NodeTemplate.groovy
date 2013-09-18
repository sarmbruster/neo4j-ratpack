package org.neo4j.ratpack.msgpack

import org.msgpack.packer.Packer
import org.msgpack.template.AbstractTemplate
import org.msgpack.unpacker.Unpacker
import org.neo4j.graphdb.Node

class NodeTemplate extends AbstractTemplate<Node> {
    @Override
    void write(Packer pk, Node v, boolean required) throws IOException {

        def map = [id: v.id]
        for (key in v.propertyKeys) {
            map[key] = v.getProperty(key)
        }
        pk.write(map)
    }

    @Override
    Node read(Unpacker u, Node to, boolean required) throws IOException {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }
}
