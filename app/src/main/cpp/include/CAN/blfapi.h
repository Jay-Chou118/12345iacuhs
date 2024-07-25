#ifndef INCLUDE_BLFAPI_H
#define INCLUDE_BLFAPI_H

/*  blfapi.h --  declarations for BLF API
    Copyright (C) 2016-2017 Andreas Heitmann

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */

#ifdef HAVE_CONFIG_H
# include "config.h"
#endif

#ifdef HAVE_INTTYPES_H
# include <inttypes.h>
#endif
#ifdef HAVE_STDINT_H
# include <stdint.h>
#endif

#include "blfstream.h"

#ifdef __cplusplus
extern "C" {
#endif

#ifndef GENERIC_WRITE
#define GENERIC_WRITE (1)
#endif

#ifndef GENERIC_READ
#define GENERIC_READ (0)
#endif

#define BL_APPID_UNKNOWN          0
#define BL_APPID_CANALYZER        1
#define BL_APPID_CANOE            2
#define BL_APPID_CANSTRESS        3
#define BL_APPID_CANLOG           4
#define BL_APPID_CANAPE           5
#define BL_APPID_CANCASEXLLOG     6
#define BL_APPID_VLCONFIG         7
#define BL_APPID_PORSCHELOGGER    200
#define BL_APPID_CAETECLOGGER     201
#define BL_APPID_VECTORNETWORKSIMULATOR 202
#define BL_APPID_IPETRONIKLOGGER  203
#define BL_APPID_RT_RK            204
#define BL_APPID_PIKETEC          205

#define BL_COMPRESSION_NONE     0
#define BL_COMPRESSION_SPEED    1
#define BL_COMPRESSION_DEFAULT  6
#define BL_COMPRESSION_MAX      9

#define BL_OBJ_TYPE_UNKNOWN                       0       /* unknown object */
#define BL_OBJ_TYPE_CAN_MESSAGE                   1       /* CAN message object */
#define BL_OBJ_TYPE_CAN_ERROR                     2       /* CAN error frame object */
#define BL_OBJ_TYPE_CAN_OVERLOAD                  3       /* CAN overload frame object */
#define BL_OBJ_TYPE_CAN_STATISTIC                 4       /* CAN driver statistics object */
#define BL_OBJ_TYPE_APP_TRIGGER                   5       /* application trigger object */
#define BL_OBJ_TYPE_ENV_INTEGER                   6       /* environment integer object */
#define BL_OBJ_TYPE_ENV_DOUBLE                    7       /* environment double object */
#define BL_OBJ_TYPE_ENV_STRING                    8       /* environment string object */
#define BL_OBJ_TYPE_ENV_DATA                      9       /* environment data object */
#define BL_OBJ_TYPE_LOG_CONTAINER                10       /* container object */

#define BL_OBJ_TYPE_LIN_MESSAGE                  11       /* LIN message object */
#define BL_OBJ_TYPE_LIN_CRC_ERROR                12       /* LIN CRC error object */
#define BL_OBJ_TYPE_LIN_DLC_INFO                 13       /* LIN DLC info object */
#define BL_OBJ_TYPE_LIN_RCV_ERROR                14       /* LIN receive error object */
#define BL_OBJ_TYPE_LIN_SND_ERROR                15       /* LIN send error object */
#define BL_OBJ_TYPE_LIN_SLV_TIMEOUT              16       /* LIN slave timeout object */
#define BL_OBJ_TYPE_LIN_SCHED_MODCH              17       /* LIN scheduler mode change object */
#define BL_OBJ_TYPE_LIN_SYN_ERROR                18       /* LIN sync error object */
#define BL_OBJ_TYPE_LIN_BAUDRATE                 19       /* LIN baudrate event object */
#define BL_OBJ_TYPE_LIN_SLEEP                    20       /* LIN sleep mode event object */
#define BL_OBJ_TYPE_LIN_WAKEUP                   21       /* LIN wakeup event object */

#define BL_OBJ_TYPE_MOST_SPY                     22       /* MOST spy message object */
#define BL_OBJ_TYPE_MOST_CTRL                    23       /* MOST control message object */
#define BL_OBJ_TYPE_MOST_LIGHTLOCK               24       /* MOST light lock object */
#define BL_OBJ_TYPE_MOST_STATISTIC               25       /* MOST statistic object */

#define BL_OBJ_TYPE_reserved_1                   26
#define BL_OBJ_TYPE_reserved_2                   27
#define BL_OBJ_TYPE_reserved_3                   28

#define BL_OBJ_TYPE_FLEXRAY_DATA                 29       /* FLEXRAY data object */
#define BL_OBJ_TYPE_FLEXRAY_SYNC                 30       /* FLEXRAY sync object */

#define BL_OBJ_TYPE_CAN_DRIVER_ERROR             31       /* CAN driver error object */

#define BL_OBJ_TYPE_MOST_PKT                     32       /* MOST Packet */
#define BL_OBJ_TYPE_MOST_PKT2                    33       /* MOST Packet including original timestamp */
#define BL_OBJ_TYPE_MOST_HWMODE                  34       /* MOST hardware mode event */
#define BL_OBJ_TYPE_MOST_REG                     35       /* MOST register data (various chips)*/
#define BL_OBJ_TYPE_MOST_GENREG                  36       /* MOST register data (MOST register) */
#define BL_OBJ_TYPE_MOST_NETSTATE                37       /* MOST NetState event */
#define BL_OBJ_TYPE_MOST_DATALOST                38       /* MOST data lost */
#define BL_OBJ_TYPE_MOST_TRIGGER                 39       /* MOST trigger */

#define BL_OBJ_TYPE_FLEXRAY_CYCLE                40       /* FLEXRAY V6 start cycle object */
#define BL_OBJ_TYPE_FLEXRAY_MESSAGE              41       /* FLEXRAY V6 message object */

#define BL_OBJ_TYPE_LIN_CHECKSUM_INFO            42       /* LIN checksum info event object */
#define BL_OBJ_TYPE_LIN_SPIKE_EVENT              43       /* LIN spike event object */

#define BL_OBJ_TYPE_CAN_DRIVER_SYNC              44       /* CAN driver hardware sync */

#define BL_OBJ_TYPE_FLEXRAY_STATUS               45       /* FLEXRAY status event object */

#define BL_OBJ_TYPE_GPS_EVENT                    46       /* GPS event object */

#define BL_OBJ_TYPE_FR_ERROR                     47       /* FLEXRAY error event object */
#define BL_OBJ_TYPE_FR_STATUS                    48       /* FLEXRAY status event object */
#define BL_OBJ_TYPE_FR_STARTCYCLE                49       /* FLEXRAY start cycle event object */
#define BL_OBJ_TYPE_FR_RCVMESSAGE                50       /* FLEXRAY receive message event object */

#define BL_OBJ_TYPE_REALTIMECLOCK                51       /* Realtime clock object */
#define BL_OBJ_TYPE_AVAILABLE2                   52       /* this object ID is available for the future */
#define BL_OBJ_TYPE_AVAILABLE3                   53       /* this object ID is available for the future */

#define BL_OBJ_TYPE_LIN_STATISTIC                54       /* LIN statistic event object */

#define BL_OBJ_TYPE_J1708_MESSAGE                55       /* J1708 message object */
#define BL_OBJ_TYPE_J1708_VIRTUAL_MSG            56       /* J1708 message object with more than 21 data bytes */

#define BL_OBJ_TYPE_LIN_MESSAGE2                 57       /* LIN frame object - extended */
#define BL_OBJ_TYPE_LIN_SND_ERROR2               58       /* LIN transmission error object - extended */
#define BL_OBJ_TYPE_LIN_SYN_ERROR2               59       /* LIN sync error object - extended */
#define BL_OBJ_TYPE_LIN_CRC_ERROR2               60       /* LIN checksum error object - extended */
#define BL_OBJ_TYPE_LIN_RCV_ERROR2               61       /* LIN receive error object */
#define BL_OBJ_TYPE_LIN_WAKEUP2                  62       /* LIN wakeup event object  - extended */
#define BL_OBJ_TYPE_LIN_SPIKE_EVENT2             63       /* LIN spike event object - extended */
#define BL_OBJ_TYPE_LIN_LONG_DOM_SIG             64       /* LIN long dominant signal object */

#define BL_OBJ_TYPE_APP_TEXT                     65       /* text object */

#define BL_OBJ_TYPE_FR_RCVMESSAGE_EX             66       /* FLEXRAY receive message ex event object */

#define BL_OBJ_TYPE_MOST_STATISTICEX             67       /* MOST extended statistic event */
#define BL_OBJ_TYPE_MOST_TXLIGHT                 68       /* MOST TxLight event */
#define BL_OBJ_TYPE_MOST_ALLOCTAB                69       /* MOST Allocation table event */
#define BL_OBJ_TYPE_MOST_STRESS                  70       /* MOST Stress event */

#define BL_OBJ_TYPE_ETHERNET_FRAME               71       /* Ethernet frame object */

#define BL_OBJ_TYPE_SYS_VARIABLE                 72       /* system variable object */

#define BL_OBJ_TYPE_CAN_ERROR_EXT                73       /* CAN error frame object (extended) */
#define BL_OBJ_TYPE_CAN_DRIVER_ERROR_EXT         74       /* CAN driver error object (extended) */

#define BL_OBJ_TYPE_LIN_LONG_DOM_SIG2            75       /* LIN long dominant signal object - extended */

#define BL_OBJ_TYPE_MOST_150_MESSAGE             76   /* MOST150 Control channel message */
#define BL_OBJ_TYPE_MOST_150_PKT                 77   /* MOST150 Asynchronous channel message */
#define BL_OBJ_TYPE_MOST_ETHERNET_PKT            78   /* MOST Ethernet channel message */
#define BL_OBJ_TYPE_MOST_150_MESSAGE_FRAGMENT    79   /* Partial transmitted MOST50/150 Control channel message */
#define BL_OBJ_TYPE_MOST_150_PKT_FRAGMENT        80   /* Partial transmitted MOST50/150 data packet on asynchronous channel */
#define BL_OBJ_TYPE_MOST_ETHERNET_PKT_FRAGMENT   81   /* Partial transmitted MOST Ethernet packet on asynchronous channel */
#define BL_OBJ_TYPE_MOST_SYSTEM_EVENT            82   /* Event for various system states on MOST */
#define BL_OBJ_TYPE_MOST_150_ALLOCTAB            83   /* MOST50/150 Allocation table event */
#define BL_OBJ_TYPE_MOST_50_MESSAGE              84   /* MOST50 Control channel message */
#define BL_OBJ_TYPE_MOST_50_PKT                  85   /* MOST50 Asynchronous channel message */

#define BL_OBJ_TYPE_CAN_MESSAGE2                 86   /* CAN message object - extended */

#define BL_OBJ_TYPE_LIN_UNEXPECTED_WAKEUP        87
#define BL_OBJ_TYPE_LIN_SHORT_OR_SLOW_RESPONSE   88
#define BL_OBJ_TYPE_LIN_DISTURBANCE_EVENT        89

#define BL_OBJ_TYPE_SERIAL_EVENT                 90

#define BL_OBJ_TYPE_OVERRUN_ERROR                91   /* driver overrun event */

#define BL_OBJ_TYPE_EVENT_COMMENT                92

#define BL_OBJ_TYPE_WLAN_FRAME                   93
#define BL_OBJ_TYPE_WLAN_STATISTIC               94

#define BL_OBJ_TYPE_MOST_ECL                     95   /* MOST Electrical Control Line event */

#define BL_OBJ_TYPE_GLOBAL_MARKER                96

#define BL_OBJ_TYPE_AFDX_FRAME                   97
#define BL_OBJ_TYPE_AFDX_STATISTIC               98

#define BL_OBJ_TYPE_KLINE_STATUSEVENT            99   /* E.g. wake-up pattern */

#define BL_OBJ_TYPE_CAN_FD_MESSAGE              100   /*CAN FD message object*/

#define BL_OBJ_TYPE_CAN_FD_MESSAGE_64           101   /*CAN FD message object */

#define BL_OBJ_TYPE_ETHERNET_RX_ERROR           102   /* Ethernet RX error object */
#define BL_OBJ_TYPE_ETHERNET_STATUS             103   /* Ethernet status object */

#define BL_OBJ_TYPE_CAN_FD_ERROR_64             104   /*CAN FD Error Frame object */
#define BL_OBJ_TYPE_LIN_SHORT_OR_SLOW_RESPONSE2 105

#define BL_OBJ_TYPE_AFDX_STATUS                 106   /* AFDX status object */
#define BL_OBJ_TYPE_AFDX_BUS_STATISTIC          107   /* AFDX line-dependent busstatistic object */
#define BL_OBJ_TYPE_reserved_4                  108
#define BL_OBJ_TYPE_AFDX_ERROR_EVENT            109   /* AFDX asynchronous error event*/

#define BL_OBJ_TYPE_A429_ERROR                  110   /* A429 error object */
#define BL_OBJ_TYPE_A429_STATUS                 111   /* A429 status object */
#define BL_OBJ_TYPE_A429_BUS_STATISTIC          112   /* A429 busstatistic object */
#define BL_OBJ_TYPE_A429_MESSAGE                113   /* A429 Message*/

#define BL_OBJ_TYPE_ETHERNET_STATISTIC          114   /* Ethernet statistic object */

#define BL_OBJ_TYPE_reserved_5                  115

#define BL_OBJ_TYPE_reserved_6                  116

#define BL_OBJ_TYPE_reserved_7                  117

#define BL_OBJ_TYPE_TEST_STRUCTURE              118   /* Event for test execution flow */

#define BL_OBJ_TYPE_DIAG_REQUEST_INTERPRETATION 119   /* Event for correct interpretation of diagnostic requests */

#define BL_OBJ_TYPE_ETHERNET_FRAME_EX           120   /* Ethernet packet extended object */
#define BL_OBJ_TYPE_ETHERNET_FRAME_FORWARDED    121   /* Ethernet packet forwarded object */
#define BL_OBJ_TYPE_ETHERNET_ERROR_EX           122   /* Ethernet error extended object */
#define BL_OBJ_TYPE_ETHERNET_ERROR_FORWARDED    123   /* Ethernet error forwarded object */

#define BL_OBJ_TYPE_FUNCTION_BUS                124   /* OBSOLETE */
#define BL_OBJ_TYPE_COMMUNICATION_OBJECT        124   /* Communication object in the communication setup */

#define BL_OBJ_TYPE_DATA_LOST_BEGIN             125   /* Data lost begin*/
#define BL_OBJ_TYPE_DATA_LOST_END               126   /* Data lost end*/
#define BL_OBJ_TYPE_WATER_MARK_EVENT            127   /* Watermark event*/
#define BL_OBJ_TYPE_TRIGGER_CONDITION           128   /* Trigger Condition event*/
#define BL_OBJ_TYPE_CAN_SETTING_CHANGED         129   /* CAN Settings Changed object */

#define BL_OBJ_FLAG_TIME_TEN_MICS 1
#define BL_OBJ_FLAG_TIME_ONE_NANS       2

typedef struct __attribute__ ((__packed__)) {
  uint16_t wYear;
  uint16_t wMonth;
  uint16_t wDayOfWeek;
  uint16_t wDay;
  uint16_t wHour;
  uint16_t wMinute;
  uint16_t wSecond;
  uint16_t wMilliseconds;
} SYSTEMTIME_, *PSYSTEMTIME_, *LPSYSTEMTIME_;

typedef struct __attribute__ ((__packed__)) {
  uint32_t mSignature;
  uint32_t mHeaderSize;
  uint32_t mCRC;
  uint8_t  appID;
  uint8_t  dwCompression;
  uint8_t  appMajor;
  uint8_t  appMinor;
  uint64_t fileSize;
  uint64_t uncompressedFileSize;
  uint32_t objectCount;
  uint8_t  appBuild;
  uint8_t  mReserved1;
  uint8_t  mReserved2;
  uint8_t  mReserved3;
  SYSTEMTIME_ mMeasurementStartTime;
  SYSTEMTIME_ mMeasurementEndTime;
  uint8_t  mReserved4[72];
} *LOGG, LOGG_t;

typedef struct __attribute__ ((__packed__)) {
  uint32_t  mSignature;         /*  0: "LOBJ" BL_OBJ_SIGNATURE */
  uint16_t  mHeaderSize;        /*  4: header size */
  uint16_t  mHeaderVersion;     /*  6: 1=VBLObjectHeader, 2=VBLObjectHeader2*/
  uint32_t  mObjectSize;        /*  8: object size*/
  uint32_t  mObjectType;        /* 12: block type */
} VBLObjectHeaderBase;          /* 16 */

typedef struct __attribute__ ((__packed__)) {
  VBLObjectHeaderBase mBase;
  uint32_t  mObjectFlags;       /* 16 */
  uint16_t  mReserved;          /* 20 */
  uint16_t  mObjectVersion;     /* 22 */
  uint64_t  mObjectTimeStamp;   /* 24..31 */
} VBLObjectHeader;              /* 32 */


typedef struct __attribute__ ((__packed__)) {
  VBLObjectHeaderBase base;    /*  0: base */
  uint32_t            compressedflag;    /* 16: compressed data=2 */
  uint32_t            reserved1;         /* 20: reserved */
  uint32_t            deflatebuffersize; /* 24: uncompressed size*/
  uint32_t            reserved2;         /* 28: reserved */
} VBLObjectHeaderBaseLOGG;     /* 32 */

typedef struct __attribute__ ((__packed__)) {
  VBLObjectHeader mHeader;     /*  0: header */
  uint16_t        mChannel;    /* 32: channel*/
  uint8_t         mFlags;      /* 34: flags */
  uint8_t         mDLC;        /* 35: DLC */
  uint32_t        mID;         /* 36: message ID*/
  uint8_t         mData[8];    /* 40 */
} VBLCANMessage;

// CAN dir, rtr, wu & nerr encoded into flags
#define CAN_MSG_DIR( f)          ( uint8_t)(   f & 0x0F)
#define CAN_MSG_RTR( f)          ( uint8_t)( ( f & 0x80) >> 7)
#define CAN_MSG_WU( f)           ( uint8_t)( ( f & 0x40) >> 6)
#define CAN_MSG_NERR( f)         ( uint8_t)( ( f & 0x20) >> 5)
#define CAN_MSG_FLAGS( dir, rtr) ( uint8_t)( ( ( uint8_t)( rtr & 0x01) << 7) | \
                                            ( uint8_t)( dir & 0x0F))
