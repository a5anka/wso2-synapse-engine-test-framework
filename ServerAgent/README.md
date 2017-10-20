# WSO2 Synapse Engine Test Framework - Synapse Server Agent

Synapse server agent is responsible for running the synapse server with required configuration changes. Currently 
following operations are supported through the agent.
- uploading synapse configuration
- starting the synapse server with uploaded configuration
- stopping the synapse server

## Installation 

```sh
mvn package
```

## Running

```sh
java -jar -Dsynapse.home=/path/to/synapse/home -Dtransports.netty.conf=conf/netty-transports.yml target/ServerAgent-1
.0-SNAPSHOT.jar
```

## Preparing Synapse Home

You will have to perform following steps to prepare the synapse home for running test framework
1. Copy the ServerAgent/src/main/resources/synapse-stop.sh to $SYNAPSE_HOME/bin folder
