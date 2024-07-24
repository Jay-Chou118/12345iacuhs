package com.example.testcdc;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.CRC32;
import android.os.Process;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("MyLibrary");
    }

    public enum PARSE_SER_BUFFER_STATE {
        PARSE_HEAD_PHASE1,                    ///<解析函数接收帧头HEAD_FLAG_1状态
        PARSE_HEAD_PHASE2,                    ///<解析函数接收帧头HEAD_FLAG_2/3/4状态
        PARSE_TYPE_PHASE,                     ///<解析函数接收报文类型状态
        PARSE_USB_PACKAGE_SIZE,               ///<解析函数接收数据包长度状态
        PARSE_USB_PACKAGE_DATA,               ///<解析函数接收报文数据状态

    }

    ///对MCU下发的指令及MCU的ACK响应枚举
    public enum COMMAND_TYPE
    {
        DATA(0x0000),

        ERROR_FRAME(0x3000),
        PING_PONG(0x1001),                     ///<PING_PONG指令
        PING_PONG_ACK(0x2001),                 ///<PING_PONG响应ACK
        START_CAN_ALL(0x1002),                 ///<开启MCU的CAN控制器指令
        START_CAN_ALL_ACK(0x2002),             ///<MCU的CAN控制器开启完成响应
        STOP_CAN_ALL(0x1003),                    ///<关闭MCU的CAN控制器指令
        STOP_CAN_ALL_ACK(0x2003),                ///<MCU的CAN控制器关闭完成响应
        // 发送类的指令10开头
        PERIOD_SEND_CONFIG(0x1010),            ///<下发周期发送报文配置指令
        PERIOD_SEND_CONFIG_ACK(0x2010),        ///<周期发送报文配置完成响应
        PERIOD_SEND_START (0x1011),             ///<开启MCU周期发送功能指令
        PERIOD_SEND_START_ACK(0x2011),         ///<周期发送功能开启成功响应
        PERIOD_SEND_STOP(0x1012),              ///<关闭MCU周期发送功能指令
        PERIOD_SEND_STOP_ACK (0x2012),          ///<MCU周期发送关闭完成响应
        PERIOD_SEND_ONCE (0x1013),              ///<单次发送一帧报文功能指令
        PERIOD_SEND_ONCE_ACK (0x2013),          ///<发送单帧报文完成响应

        SET_DEVICE_CONFIG (0x1020),             ///<写入MCU配置指令
        SET_DEVICE_CONFIG_ACK (0x2020),         ///<MCU配置完成响应
        SET_HEART_BEATS (0x1021),               ///< 和板子的心跳包
        SET_HEART_BEATS_ACK (0x2021),           ///< ack
        PERIOD_SEND_E2E_START (0x1014),         ///<发送E2E配置指令
        PERIOD_SEND_E2E_START_ACK(0x2014),     ///<发送E2E配置指令响应



        OTA_MODE_ENTER(0x1090),                ///<下发进入OTA模式指令
        OTA_MODE_ENTER_ACK (0x2090),            ///<MCU进入OTA模式完成响应
        OTA_PACKAGE_TRANSMIT (0x1091),          ///<下发OTA升级包指令
        OTA_PACKAGE_TRANSMIT_ACK (0x2091),      ///<OTA升级包接收响应


        GET_APP_VERSION (0x1080),              ///<获取MCU固件版本号指令
        GET_APP_VERSION_ACK  (0x2080),          ///<MCU固件版本号回复响应
        GET_APP_BUILD_TIME (0x1081),            ///<读取MCU软件编译信息指令
        GET_APP_BUILD_TIME_ACK (0x2081),        ///<MCU软件编译信息回复响应
        GET_DEVICE_CONFIG (0x1082),             ///<读取MCU flash配置信息指令,例如：sn号等
        GET_DEVICE_CONFIG_ACK (0x2082),         ///<MCU flash配置信息回复响应
        GET_CPU_LOAD (0x1083),                  ///<获取MCU负载率指令
        GET_CPU_LOAD_ACK (0x2083),              ///<MCU负载率信息回复响应
        GET_APP_LEVEL (0x1084),                 ///<读取APILevel指令
        GET_APP_LEVEL_ACK (0x2084),             ///<APILevel信息回复响应
        GET_BUS_STATUS (0x1085),                ///<获取总线状态
        GET_BUS_STATUS_ACK (0x2085),

        NOT_VALID (0xFFFF);

        private final int code;

        COMMAND_TYPE(int code) {
            this.code = code;
        }

        // 静态方法，根据int值获取枚举常量
        public static COMMAND_TYPE fromValue(int value) {
            for (COMMAND_TYPE command : COMMAND_TYPE.values()) {
                if (command.code == value) {
                    return command;
                }
            }
            return COMMAND_TYPE.NOT_VALID;
        }
    };

    class ParsePackage implements Runnable
    {

        private static final byte HEAD_FLAG_1 = 0x12;                        ///<回复包帧头第一个字节
        private static final byte HEAD_FLAG_2 = 0x34;                        ///<回复包帧头第一个字节
        private static final byte HEAD_FLAG_3 = 0x56;                        ///<回复包帧头第一个字节
        private static final byte HEAD_FLAG_4 = 0x78;                        ///<回复包帧头第一个字节

        private PARSE_SER_BUFFER_STATE m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;

        private COMMAND_TYPE m_curCmdType = COMMAND_TYPE.NOT_VALID;


        private long m_usbPackageIndex = 0;

        private void waitForData()
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private int convert_u16(byte[] data)
        {
            return ((data[1] & 0xff) << 8) | (data[0] & 0xff);
        }

        private long convert_u32(byte[] data)
        {
            return ((long)(data[3] & 0xff) << 24) | ( (data[2] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[0] & 0xff);
        }

        private long convert_u64(byte[] data)
        {
            return ((long)(data[7] & 0xff) << 56) | ((long)(data[6] & 0xff) << 48) | ((long)(data[5] & 0xff) << 40) | ((long)(data[4] & 0xff) << 32) | ((long)(data[3] & 0xff) << 24) | ( (data[2] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[0] & 0xff);
        }

        private long myCrc32(byte[] data)
        {
            CRC32 crc32 = new CRC32();

            // 更新CRC32校验和
            crc32.update(data);

            // 完成校验和的计算
            return crc32.getValue();
        }
        private void parseUsbPackage_v2(byte[] data,int num, boolean ifCheckCrc)
        {
//
//            Log.i(PARSE_TAG,"parseUsbPackage_v2");
            // 等待处理的剩余个数
            int remainNum = num;
            if(ifCheckCrc)
            {
                // check crc32的值
                /// 取出最后4个字节,计算crc的值
                long crc = convert_u32(Arrays.copyOfRange(data, num-4, num));

//

                long calCrc = myCrc32(Arrays.copyOf(data,num-4));
//
                if(crc != calCrc)
                {
                    Log.e(PARSE_TAG,"crc from board is " +crc);
                    Log.e(PARSE_TAG,"crc from calculate is " +calCrc);
                    receive(Arrays.copyOf(data, num));
                    Log.e(TAG,"crc check is error!!!!!!");
                    return;
                }
                remainNum = num-4;

                int currentIndex = 0;
                int CAN_num  = 0;

                /// 先取出4个字节,代表usb包的index
                long currentUsbPackageIndex = convert_u32(data);
                currentIndex += 4;
                /// 代表还未收到usb包
                if(m_usbPackageIndex == 0)
                {
                    m_usbPackageIndex = currentUsbPackageIndex;
                }else{
                    if(currentUsbPackageIndex != (m_usbPackageIndex +1))
                    {
                        Log.e(PARSE_TAG,"last: " + m_usbPackageIndex + "\t current: " + currentUsbPackageIndex + "\t fatal error usb dismiss!" + "\n");
                        return;
                    }
                    m_usbPackageIndex = currentUsbPackageIndex;
                }



                while(currentIndex < remainNum )
                {
                    CanMessage msg = new CanMessage();
                    int CAN_ID = (data[currentIndex] & 0xff) | ( (data[currentIndex +1 ]  & 0xff) << 8);
                    currentIndex += 2;
                    byte direct = (byte) (data[currentIndex] & 0x01);
                    byte is_can = (byte) (data[currentIndex] & 0x02);
                    currentIndex += 2;
                    long timestamp = convert_u64(Arrays.copyOfRange(data, currentIndex, currentIndex + 8));
                    currentIndex += 8;
                    byte busid = data[currentIndex];
                    currentIndex += 1;
                    byte datalength = data[currentIndex];
                    currentIndex += 1;
                    byte[] can_data =  Arrays.copyOfRange(data, currentIndex, currentIndex + datalength);
                    currentIndex += datalength;
                    msg.setCAN_ID(CAN_ID);
                    msg.setDirect(direct);
                    msg.setCAN_TYPE(is_can);
                    msg.setTimestamp(timestamp);
                    msg.setBUS_ID(busid);
                    msg.setDataLength(datalength);
                    msg.setData(can_data);
                    CAN_num ++;
                    // 这边会抛出异常
                    g_queue.add(msg);
                    g_totalRecvMsgNum += 1;
//                    Log.i(PARSE_TAG,msg.toString());
                }
//                Log.i(PARSE_TAG,"当前usb包包含 " + CAN_num);
            }

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024*1024];

            /// 每次读取的返回值，若为空，则跳到idle状态，等待总线数据
            boolean readRet = false;
            int usbPackageSize = 0;

            while (true)
            {
                switch (m_curState){
                    case PARSE_HEAD_PHASE1:
//                        Log.i(PARSE_TAG,m_curState.toString());
                        readRet = m_serialBuffer.readBuffer(buffer,1);
                        if(!readRet)
                        {
//                            Log.d(PARSE_TAG,"未读取到数据");
                            waitForData();
                            continue;
                        }
                        if(HEAD_FLAG_1 == buffer[0])
                        {
                            m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE2;
                        }else{
                            Log.w(PARSE_TAG,String.format( "============not valid===============: %d",buffer[0]));
                        }
                        break;
                    case PARSE_HEAD_PHASE2:
//                        Log.i(PARSE_TAG,m_curState.toString());
                        readRet = m_serialBuffer.readBuffer(buffer,3);
                        if(!readRet)
                        {
//                            Log.d(PARSE_TAG,"未读取到数据");
                            waitForData();
                            continue;
                        }
                        if(HEAD_FLAG_2 == buffer[0] && HEAD_FLAG_3 == buffer[1] && HEAD_FLAG_4 == buffer[2])
                        {
                            m_curState = PARSE_SER_BUFFER_STATE.PARSE_TYPE_PHASE;
                        }else
                        {
                            Log.w(PARSE_TAG, "============not valid===============: PARSE_HEAD_PHASE2");
                            m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;
                        }
                        break;
                    case PARSE_TYPE_PHASE:
//                        Log.i(PARSE_TAG,m_curState.toString());
                        readRet = m_serialBuffer.readBuffer(buffer,2);
                        if(!readRet)
                        {
//                            Log.d(PARSE_TAG,"未读取到数据");
                            waitForData();
                            continue;
                        }
                        m_curCmdType = COMMAND_TYPE.fromValue(convert_u16(buffer));
                        m_curState = PARSE_SER_BUFFER_STATE.PARSE_USB_PACKAGE_SIZE;
                        break;
                    case PARSE_USB_PACKAGE_SIZE:
//                        Log.i(PARSE_TAG,m_curState.toString());
                        readRet = m_serialBuffer.readBuffer(buffer,2);
                        if(!readRet)
                        {
//                            Log.d(PARSE_TAG,"未读取到数据");
                            waitForData();
                            continue;
                        }
                        usbPackageSize =convert_u16(buffer);
//                        receive(Arrays.copyOf(buffer, 2));
//                        Log.i(PARSE_TAG,String.format("包大小为 %d",usbPackageSize));
                        m_curState = PARSE_SER_BUFFER_STATE.PARSE_USB_PACKAGE_DATA;
                        break;
                    case PARSE_USB_PACKAGE_DATA:
//                        Log.i(PARSE_TAG,m_curState.toString());
//                        Log.i(PARSE_TAG,"==========" + m_curCmdType.toString());
                        readRet = m_serialBuffer.readBuffer(buffer, usbPackageSize);
                        if(!readRet)
                        {
                            Log.d(PARSE_TAG,"未读取到数据");
                            waitForData();
                            continue;
                        }
                        if(m_curCmdType.code <= 0x1000)
                        {
                            parseUsbPackage_v2(buffer,usbPackageSize,true);
                        }else if(m_curCmdType.code == 0x3000)
                        {
                            Log.i(PARSE_TAG,"错误帧处理逻辑");
                        }
                        else
                        {
                            Log.i(PARSE_TAG,"命令型报文处理逻辑");
                        }
                        m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;
                        break;
                    default:
                        Log.e(PARSE_TAG,"default is not designed!");

                }
            }

        }
    }

    class ReadPort implements Runnable
    {


        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            byte[] buffer = new byte[1024*1024];
            while (true)
            {
                try {
                    if(m_port == null)
                    {
                        Log.w(TAG,"m_port is null");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        continue;
                    }
                    int len = m_port.read(buffer,100);
                    if(len >0)
                    {
                        m_serialBuffer.writeBuffer(buffer,len);
//                        receive(Arrays.copyOf(buffer, len));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class HeartBeat implements Runnable
    {

        @Override
        public void run() {

            byte[] data = {0x5a,0x5a,0x5a,0x5a,0x21,0x10,0x00,0x00};
            while (true)
            {
                if(m_port != null)
                {
                    try {
                        m_port.write(data,2000);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private long g_totalRecvMsgNum = 0;
    private final LinkedBlockingQueue<CanMessage> g_queue = new LinkedBlockingQueue<>(1024*1024);

    private static final String TAG = "CDC_";

    private static final String PARSE_TAG = "CDC_PARSE";

    private UsbSerialPort m_port = null;

    private Thread m_readThread = null;

    private Thread m_parseThread = null;

    private Thread m_heartBeatThread = null;

    private Thread m_monitorThread = null;

    private Thread m_costMsgThread = null;

    private final NoLockBuffer m_serialBuffer = new NoLockBuffer(1024*1024*200);

    private void receive(byte[] data) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        spn.append("receive " + data.length + " bytes\n");
        spn.append(HexDump.dumpHexString(data)).append("\n");
        Log.i(TAG,spn.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_port !=null)
                {
                    return;
                }

                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
                if (availableDrivers.isEmpty()) {
                    Log.e("CDC","USB devices is empty");
                }else {
                    for(UsbSerialDriver driver:availableDrivers)
                    {
                        Log.i(TAG,driver.getDevice().toString());
                        if(driver.getDevice().getVendorId() == 1155)
                        {
                            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
                            if (connection == null) {
                                Log.e("CDC","connection failed!!");
                                return;
                            }

                            m_port = driver.getPorts().get(0); // Most devices have just one port (port 0)
                            try {
                                m_port.open(connection);
                                m_port.setParameters(1382400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                                Log.i(TAG,"open port successful~~~~~");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }


                        }
                    }
                }
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开始一个线程进行读取串口数据
                if(m_readThread == null)
                {
                    m_readThread = new Thread(new ReadPort(),"readSerial");
//                    m_readThread.setPriority(Thread.MAX_PRIORITY); // 设置为最高优先级?
                    m_readThread.start();
                }

                if(m_parseThread == null)
                {
                    m_parseThread = new Thread(new ParsePackage(),"parseSerial");
                    m_parseThread.start();
                }

                if(m_heartBeatThread == null)
                {
                    m_heartBeatThread = new Thread(new HeartBeat());
                    m_heartBeatThread.start();
                }

                if(m_monitorThread == null)
                {
                    m_monitorThread = new Thread(
                            new Runnable() {
                        @Override
                        public void run() {
                            while (true)
                            {
                                Log.d(PARSE_TAG,"RecvNum: " + g_totalRecvMsgNum + "\t CanMessage Queue " + g_queue.size() + "\t m_serialBuffer: " + m_serialBuffer.size());
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    },"monitor");
                    m_monitorThread.start();
                }

                if(m_costMsgThread == null)
                {
                    m_costMsgThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true)
                            {
                                CanMessage poll = g_queue.poll();
                                if(poll == null)
                                {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    continue;
                                }
//                                Log.d(PARSE_TAG,poll.toString());
                            }
                        }
                    },"costMsg");
                    m_costMsgThread.start();
                }
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_port == null)
                {
                   Log.i(TAG,"未打开设备");
                   return;
                }
                byte[] data = {0x5a,0x5a,0x5a,0x5a,0x01,0x10,0x00,0x00};
                try {
                    m_port.write(data,2000);
                    Log.i(TAG,"下发PING指令");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_port == null)
                {
                    Log.i(TAG,"未打开设备");
                    return;
                }
                byte[] data = {0x5a,0x5a,0x5a,0x5a,0x02,0x10,0x00,0x00};
                try {
                    m_port.write(data,2000);
                    Log.i(TAG,"打开CAN设备");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        TextView textView = findViewById(R.id.textView);
        textView.setText(stringFromJNI());



    }

    public native String stringFromJNI();


}