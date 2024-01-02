cmake_minimum_required(VERSION 3.18.1)

project("alextuner")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_subdirectory(q_lib)

add_library(
        alextuner
        SHARED
        AudioAnalyzer.cpp
        jni_bridge.cpp)

target_include_directories(alextuner PRIVATE libq)

target_link_libraries(
        alextuner
        libq
)