#define CAN_MSG_FLAGS_EXT( dir, rtr, wu, nerr) \
                                 ( uint8_t)( ( ( uint8_t)( rtr  & 0x01) << 7) | \
                                          ( ( uint8_t)( wu   & 0x01) << 6) | \
                                          ( ( uint8_t)( nerr & 0x01) << 5) | \
                                            ( uint8_t)( dir  & 0x0F))
#define CAN_FD_MSG_EDL( f)      (uint8_t) (f & 0x1)
#define CAN_FD_MSG_BRS( f)      (uint8_t) ((f & 0x2) >> 1)
#define CAN_FD_MSG_ESI( f)      (uint8_t) ((f & 0x4) >> 2)


#define  CAN_FD_MSG_FLAGS( edl, brs, esi) \
  ( uint8_t)( ( ( uint8_t)( edl  & 0x01)) | \
  ( ( uint8_t)( brs & 0x01) << 1) | \
  ( uint8_t)( esi  & 0x01) << 2)


/*----------------------------------------------------------------------------
|
| CAN FD objects
|
-----------------------------------------------------------------------------*/
/* HINT: This structure might be extended in future versions! */
typedef struct VBLCANFDMessage_t
{
  VBLObjectHeader mHeader;                     /* object header */
  uint16_t           mChannel;                    /* application channel */
  uint8_t            mFlags;                      /* CAN dir & rtr */
  uint8_t            mDLC;                        /* CAN dlc */
  uint32_t           mID;                         /* CAN ID */
  uint32_t           mFrameLength;                /* message length in ns - without 3 inter frame space bits and by Rx-message also without 1 End-Of-Frame bit */
  uint8_t            mArbBitCount;                /* bit count of arbitration phase */
  uint8_t            mCANFDFlags;                 /* CAN FD flags */
  uint8_t            mValidDataBytes;             /* Valid payload length of mData */
  uint8_t            mReserved1;                  /* reserved */
  uint32_t           mReserved2;                  /* reserved */
  uint8_t            mData[64];                   /* CAN FD data */
} VBLCANFDMessage;

