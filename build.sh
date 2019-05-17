#!/usr/bin/env bash
native-image --server-shutdown-all
native-image --shared \
             -R:+PrintGC \
             -R:+VerboseGC \
             -jar client/target/client.jar \
             -H:CLibraryPath=./client/src/main/c \
             -Dio.netty.noUnsafe=true