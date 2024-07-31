package com.example.testcdc.Utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.hoho.android.usbserial.util.HexDump;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
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


}
