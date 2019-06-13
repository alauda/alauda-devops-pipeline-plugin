#!/bin/bash

set -e

TOKEN_ADMIN="c6fc6a59033d1144fec5a565cb2b6796"
ADDRESS="http://localhost:30251"
export JENKINS_CONTAINER=$(docker ps | grep jenkins |  head -n1 | awk '{print $1;}')
echo $JENKINS_CONTAINER
if [ "$JENKINS_CONTAINER" == "" ]; then
  echo "Jenkins container not found"
  exit 1;
fi
mvn clean install -DskipTests
docker cp target/alauda-devops-sync.hpi $JENKINS_CONTAINER:/var/jenkins_home/plugins
./restart.sh
