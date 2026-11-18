cmake_minimum_required(VERSION 3.22.1)
project("rkllm_demo")

set(CMAKE_CXX_STANDARD 17)

# Add include directory
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Set library path - now using app/jniLibs
set(RKLLM_LIB_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# Create JNI library
add_library(
    rkllm_jni
    SHARED
    rkllm_jni.cpp
)

# Link libraries using full paths
target_link_libraries(
    rkllm_jni
    ${RKLLM_LIB_PATH}/librkllmrt.so
    ${RKLLM_LIB_PATH}/libomp.so
    log
    android
)
