#ifndef INCLUDE_BLFREADER_H
#define INCLUDE_BLFREADER_H

/*  blfreader.h --  declarations for blfReader
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

#include <stdint.h>
#include <stdio.h>


#ifdef __cplusplus
extern "C"{
#endif

//#include "dbctypes.h"
//#include "measurement.h"

/* CAN message type */
typedef struct {
  struct {
    //time_t tv_sec;
    uint32_t tv_sec;
    uint32_t tv_nsec;
  } t; /* time stamp */
  uint8_t   bus;     /* can bus */
  uint32_t  id;      /* numeric CAN-ID */
  uint8_t   dlc;
  uint8_t   byte_arr[64];
} canMessage_t;

/* message received callback function */
typedef void (* msgRxCb_t)(canMessage_t *message, void *cbData);

/* blfRead function */
void blfReader_processFile(FILE *fp, msgRxCb_t msgRxCb, void *cbData);

#ifdef __cplusplus
}
#endif

#endif
