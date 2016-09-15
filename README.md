# Corfu example project

This repository contains a sample project for Corfu, which showcases some basic features of CorfuDB.


As you navigate through the source code, the [javadoc](http://corfudb.github.io/CorfuDB/javadoc/) may
help you, and there is also a [wiki](https://github.com/CorfuDB/CorfuDB/wiki) to guide you through
the architecture of CorfuDB.

To build your own CorfuDB project, simply fork this project.

## Building the Project

This project uses Maven as a build system. To build this project, you will need to have Java 8 and Maven installed.

On a Ubuntu 14.02 machine, run:
```
# Install prerequisites
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer maven
# Clone this project and build
cd ..
git clone https://github.com/CorfuDB/CorfuDB-HelloWorld.git
cd CorfuDB-HelloWorld
mvn install
```

# Running the project

We provide a simple script in ```bin/airline``` which will deal with the classpath automatically.
The project uses the docopt command line parser for, simple readable command line options.

To get help on options, simply type from the project directory:
```
bin/airline --help
```

## Make certain that the Corfu server is running

This example assumes that both the Corfu server and Corfu client are
running on the same machine.  If they run on different machines, use
the `-c server-host:port-number` syntax on the client command line to
specify where the server is located.

To run the server to listen on TCP port 9000 (the default port), use:

    /path/to/your/corfu-source-top-directory/bin/corfu_server -s -m 9000

* `-s` = This will run the Corfu server in "single server" mode.
* `-m` = The server will be running in "in-memory" mode (i.e., non-persistent)

## Let's play with airplanes!

If you just started the `corfu_server` process, then the database is
empty.  When we view the database's contents, we won't see much.

    $ ./bin/airline view-database
    view-database using db testdb and runtime args localhost:9000
    16:21:44.501 INFO  [main] o.c.r.CorfuRuntime - Connecting to Corfu server instance, layout servers=[localhost:9000]
    Planes:
    
    Flight schedules:

Let's add a couple of airplanes.

    % ./bin/airline add-plane A319 small7
    add-plane using db testdb and runtime args localhost:9000
    16:22:34.312 INFO  [main] o.c.r.CorfuRuntime - Connecting to Corfu server instance, layout servers=[localhost:9000]
    Airplane small7 (A319) added.

    % ./bin/airline add-plane B747 big3
    add-plane using db testdb and runtime args localhost:9000
    16:23:00.042 INFO  [main] o.c.r.CorfuRuntime - Connecting to Corfu server instance, layout servers=[localhost:9000]
    Airplane big3 (B747) added.

Excellent.  If we use the `./bin/airline view-database` command again,
we'll see our two new airplanes now.

    [...]
    Planes:
    small7 (A319)
    big3 (B747)
    
    Flight schedules:

We haven't added any flight schedules yet.  Let's do that.

    % ./bin/airline add-flight big3 BOS '2016-09-15T12:00:00' SFO '2016-09-15T14:35:00'
    add-flight using db testdb and runtime args localhost:9000
    16:28:48.785 INFO  [main] o.c.r.CorfuRuntime - Connecting to Corfu server instance, layout servers=[localhost:9000]
    Flight FL 1 BOS-SFO dep 2016-09-15T12:00 arr 2016-09-15T14:35 [big3 (B747)] added

Note that the daily flight number, `FL 1`, has been assigned automatically
by the system.  Let's add another flight with the same plane and with
an overlapping time window.

    % ./bin/airline add-flight big3 MSP '2016-09-15T13:05:00' NRT '2016-09-16T17:05:00'
    add-flight using db testdb and runtime args localhost:9000
    16:30:58.450 INFO  [main] o.c.r.CorfuRuntime - Connecting to Corfu server instance, layout servers=[localhost:9000]
    Plane big3 (B747) is already in use during this timeframe!

Oops.  We won't do that again.

Let's add two more flights, then look at the flight schedule.

    % ./bin/airline add-flight small7 JFK '2016-09-14T06:01:00' ORD '2016-09-14T08:12:00'
    [...]
    
    % ./bin/airline add-flight big3 SFO '2016-09-15T15:00:00' SEA '2016-09-15T17:22:00'
    [...]
    
    % ./bin/airline view-database
    view-database using db testdb and runtime args localhost:9000
    16:32:42.799 INFO  [main] o.c.r.CorfuRuntime - Connecting to Corfu server instance, layout servers=[localhost:9000]
    Planes:
    small7 (A319)
    big3 (B747)
    
    Flight schedules:
    Schedule for 2016-09-14
    FL 1 JFK-ORD dep 2016-09-14T06:01 arr 2016-09-14T08:12 [small7 (A319)]
    Schedule for 2016-09-15
    FL 1 BOS-SFO dep 2016-09-15T12:00 arr 2016-09-15T14:35 [big3 (B747)]
    FL 2 SFO-SEA dep 2016-09-15T15:00 arr 2016-09-15T17:22 [big3 (B747)]
