This is a PoC (Proof of Concept) like small project to integrate [Neo4j](http://www.neo4j.org) with [Ratpack](http://www.ratpack-framework.org). <https://github.com/ratpack/example-ratpack-gradle-groovy-app> acts as a template for this project.

The primary goal is allow remote access to Neo4j via (Cypher)[http://docs.neo4j.org/chunked/stable/cypher-query-lang.html].

## Features

The list below is a list of features, planned stuff is in parenthesis.

* execute Cypher via HTTP POST and GET
* support parameterized Cypher
* (transactional Cypher Endpoint)
* (use [message pack](http://msgpack.org/) optionally as serialization format)
* (provide multiple output formats e.g. JSON, XML, CSV)
* (provide a interface to cancel currently running cypher queries)


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

Result is rendered in JSON as outlined on (Neo4j REST API)[http://docs.neo4j.org/chunked/stable/rest-api-cypher.html].

#### Cypher via HTTP POST

Sending Cypher queries using HTTP POST works just like in the standard (Neo4j REST API)[http://docs.neo4j.org/chunked/stable/rest-api-cypher.html]

Rest of this document is copy & pasted from <https://github.com/ratpack/example-ratpack-gradle-groovy-app>

---

It is also using the Ratpack Gradle plugin as the development environment.

## Getting Started

*To run this app, you need Java 7* (to be fixed)

Check this project out, cd into the directory and run:

    ./gradlew run

This will start the ratpack app. In your browser go to <http://localhost:5050>.

The Gradle Ratpack plugin builds on the Gradle Application plugin. This means it's easy to create a standalone
distribution for your app.

Run:

    ./gradlew installApp
    cd build/install/groovy-web-console/
    bin/groovy-web-console

Your app should now be running. See [The Application Plugin](http://gradle.org/docs/current/userguide/application_plugin.html) chapter in the [Gradle User Guide](http://www.gradle.org/docs/current/userguide/userguide.html) for more on what
the Gradle application plugin can do for you.

## Development time reloading

One of the key Ratpack features is support for runtime reloading. The `src/ratpack/ratpack.groovy` file (which defines
the request routes and handlers) can be changed at runtime.

Furthermore, full reloading of supporting classes (i.e. `src/main/groovy`) is enabled via
[SpringSource's SpringLoaded](https://github.com/SpringSource/spring-loaded) library.

See `src/ratpack/ratpack.groovy` and `src/main/groovy/groovywebconsole/ReloadingThing.groovy` for instructions on how to test
the SpringLoaded based reloading.

## IDEA integration

The Ratpack Gradle plugin has special support for [IntelliJ IDEA](http://www.jetbrains.com/idea/download/). To open the project in IDEA, run:

    ./gradlew idea

If you have the `idea` command line tool installed you can then run:

    idea groovy-web-console.ipr

In the “Run” menu, you will find a run configuration for launching the Ratpack app from within your IDE. Hot reloading
is supported in this mode. See `src/main/groovy/groovywebconsole/ReloadingThing.groovy` for details.

## Configuring a port

### When running via Gradle

Add

    jvmArgs "-Dratpack.port=8080"

To your ```run``` closure, for example:

    run {
        jvmArgs "-Dratpack.port=8080"
        systemProperty "ratpack.reloadable", "true"
    }

### When running the built application/jar

Set the JVM property using the ```JAVA_OPTS``` shell variable.  For example, in the Bash shell:

    export JAVA_OPTS=-Dratpack.port=8080

## More on Ratpack

The published [Ratpack Manual](http://www.ratpack-framework.org/manual/snapshot/) is currently minimal, but contributions are welcome.

More information, including issue tracker and support forum, is available on the [Ratpack Website](http://www.ratpack-framework.org).

You can also check out the source @ https://github.com/ratpack/ratpack/tree/master or open this project in IDEA and
dig through the source there.