typedef struct VBLCANFDExtFrameData_t{
  uint32_t mBTRExtArb;
  uint32_t mBTRExtData;
  //may be extended in future versions
} VBLCANFDExtFrameData;

#define BLHasExtFrameData(b) (((b)->mExtDataOffset != 0) && ((b)->mHeader.mBase.mObjectSize >= ((b)->mExtDataOffset + sizeof(VBLCANFDExtFrameData))))
#define BLExtFrameDataPtr(b) ((VBLCANFDExtFrameData*)((BYTE*)(b) + (b)->mExtDataOffset))

typedef struct VBLCANFDMessage64_t
{
  VBLObjectHeader mHeader;                     /* object header */
  uint8_t            mChannel;                    /* application channel */
  uint8_t            mDLC;                        /* CAN dlc */
  uint8_t            mValidDataBytes;             /* Valid payload length of mData */
  uint8_t            mTxCount;                    /* TXRequiredCount (4 bits), TxReqCount (4 Bits) */
  uint32_t           mID;                         /* CAN ID */
  uint32_t           mFrameLength;                /* message length in ns - without 3 inter frame space bits */
                                               /* and by Rx-message also without 1 End-Of-Frame bit */
  uint32_t           mFlags;                      /* flags */
  uint32_t           mBtrCfgArb;                  /* bit rate used in arbitration phase */
  uint32_t           mBtrCfgData;                 /* bit rate used in data phase */
  uint32_t           mTimeOffsetBrsNs;            /* time offset of brs field */
  uint32_t           mTimeOffsetCrcDelNs;         /* time offset of crc delimiter field */
  uint16_t            mBitCount;                   /* complete message length in bits */
  uint8_t            mDir;
  uint8_t            mExtDataOffset;
  uint32_t           mCRC;                        /* CRC for CAN */
  uint8_t            mData[64];                   /* CAN FD data */
  VBLCANFDExtFrameData mExtFrameData;
} VBLCANFDMessage64;

