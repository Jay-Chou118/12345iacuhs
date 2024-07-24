LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := MyLibrary

# 定义源代码文件路径
LOCAL_SRC_FILES := \
    native-lib.cpp

LOCAL_LDFLAGS := -llog -lc++ -std=c++17



include $(BUILD_SHARED_LIBRARY)

APP_STL := c++_shared

APP_ABI := arm64-v8a,armeabi-v7a

APP_PLATFORM := android-28