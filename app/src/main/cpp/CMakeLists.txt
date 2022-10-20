cmake_minimum_required(VERSION 3.18.1)

project("alextuner")

add_library(
        alextuner
        SHARED
        AudioAnalyzer.cpp
        jni_bridge.cpp)

find_library(log-lib log)

target_link_libraries(
        alextuner
        ${log-lib}
)
