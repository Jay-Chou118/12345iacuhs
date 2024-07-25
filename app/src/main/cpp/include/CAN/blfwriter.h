#ifndef INCLUDE_BLFWRITE_H
#define INCLUDE_BLFWRITE_H

#include <stdint.h>
#include <stdio.h>
#include "blfapi.h"

//#include "dbctypes.h"
//#include "measurement.h"

#ifdef __cplusplus
extern "C"{
#endif

typedef struct CANFrameRaw_t {
  uint32_t can_type;
  uint32_t can_id;
  uint16_t can_dlc;
  uint16_t can_channel;
  uint64_t time_stamp;
  uint8_t  data[64];
}CANFrameRaw;

/* blfRead function */
BLFHANDLE Initiate(const char* blf_name);
int WriteCanFrame(BLFHANDLE hFile, CANFrameRaw* can_frame);
int Finish(BLFHANDLE hFile) ;


#ifdef __cplusplus
}
#endif

#endif

