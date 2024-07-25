#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include <time.h>
#include "blfapi.h"
#include "blfparser.h"
#include "blfwriter.h"

#define WRITE_BUF_SIZE (1024*1024*5)
static int canfd_dlc[16] = {0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64};

static int len2dlc(int len)
{
	for (int i = 0; i < 16; i++)
	{
		if (canfd_dlc[i] == len)
		{
			return i;
		}
	}
	return 0;
}

static void logg_obj_init(BLFHANDLE hFile)
{
	hFile->mLOGG.mSignature = BL_LOGG_SIGNATURE;
	hFile->mLOGG.mHeaderSize = sizeof(hFile->mLOGG);
	hFile->mLOGG.appID = BL_APPID_CANOE;
	hFile->mLOGG.appMajor = 1;
	hFile->mLOGG.appMinor = 0;
	hFile->mLOGG.appBuild = 1;
	hFile->mLOGG.objectCount = 0;
	hFile->mLOGG.fileSize = 0;
	hFile->mLOGG.uncompressedFileSize = 0;
	
	time_t tt = time(NULL);
	struct tm *startTime = localtime(&tt);
	hFile->mLOGG.mMeasurementStartTime.wYear = startTime->tm_year+1900;
	hFile->mLOGG.mMeasurementStartTime.wMonth = startTime->tm_mon+1;
	hFile->mLOGG.mMeasurementStartTime.wDayOfWeek = startTime->tm_wday;
	hFile->mLOGG.mMeasurementStartTime.wDay = startTime->tm_mday;
	hFile->mLOGG.mMeasurementStartTime.wHour = startTime->tm_hour;
	hFile->mLOGG.mMeasurementStartTime.wMinute = startTime->tm_min;
	hFile->mLOGG.mMeasurementStartTime.wSecond = startTime->tm_sec;
	hFile->mLOGG.mMeasurementStartTime.wMilliseconds = 0;

	//init_ = init_ && BLSetWriteOptions(file_, 6, 0);
	hFile->mLOGG.dwCompression = BL_COMPRESSION_DEFAULT;
}

static int logg_obj_finish(BLFHANDLE hFile)
{
	VBLObjectHeaderBaseLOGG lobjBase;
	lobjBase.base.mSignature = BL_OBJ_SIGNATURE;
	lobjBase.base.mHeaderVersion = 1;
	lobjBase.base.mHeaderSize = sizeof(lobjBase.base);
	lobjBase.base.mObjectSize = hFile->mDualStream.memStream.mBytesLeft + sizeof(lobjBase);
	lobjBase.base.mObjectType = BL_OBJ_TYPE_LOG_CONTAINER;
	lobjBase.compressedflag = 2;
	lobjBase.deflatebuffersize = hFile->mDualStream.memStream.mBytesLeft;

	if (blfCompress(&lobjBase, &hFile->mDualStream.memStream.mBuffer, &lobjBase.deflatebuffersize, hFile->mLOGG.dwCompression) < 0)
	{
		return -1;
	}

	hFile->mLOGG.fileSize += lobjBase.base.mObjectSize;
	int pad = (lobjBase.base.mObjectSize - sizeof(VBLObjectHeaderBaseLOGG)) & 3;
	if (pad != 0)
	{
		hFile->mLOGG.fileSize += pad;
	}
	hFile->mLOGG.uncompressedFileSize += (lobjBase.deflatebuffersize + sizeof(VBLObjectHeaderBaseLOGG));

	fwrite((char *)&lobjBase, (size_t)1, sizeof(VBLObjectHeaderBaseLOGG), hFile->mDualStream.fileStream.mFile);
	fwrite(hFile->mDualStream.memStream.mBuffer, (size_t)1, lobjBase.base.mObjectSize - sizeof(VBLObjectHeaderBaseLOGG), hFile->mDualStream.fileStream.mFile);
  fwrite(hFile->mDualStream.memStream.mBuffer, (size_t)1, pad, hFile->mDualStream.fileStream.mFile);
	return 0;
}

BLFHANDLE Initiate(const char* blf_name) 
{
  BLFHANDLE writerh;
  FILE *fp = fopen(blf_name, "wb");  
  if (fp == NULL)
  {
	  perror("BLF:");
	  return NULL;
  }
  writerh = blfCreateFile(fp, GENERIC_WRITE);
  //BLSetApplication(file_, BL_APPID_CANCASEXLLOG, 1, 0, 1);
  writerh->magic = BLHANDLE_MAGIC;
  
  writerh->mDualStream.memStream.mBuffer = malloc(WRITE_BUF_SIZE);
  writerh->mDualStream.memStream.mBytesLeft = 0;
  writerh->mDualStream.fileStream.mFile = fp;
  fseek(fp, sizeof(writerh->mLOGG), SEEK_SET);
  
  logg_obj_init(writerh);

  writerh->mbasetime = 0xffffffffffffffff;

  memset(&writerh->mCanFdMsg, 0, sizeof(writerh->mCanFdMsg));
  writerh->mCanFdMsg.mHeader.mBase.mSignature = BL_OBJ_SIGNATURE;
  writerh->mCanFdMsg.mHeader.mBase.mHeaderSize = sizeof(writerh->mCanFdMsg.mHeader);
  writerh->mCanFdMsg.mHeader.mBase.mHeaderVersion = 1;
  writerh->mCanFdMsg.mHeader.mBase.mObjectSize = sizeof(writerh->mCanFdMsg);
  writerh->mCanFdMsg.mHeader.mBase.mObjectType = BL_OBJ_TYPE_CAN_FD_MESSAGE;
  writerh->mCanFdMsg.mHeader.mObjectFlags = BL_OBJ_FLAG_TIME_ONE_NANS;

  return writerh;
}

