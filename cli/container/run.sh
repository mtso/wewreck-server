#!/bin/bash
export "$(cat /config/java_opts)"

bash wehack/bin/wehack server /config/wehack.yml
