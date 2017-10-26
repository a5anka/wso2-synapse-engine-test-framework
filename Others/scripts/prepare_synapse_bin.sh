#!/bin/bash

git clone -b test-framework-dev https://github.com/wso2/wso2-synapse.git --depth 1
pushd wso2-synapse/
mvn clean install -Dmaven.test.skip=true
popd
unzip -o wso2-synapse/modules/distribution/target/synapse*-bin.zip
SYNAPSE_HOME=$(find . -maxdepth 1 -name "synapse*" -type d)
wget --directory-prefix=$SYNAPSE_HOME/bin/ https://raw.githubusercontent.com/a5anka/wso2-synapse-engine-test-framework/master/ServerAgent/src/main/resources/synapse-stop.sh
zip -r ${SYNAPSE_HOME}-bin.zip $SYNAPSE_HOME
