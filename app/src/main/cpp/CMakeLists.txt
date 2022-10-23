cmake_minimum_required(VERSION 3.18.1)

project("alextuner")

set(CMAKE_POLICY_DEFAULT_CMP0077 NEW)

set(ENABLE_FLOAT ON)
set(BUILD_SHARED_LIBS OFF)
set(BUILD_TESTS OFF)
set(DISABLE_FORTRAN ON)
set(ENABLE_THREADS ON)
add_subdirectory(fftw-3.3.10)

add_library(
        alextuner
        SHARED
        AudioAnalyzer.cpp
        jni_bridge.cpp)

find_library(log-lib log)

target_include_directories(alextuner PRIVATE fftw-3.3.10/api)

target_link_libraries(
        alextuner
        fftw3f
        ${log-lib}
)
