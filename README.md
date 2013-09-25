[![Build Status](https://secure.travis-ci.org/sarmbruster/neo4j-ratpack.png)](http://travis-ci.org/sarmbruster/neo4j-ratpack)

This is a PoC (Proof of Concept) like small project to integrate [Neo4j](http://www.neo4j.org) with [Ratpack](http://www.ratpack-framework.org). <https://github.com/ratpack/example-ratpack-gradle-groovy-app> acts as a template for this project.

The primary goal is allow remote access to Neo4j via [Cypher](http://docs.neo4j.org/chunked/milestone/cypher-query-lang.html).

## Features

The list below is a list of features, planned stuff is in parenthesis.

* execute Cypher via HTTP POST and GET
* support parameterized Cypher
* (transactional Cypher Endpoint)
* use [message pack](http://msgpack.org/) optionally as serialization format
* provide multiple output formats e.g. JSON, XML, CSV
* provide a interface to cancel currently running cypher queries


### Configuration

tbd

### Cypher endpoint

The regular, non-transactional Cypher endpoint can be accessed using the path `/cypher`. Access is either possible via HTTP GET OR POST.

#### Cypher via HTTP GET

When sending Cypher queries using GET, the URL parameter `query` needs to contain the Cypher query in url encoded way. All other URL parameters are used as Cypher parameters.

example:

```
http://localhost:5050/cypher?query=start%20n%3Dnode(*)%20return%20n
```

Result is rendered in JSON as outlined on [Neo4j REST API](http://docs.neo4j.org/chunked/stable/rest-api-cypher.html).

#### Cypher via HTTP POST

Sending Cypher queries using HTTP POST works just like in the standard [Neo4j REST API](http://docs.neo4j.org/chunked/stable/rest-api-cypher.html)


## Getting Started

*To run this app, you need Java 7* (to be fixed)

Check this project out, cd into the directory and run:

    ./gradlew run

This will start the neo4j-ratpack app. In your browser go to <http://localhost:5050>.

## More on Ratpack

The published [Ratpack Manual](http://www.ratpack-framework.org/manual/snapshot/) is currently minimal, but contributions are welcome.

More information, including issue tracker and support forum, is available on the [Ratpack Website](http://www.ratpack-framework.org).

You can also check out the source @ https://github.com/ratpack/ratpack/tree/master or open this project in IDEA and
dig through the source there.
