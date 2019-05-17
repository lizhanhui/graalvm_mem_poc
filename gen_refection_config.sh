#!/usr/bin/env bash
java -agentlib:native-image-agent=trace-output=./trace-file.json -jar client/target/client.jar
native-image-configure process-trace --output-dir=. ./trace-file.json
