#!/bin/bash
# This needs to be run from inside the docker image. ONLY RUN ON LOCAL MACHINE
cat /etc/resolv.conf | sed 's/^search/search we\.pay c\.mythic\-crane\-708\.internal/g'  > /tmp/123 && echo y | cp -i /tmp/123 /etc/resolv.conf
export ENVIRONMENT_NAME=poc
bash /jar/cli/container/run.sh
