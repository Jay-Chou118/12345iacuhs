package com.example.testcdc.Utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.testcdc.MyApplication;
import com.example.testcdc.dao.MsgInfoDao;
import com.example.testcdc.dao.SignalInfoDao;
import com.example.testcdc.database.MX11E4Database;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;
import com.hoho.android.usbserial.util.HexDump;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;

public class Utils {
    private static final String TAG = "MICAN_UTILS";

    static public int convert_u16(byte[] data)
    {
        return ((data[1] & 0xff) << 8) | (data[0] & 0xff);
    }

    static public long convert_u32(byte[] data)
    {
        return ((long)(data[3] & 0xff) << 24) | ( (data[2] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[0] & 0xff);
    }

    static public long convert_u64(byte[] data)
    {
        return ((long)(data[7] & 0xff) << 56) | ((long)(data[6] & 0xff) << 48) | ((long)(data[5] & 0xff) << 40) | ((long)(data[4] & 0xff) << 32) | ((long)(data[3] & 0xff) << 24) | ( (data[2] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[0] & 0xff);
    }

    static public long myCrc32(byte[] data)
    {
        CRC32 crc32 = new CRC32();

        // 更新CRC32校验和
        crc32.update(data);

        // 完成校验和的计算
        return crc32.getValue();
    }

    static public void wait100ms()
    {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Log.e(TAG,"sleep error");
        }
    }

    static public void wait10ms()
    {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Log.e(TAG,"sleep error");
        }
    }

    static public void wait200ms()
    {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Log.e(TAG,"sleep error");
        }
    }

    static public void wait1000ms()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG,"sleep error");
        }
    }

    static public void receive(byte[] data) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        spn.append("receive " + data.length + " bytes\n");
        spn.append(HexDump.dumpHexString(data)).append("\n");
        Log.i(TAG,spn.toString());
    }

    static public String formatTime()
    {
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }


    static public long getCurTime()
    {

        Instant instant = Instant.now(); // 获取当前时间
        long timestamp = instant.toEpochMilli() * 1000;
        return  timestamp; // 获取微秒级时间戳
    }

    static int[] mapInfo = mapInfo = new int[]{0b00000000, 0b00000001, 0b00000011, 0b00000111, 0b00001111, 0b00011111, 0b00111111, 0b01111111,0b11111111};
    static public long getSignal(int startBit,int bitLength,byte[] data)
    {
        long parsedValue = 0;
        //第一行的signal到达的位
        int curByteContainBitLength = startBit % 8 +1;
        //开始的行数
        int curByteIndex = startBit / 8;
        //未处理字节数
        int remainBitNum = bitLength;
        while(true) {
            if (curByteContainBitLength >= remainBitNum) {
                int offset = curByteContainBitLength - remainBitNum;
                parsedValue += (data[curByteIndex] >> offset) & mapInfo[remainBitNum];
                break;
            } else {
                ///从上到下依次左移剩余长度
                remainBitNum = remainBitNum - curByteContainBitLength;
                parsedValue += (long) (data[curByteIndex] & mapInfo[curByteContainBitLength]) << remainBitNum;
                curByteIndex++;
                curByteContainBitLength = 8;
            }
        }
        return parsedValue;
    }

    static public long getKey(int BUSId,int CANId)
    {
        return (long) BUSId << 32 | CANId;
    }


