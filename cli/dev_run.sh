#!/bin/bash
this_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
export "$(cat $this_dir/../config/java_opts)"
./gradlew clean installDist
bash build/install/wehack/bin/wehack server config/wehack.yml
