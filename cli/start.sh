#!/bin/bash
docker stop wehack
docker rm wehack
this_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd) 
docker run --name wehack \
    -p 8000:8000 \
    -e WEPAY_SERVICE_NAME=wehack \
    -e ENVIRONMENT_NAME=poc \
    -e DISABLE_BAELISH_CLIENT=true \
    -v "$this_dir/../config":"/jar/config" -it \
    wehack:latest \
    bash cli/container/dev_run_docker.sh
