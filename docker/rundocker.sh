#!/bin/bash
scriptdir=$(dirname "$0")
dockerfile="${scriptdir}/Dockerfile"
if [ ! -f "${dockerfile}" ]; then
  echo "Can't find ${dockerfile}, aborting."
  exit 1
fi
mvn -f ${scriptdir}/../ clean package && \
  (docker kill $(docker ps -q) || true) 2>/dev/null && \
  docker container rm /sqreen-agent-app
  docker container run -i -p 8080:8080  --name sqreen-agent-app com.sqreen/sqreen-monitoring-agent:1.0-SNAPSHOT