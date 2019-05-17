#!/usr/bin/env bash
cp client/src/main/c/simple_data.h /usr/local/include

if test $? != 0 ; then
    echo "Copy header file failed"
    exit -1;
fi

native-image --server-shutdown-all

mvn clean package

native-image --shared                             \
             -H:Path=./build                      \
             -R:+PrintGC                          \
             -R:+VerboseGC                        \
             -jar ./client/target/client.jar      \
             -H:CLibraryPath=./client/src/main/c  \
             -Dio.netty.noUnsafe=true