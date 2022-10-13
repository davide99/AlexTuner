cmake_minimum_required(VERSION 3.18.1)

project("alextuner")

add_library(
        alextuner
        SHARED
        AudioRecorder.cpp
        jni_bridge.cpp)

find_library(log-lib log)

find_package(oboe REQUIRED CONFIG)

target_link_libraries(
        alextuner
        ${log-lib}
        oboe::oboe
        )