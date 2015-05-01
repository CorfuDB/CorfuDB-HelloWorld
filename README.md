# CorfuDB example project

This repository contains a sample project for CorfuDB, which showcases some basic features of CorfuDB.
The main source file is located [here](src/main/java/org/corfudb/example/CorfuHelloWorld.java). The code
is well commented and will walk you through how to use CorfuDB.

As you navigate through the source code, the [javadoc](http://corfudb.github.io/CorfuDB/javadoc/) may
help you, and there is also a [wiki](https://github.com/CorfuDB/CorfuDB/wiki) to guide you through
the architecture of CorfuDB.

To build your own CorfuDB project, simply fork this project.

## Building the Project

This project uses Maven as a build system. To build this project, you will need to have Java 8 and Maven installed.
Also, until CorfuDB is submitted to Maven central, you will have to build CorfuDB on your system somewhere first
using ```mvn install```, before building this project.

On a Ubuntu 14.02 machine, run:
```
# Install prerequisites
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer maven git
wget http://people.apache.org/~jfarrell/thrift/0.9.2/contrib/ubuntu/thrift-compiler_0.9.2_amd64.deb -O /tmp/thrift-compiler.deb
sudo dpkg -i /tmp/thrift-compiler.deb
# Clone CorfuDB and build
git clone https://github.com/CorfuDB/CorfuDB.git
cd CorfuDB
mvn install
# Clone this project and build
cd ..
git clone https://github.com/CorfuDB/CorfuDB-HelloWorld.git
cd CorfuDB-HelloWorld
mvn install
```