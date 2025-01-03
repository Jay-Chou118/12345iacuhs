LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := MyLibrary

# 添加头文件查找路径
LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/include \
    $(LOCAL_PATH)/include/CAN \
    $(LOCAL_PATH)/src/minilzo

LOCAL_LDFLAGS += -L$(LOCAL_PATH)/../jniLibs/arm64-v8a

# 定义源代码文件路径
LOCAL_SRC_FILES := \
    native-lib.cpp \
    src/CAN/blfapi.c \
    src/CAN/blfwriter.c \
    src/CAN/blfparser.c \
    src/CAN/blfstream.c \
    src/minilzo/minilzo.c

LOCAL_LDFLAGS += -lmodule_mican -llog -lc++ -std=c++17 -lz




include $(BUILD_SHARED_LIBRARY)

APP_STL := c++_shared

APP_ABI := arm64-v8a

APP_PLATFORM := android-28