cmake_minimum_required(VERSION 3.10.2)
project("blurlib")

add_library(blurlib SHARED
        NativeBlur.cpp
        image.cpp
        )

target_link_libraries(blurlib -ljnigraphics)