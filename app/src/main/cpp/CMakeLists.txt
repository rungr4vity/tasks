cmake_minimum_required(VERSION 3.22.1)
project("weatherkeys")

add_library(
        keys
        SHARED
        keys.cpp        # ← no path prefix, just the filename
)

find_library(log-lib log)
target_link_libraries(keys ${log-lib})