//    static public void DeleteIfExist(MX11E4Database database, long cid)
//    {
//        if(database.msgInfoDao().existsBycid(cid)){
//            database.msgInfoDao().deleteBycid(cid);
//            Log.d(TAG, "Finished delete for msg with cid " + cid);
//        }else {
//            Log.d(TAG, "No records found in msg for cid " + cid);
//        }
//
//        if (database.signalInfoDao().existsBycid(cid)){
//            database.signalInfoDao().deleteBycid(cid);
//            Log.d(TAG, "Finished delete for signal with cid " + cid);
//        }else {
//            Log.d(TAG, "No records found in signal for cid " + cid);
//        }
//
//    }
    static public String parseDBCByPython(String filePath)
    {
        Python python = Python.getInstance();
        PyObject pyObject = python.getModule("HelloWorld");
        String usermsg = String.valueOf(pyObject.callAttr("parse_dbc_file", filePath));
//        Log.e(TAG,"parseDBCByPython :" + usermsg);
        return usermsg;
    }

    static public void updateCustomData(String content,long cid,int BUSId)
    {

        MsgInfoDao msgInfoDao = MyApplication.getInstance().getMx11E4Database().msgInfoDao();
        SignalInfoDao signalInfoDao = MyApplication.getInstance().getMx11E4Database().signalInfoDao();
        List<MsgInfoEntity> msgInfoEntities = new ArrayList<>();
        List<SignalInfo> signalInfos = new ArrayList<>();
        try {
            JSONArray usermsgArray = new JSONArray(content);


            for (int i = 0; i < usermsgArray.length(); i++) {
                JSONObject usermsgObject = usermsgArray.getJSONObject(i);
                MsgInfoEntity msgInfo = new MsgInfoEntity();
                msgInfo.cid = cid;

                msgInfo.name = usermsgObject.getString("name");
                msgInfo.BUSId = BUSId;
                msgInfo.CANId = usermsgObject.getInt("id");
                msgInfo.sendType =  usermsgObject.getInt("cycle_time") == 0 ? "spontaneous": "cyclic";
                msgInfo.cycleTime =  usermsgObject.getInt("cycle_time");
                msgInfo.comment = usermsgObject.getString("comment");
                msgInfo.BUSName = "";
                msgInfo.senders = usermsgObject.getString("senders");
                msgInfo.receivers = usermsgObject.getString("receivers");
                msgInfo.CANType = usermsgObject.getBoolean("is_fd") ? "CANFD": "CAN";
                msgInfoEntities.add(msgInfo);
//                msgInfoDao.insert(msgInfo);
                // 处理这个报文下的所有signal
                JSONArray signals = usermsgObject.getJSONArray("signals");
                for (int j = 0; j < signals.length(); j++) {
                    JSONObject signal = signals.getJSONObject(j);
                    SignalInfo signalInfo = new SignalInfo();
                    signalInfo.name = signal.getString("name");
                    signalInfo.BUSId = BUSId;
                    signalInfo.CANId = usermsgObject.getInt("id");
                    //(name='XCDToBodyFuncDiagReq', start_bit=0, size=64, is_little_endian=False,
                    // is_signed=False, offset=Decimal('0'), factor=Decimal('1'),
                    // unit='', receivers=['ACU', 'BMDM'], comment='VCCDToBodyCanFuncDiagReqNpdu',
                    // multiplex=None, mux_value=None, is_float=False, is_ascii=False, type_label='',
                    // enumeration=None, comments={}, attributes={'GenSigSendType': 'OnWrite', 'GenSigStartValue': '0'},
                    // values={}, mux_val_grp=[], muxer_for_signal=None, calc_min_for_none=True, calc_max_for_none=True, cycle_time=0,
                    // initial_value=Decimal('0'), min=Decimal('0'), max=Decimal('18446744073709551615'))
                    signalInfo.byteOrder = signal.getBoolean("is_little_endian");
                    signalInfo.isSigned = signal.getBoolean("is_signed");
                    signalInfo.bitStart = signal.getInt("start_bit") ;
                    signalInfo.bitLength = signal.getInt("size");
                    signalInfo.scale = signal.getDouble("factor");
                    signalInfo.offset = signal.getDouble("offset");
                    signalInfo.comment = signal.getString("comment");
//                    Log.e(TAG, "updateCustomData: " +   signalInfo.comment);
                    signalInfo.minimum = signal.getDouble("min");
                    signalInfo.maximum = signal.getDouble("max");
                    signalInfo.initial = signal.getDouble("initial_value");
                    signalInfo.choices = signal.getString("choices");
                    signalInfo.cid = cid;
                    signalInfos.add(signalInfo);
//                    signalInfoDao.insert(signalInfo);

                }
            }
            msgInfoDao.insertAll(msgInfoEntities);
            signalInfoDao.insertAll(signalInfos);
            Log.e(TAG,"insert finish");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static public String parseBlfByPython(String filePath)
    {
        Python python = Python.getInstance();
        PyObject pyObject = python.getModule("HelloWorld");
        String usermsg = String.valueOf(pyObject.callAttr("blf_Read", filePath));
//        Log.e(TAG,"parseDBCByPython :" + usermsg);
        return usermsg;
    }


}
