#!/usr/bin/env bash

if test -d $HOME/include; then
    mkdir -p $HOME/include
fi

cp -r client/src/main/c/poc $HOME/include/

if test $? != 0 ; then
    echo "Copy header file failed"
    exit -1;
fi

native-image --server-shutdown-all

mvn clean package

if [ ! -d build ]; then
    mkdir build
fi

native-image --shared                             \
             -H:ReflectionConfigurationFiles=reflect-config.json \
             -H:Path=./build                      \
             -R:+PrintGC                          \
             -R:+VerboseGC                        \
             -jar ./client/target/client.jar      \
             -H:CLibraryPath=./client/src/main/c  \
	     -H:+ReportExceptionStackTraces       \
	     --allow-incomplete-classpath         \
             -Dio.netty.noUnsafe=true             \
	     --initialize-at-build-time

if test $? = 0; then
    cp build/client.so build/libclient.so
fi
