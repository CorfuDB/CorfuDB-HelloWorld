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

We provide a simple script in ```bin/corfuHelloWorld.sh``` which will deal with the classpath automatically.
The project uses the docopt command line parser for, simple readable command line options.

To get help on options, simply type from the project directory:
```
bin/corfuHelloWorld.sh --help
```

To run the project, type:
```
bin/corfuHelloWorld.sh run
```


