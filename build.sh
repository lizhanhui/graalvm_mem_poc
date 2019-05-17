#!/usr/bin/env bash
cp -r client/src/main/c/poc /usr/local/include/

if test $? != 0 ; then
    echo "Copy header file failed"
    exit -1;
fi

native-image --server-shutdown-all

mvn clean package

if [ -d build ]; then
	rm -fr build
fi
mkdir build

native-image --shared                             \
             -H:Path=./build                      \
             -R:+PrintGC                          \
             -R:+VerboseGC                        \
             -jar ./client/target/client.jar      \
             -H:CLibraryPath=./client/src/main/c  \
             -Dio.netty.noUnsafe=true