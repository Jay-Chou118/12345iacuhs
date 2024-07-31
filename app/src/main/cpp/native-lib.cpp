#include <jni.h>
#include <string>
#include <iostream>
#include <chrono>
#include <iomanip>
#include <sstream>
#include <fstream>
#include <android/log.h>

#include "blfwriter.h"
#include "minilzo.h"

#define LOG_TAG "MICAN_CPP" //定义TAG
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static std::string formatTime();

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_testcdc_MyService_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_testcdc_MyService_testCreateFile(JNIEnv *env, jobject obj, jstring dirPath) {
    const char *str = env->GetStringUTFChars(dirPath, nullptr);
    std::string filePath = std::string(str) + "/" + formatTime() + ".blf";
    std::ofstream file(filePath);
    if(file.is_open())
    {
        LOGI("file is open");
        file << " hhh i am from c++ ";
        file.close();
    } else{
        LOGW("file can not open");
    }

    // 处理字符串
    env->ReleaseStringUTFChars(dirPath, str);
}


std::string formatTime()
{
    // 获取当前时间点
    auto now = std::chrono::system_clock::now();

    // 转换为time_t格式
    std::time_t now_c = std::chrono::system_clock::to_time_t(now - std::chrono::hours(24));

    // 转换为tm结构
    std::tm* now_tm = std::localtime(&now_c);

    // 输出格式化时间戳
    std::stringstream ss;
    ss << std::put_time(now_tm, "%Y-%m-%d_%H_%M_%S");
    return ss.str();
}

static BLFHANDLE recordFile = NULL;

extern "C" JNIEXPORT void JNICALL
Java_com_example_testcdc_MyService_startRecord(JNIEnv *env, jobject obj,jstring filePath)
{
    LOGI("========startRecord=========");
    const char *path = env->GetStringUTFChars(filePath, nullptr);
    if(recordFile)
    {
        LOGW("is recording, please stop first");
        return;
    }
    LOGI("%s",path);
    recordFile = Initiate(path);
    // 处理字符串
    env->ReleaseStringUTFChars(filePath, path);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_testcdc_MyService_stopRecord(JNIEnv *env, jobject obj)
{
    LOGI("========stopRecord=========");
    if(recordFile == NULL)
    {
        LOGW("please start first");
        return;
    }
    Finish(recordFile);
    recordFile = NULL;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_testcdc_MyService_record(JNIEnv *env, jobject obj, jlong timestamp,
                                             jshort can_channel,jshort can_dlc,jint can_id,
                                             jint can_type,jbyteArray data ) {
//    LOGI("========record=========");
    if(recordFile)
    {
        jbyte *bytes = env->GetByteArrayElements(data, NULL);
        jsize length = env->GetArrayLength(data);
        CANFrameRaw canFrameRaw = {0};
        canFrameRaw.time_stamp = timestamp *1000;
        canFrameRaw.can_channel = can_channel;
        canFrameRaw.can_dlc = can_dlc;
        canFrameRaw.can_id = can_id;
        canFrameRaw.can_type = can_type;
        memcpy(canFrameRaw.data,bytes,length);
        auto ret = WriteCanFrame(recordFile,&canFrameRaw);
//        LOGI("WriteCanFrame is %d",ret );
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_testcdc_MyService_decompress(JNIEnv *env, jclass obj,
                                              jbyteArray compressDataBuffer, jbyteArray unCompressDataBuffer) {

    jbyte *source = env->GetByteArrayElements(compressDataBuffer, JNI_FALSE);
    lzo_uint length = (lzo_uint)env->GetArrayLength(compressDataBuffer);
    jbyte * dest = env->GetByteArrayElements(unCompressDataBuffer, JNI_FALSE);
    lzo_uint destNum = (lzo_uint) env->GetArrayLength(unCompressDataBuffer);
//    LOGW("length %lu destNum: %lu",length,destNum);
    auto ret = lzo1x_decompress_safe((const unsigned char*) source,length,(unsigned char*)dest,&destNum, nullptr);


    env->ReleaseByteArrayElements(compressDataBuffer, source, JNI_FALSE);
    env->ReleaseByteArrayElements(unCompressDataBuffer, dest, JNI_FALSE);
    if (ret != LZO_E_OK)
    {
        LOGW("ret is %d",ret);
        return -1;
    }
//    for(int i = 0;i< destNum;i++)
//    {
//        LOGW(" %d",dest[i]);
//    }
    return 0;
}