static success_t blfWriteObject(BLFHANDLE hFile, VBLObjectHeaderBase *pBase)
{
	if (hFile->mDualStream.memStream.mBuffer == NULL || 
		hFile->mDualStream.memStream.mBytesLeft + pBase->mObjectSize > WRITE_BUF_SIZE)
	{
		logg_obj_finish(hFile);
		hFile->mDualStream.memStream.mBytesLeft = 0;
	}

	memcpy(hFile->mDualStream.memStream.mBuffer + hFile->mDualStream.memStream.mBytesLeft, ((uint8_t *)pBase), pBase->mObjectSize);
	hFile->mDualStream.memStream.mBytesLeft += pBase->mObjectSize;
	
	hFile->mLOGG.objectCount++;
	return pBase->mObjectSize;
}

int WriteCanFrame(BLFHANDLE hFile, CANFrameRaw* can_frame) 
{
	if (hFile == NULL || hFile->magic != BLHANDLE_MAGIC) {
		return -1;
	}

	/* setup CAN FD object header */
	uint64_t ts = can_frame->time_stamp;
	if (ts < hFile->mbasetime)
	{
		hFile->mbasetime = ts;
		ts = 0;
	} else {
		ts = ts - hFile->mbasetime;
	}
	hFile->mCanFdMsg.mHeader.mObjectTimeStamp = ts;
	/* setup CAN FD message */
	hFile->mCanFdMsg.mChannel = can_frame->can_channel;
	hFile->mCanFdMsg.mFlags = CAN_MSG_FLAGS(0, 0);
	hFile->mCanFdMsg.mCANFDFlags = CAN_FD_MSG_FLAGS(1, 1, 1);
	hFile->mCanFdMsg.mValidDataBytes = can_frame->can_dlc;
	hFile->mCanFdMsg.mDLC = len2dlc(can_frame->can_dlc);
	hFile->mCanFdMsg.mFrameLength = can_frame->can_dlc;
	hFile->mCanFdMsg.mID = can_frame->can_id;

	/*if (can_frame->can_dlc == 64) {
		printf("hFile:%d %d [", hFile->mCanFdMsg.mValidDataBytes, hFile->mCanFdMsg.mDLC);
		for (int i = 0; i < hFile->mCanFdMsg.mValidDataBytes; i++)
			printf(" %d ", can_frame->data[i]);
		printf("]\r\n");
	}*/
	memcpy(hFile->mCanFdMsg.mData, (char *)can_frame->data, hFile->mCanFdMsg.mFrameLength);

	/* write CAN FD message */
	if (blfWriteObject(hFile, &hFile->mCanFdMsg.mHeader.mBase) < 0) {
		return -1;
	};

	return 0;
}

int Finish(BLFHANDLE hFile) 
{
	int ret = 0;
	if (hFile == NULL || hFile->magic != BLHANDLE_MAGIC) {
		return -1;
	}

	logg_obj_finish(hFile);

	time_t tt = time(NULL);
	struct tm *endTime = localtime(&tt);
	hFile->mLOGG.mMeasurementEndTime.wYear = endTime->tm_year+1900;
	hFile->mLOGG.mMeasurementEndTime.wMonth = endTime->tm_mon+1;
	hFile->mLOGG.mMeasurementEndTime.wDayOfWeek = endTime->tm_wday;
	hFile->mLOGG.mMeasurementEndTime.wDay = endTime->tm_mday;
	hFile->mLOGG.mMeasurementEndTime.wHour = endTime->tm_hour;
	hFile->mLOGG.mMeasurementEndTime.wMinute = endTime->tm_min;
	hFile->mLOGG.mMeasurementEndTime.wSecond = endTime->tm_sec;
	hFile->mLOGG.mMeasurementEndTime.wMilliseconds = 0;

	hFile->mLOGG.fileSize += sizeof(hFile->mLOGG);
	hFile->mLOGG.uncompressedFileSize += sizeof(hFile->mLOGG);

	fseek(hFile->mDualStream.fileStream.mFile, 0, SEEK_SET);
	fwrite((char *)&hFile->mLOGG, (size_t)1, sizeof(hFile->mLOGG), hFile->mDualStream.fileStream.mFile);

	fclose(hFile->mDualStream.fileStream.mFile);
	free(hFile->mDualStream.memStream.mBuffer);

  return ret;
}