typedef struct VBLFileStatistics_t {
  uint32_t  mStatisticsSize;                   /* sizeof (VBLFileStatistics) */
  uint8_t   mApplicationID;                    /* application ID */
  uint8_t   mApplicationMajor;                 /* application major number */
  uint8_t   mApplicationMinor;                 /* application minor number */
  uint8_t   mApplicationBuild;                 /* application build number */
  uint64_t  mFileSize;                         /* file size in bytes */
  uint64_t  mUncompressedFileSize;             /* uncompressed file size in bytes */
  uint32_t  mObjectCount;                      /* number of objects */
  uint32_t  mObVBLFileStatisticsjectsRead;     /* number of objects read */
} VBLFileStatistics;

typedef struct VBLFileStatisticsEx_t {
  uint32_t      mStatisticsSize;               /* sizeof (VBLFileStatisticsEx) */
  uint8_t       mApplicationID;                /* application ID */
  uint8_t       mApplicationMajor;             /* application major number */
  uint8_t       mApplicationMinor;             /* application minor number */
  uint8_t       mApplicationBuild;             /* application build number */
  uint64_t      mFileSize;                     /* file size in bytes */
  uint64_t      mUncompressedFileSize;         /* uncompressed file size in bytes */
  uint32_t      mObjectCount;                  /* number of objects */
  uint32_t      mObjectsRead;                  /* number of objects read */
  SYSTEMTIME_    mMeasurementStartTime;         /* measurement start time */
  SYSTEMTIME_    mLastObjectTime;               /* last object time */
  uint32_t      mReserved[18];                 /* reserved */
} VBLFileStatisticsEx;

typedef struct blf_handle {
  uint32_t            magic;
  LOGG_t              mLOGG;
  DualStream          mDualStream;
  uint32_t            mPeekFlag;
  VBLFileStatisticsEx mStatistics;
  uint32_t            mCANMessageFormat_v1;
  VBLCANFDMessage     mCanFdMsg;
  uint64_t            mbasetime;
} *BLFHANDLE;

/* public functions */
success_t blfPeekObject(BLFHANDLE h, VBLObjectHeaderBase* pBase);
BLFHANDLE  blfCreateFile(FILE *fp, int flag);
success_t blfCloseHandle(BLFHANDLE h);
success_t blfGetFileStatisticsEx(BLFHANDLE h, VBLFileStatisticsEx* pStatistics);
success_t blfReadObject(BLFHANDLE hFile, VBLObjectHeaderBase *pBase);
success_t blfFreeObject(BLFHANDLE h, VBLObjectHeaderBase* pBase);
success_t blfSkipObject(BLFHANDLE h, VBLObjectHeaderBase* pBase);
success_t blfReadObjectSecure(BLFHANDLE h, VBLObjectHeaderBase* pBase,
                              size_t expectedSize);

#ifdef __cplusplus
}
#endif

#endif
