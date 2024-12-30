// pch.h: 这是预编译标头文件。
// 下方列出的文件仅编译一次，提高了将来生成的生成性能。
// 这还将影响 IntelliSense 性能，包括代码完成和许多代码浏览功能。
// 但是，如果此处列出的文件中的任何一个在生成之间有更新，它们全部都将被重新编译。
// 请勿在此处添加要频繁更新的文件，这将使得性能优势无效。

#ifndef PCH_H
#define PCH_H


#include <iostream>
#include <vector>
#include <list>
#include <fstream>
//#include "utils.h"
#ifdef WIN32
#define HILAYER extern "C"  __declspec(dllexport)
#elif __GNUC__
#define HILAYER extern "C"
#endif

#include <android/log.h> //添加头文件
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


HILAYER int  InitModule(uint8_t ifcheck);

HILAYER int  GetModuleInfo(unsigned char* moduleInfo);

HILAYER int  SelectChannel(unsigned int ChannelIndex, unsigned int ChannelType);

HILAYER int  CANFDSetBaudRate(unsigned int ChannelIndex, unsigned int arbBitrate, unsigned int arbSjw, unsigned int arbTseg1, unsigned int arbTseg2, unsigned int dataBitrate, unsigned int dataSjw, unsigned int dataTseg1, unsigned int dataTseg2);

HILAYER int  CANOnBus(unsigned int ChannelIndex);

HILAYER int  LINOnBus(unsigned int ChannelIndex);

HILAYER int  DeSelectChannel(unsigned int ChannelIndex);

HILAYER int  CANOffBus();

HILAYER int  LINOffBus();

HILAYER int LINMasterSendOn();

HILAYER int LINMasterSendOff();

HILAYER int ResetLINConfig1();

HILAYER int ResetLINConfig2();

HILAYER int LoadLINSendConfigOnce(const char* content);

HILAYER int LoadLINSendConfig(const char* content);

HILAYER int LoadE2EConfig(const char* content);

HILAYER int LoadE2EConfigLIN1(const char* content);

HILAYER int LoadE2EConfigLIN2(const char* content);

HILAYER int  SetBaudRate(unsigned int ChannelIndex, unsigned int Baudrate);

HILAYER int  SetBaudRateCANFD(unsigned int ChannelIndex, unsigned int arbitration_bitRate, unsigned int data_bitrate);

//HILAYER int  ReceCanFDMsg(CanMessage* canFDMsg,uint8_t ifReadErrMsg);
//
//HILAYER int  ReceLinMsg(LinMessage* LinMsg);
//
//HILAYER int  ReceCanFDMsgMulti(CanMessage* canFDMsg, int num = 16);

HILAYER int  SendCanFDMsg(const char* canFDMsg);

HILAYER int  DeInitModule();

/**
 * @brief 配置周期发送
 */
HILAYER int LoadPeriodSendConfig(const char* content);

/**
 * @brief 开启周期发送
 */
HILAYER int StartPeriodSend();

/**
 * @brief 关闭周期发送
 */
HILAYER int StopPeriodSend();

HILAYER int WriteDeviceConfig();

HILAYER int ReadDeviceConfig(uint8_t index,char* content);

HILAYER int ReadDeviceVersion(uint8_t index,char* content);

HILAYER int ReadDeviceBuildTime(uint8_t index,char* content);

HILAYER int ReadDeviceCpuLoad(uint8_t index,uint8_t * content);

HILAYER int Flash();

//HILAYER int ReceErrorMsg(CANErrorMsg_t* msg);
//
//HILAYER int GetBusStatus(uint8_t busid,BusStatus_t* status);


HILAYER int sayHello();

#endif //PCH_H
