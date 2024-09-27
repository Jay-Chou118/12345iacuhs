package com.example.testcdc;

import static com.example.testcdc.MyService.decompress;
import static com.example.testcdc.MyService.gCanQueue1;
import static com.example.testcdc.MyService.gRecvMsgNum;
import static com.example.testcdc.MyService.g_notExitFlag;
import static com.example.testcdc.Utils.Utils.convert_u16;
import static com.example.testcdc.Utils.Utils.convert_u32;
import static com.example.testcdc.Utils.Utils.convert_u64;
import static com.example.testcdc.Utils.Utils.myCrc32;
import static com.example.testcdc.Utils.Utils.wait100ms;

import android.os.Process;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MCUHelper implements SerialInputOutputManager.Listener{

    @Override
    public void onNewData(byte[] bytes) {
//        mSerialBuffer.writeBuffer(bytes);
    }

    @Override
    public void onRunError(Exception e) {

    }

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

    public enum PARSE_SER_BUFFER_STATE {
        PARSE_HEAD_PHASE1,                    ///<解析函数接收帧头HEAD_FLAG_1状态
        PARSE_HEAD_PHASE2,                    ///<解析函数接收帧头HEAD_FLAG_2/3/4状态
        PARSE_TYPE_PHASE,                     ///<解析函数接收报文类型状态
        PARSE_USB_PACKAGE_SIZE,               ///<解析函数接收数据包长度状态
        PARSE_USB_PACKAGE_DATA,               ///<解析函数接收报文数据状态

    }


    private static final String TAG = "MICAN_MCUHelper";

    private static final byte HEAD_FLAG_1 = 0x12;                        ///<回复包帧头第一个字节
    private static final byte HEAD_FLAG_2 = 0x34;                        ///<回复包帧头第一个字节
    private static final byte HEAD_FLAG_3 = 0x56;                        ///<回复包帧头第一个字节
    private static final byte HEAD_FLAG_4 = 0x78;                        ///<回复包帧头第一个字节


    private UsbSerialPort mSerial = null;

    /**
     * 下发的命令指令
     */
    private byte[] mCmdData = new byte[]{};

    private final byte[] mReadBuffer = new byte[1024*1024];
    private final byte[] mParseBuffer = new byte[1024*1024];

    private final NoLockBuffer mSerialBuffer = new NoLockBuffer(1024*1024*20);

    private PARSE_SER_BUFFER_STATE m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;

    private COMMAND_TYPE m_curCmdType = COMMAND_TYPE.NOT_VALID;

    private long mUsbPackageIndex = 0;

    private Thread m_parseThread = null;

    private Thread m_readPortThread = null;

    private Thread m_sendThread = null;



    private String mAppVersion;

    private String mAppBuildTime;

    private long mTotalOTANumber;

    private long mMcuIndex;

    private String mSN;

    private long mAppLevel;

    private int mUsbPackageSize;

    private int mErrorNum;

    private Lock mLock = new ReentrantLock();

    private SerialInputOutputManager usbIoManager;


    public long getmMcuIndex() {
        return mMcuIndex;
    }

    public String getmSN() {
        return mSN;
    }

    public long getmAppLevel() {
        return mAppLevel;
    }

    public String getmAppVersion() {
        return mAppVersion;
    }

    public String getmAppBuildTime() {
        return mAppBuildTime;
    }

    public long getmTotalOTANumber() {
        return mTotalOTANumber;
    }

    public MCUHelper(UsbSerialPort mSerial) {
        this.mSerial = mSerial;

//        m_parseThread = new Thread(new Runnable() {
//
//            private static final String TAG = "MICAN_PARSE";
//            @Override
//            public void run() {
//                /// 每次读取的返回值，若为空，则跳到idle状态，等待总线数据
//                boolean readRet = false;
//                int usbPackageSize = 0;
//
//                while (g_notExitFlag.get())
//                {
//                    switch (m_curState){
//                        case PARSE_HEAD_PHASE1:
////                        Log.i(PARSE_TAG,m_curState.toString());
//                            readRet = mSerialBuffer.readBuffer(mParseBuffer,1);
//                            if(!readRet)
//                            {
////                            Log.d(PARSE_TAG,"未读取到数据");
//                                waitForData();
//                                continue;
//                            }
//                            if(HEAD_FLAG_1 == mParseBuffer[0])
//                            {
//                                m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE2;
//                            }else{
//                                Log.w(TAG,String.format( "============not valid===============: %d",mParseBuffer[0]));
//                            }
//                            break;
//                        case PARSE_HEAD_PHASE2:
////                        Log.i(PARSE_TAG,m_curState.toString());
//                            readRet = mSerialBuffer.readBuffer(mParseBuffer,3);
//                            if(!readRet)
//                            {
////                            Log.d(PARSE_TAG,"未读取到数据");
//                                waitForData();
//                                continue;
//                            }
//                            if(HEAD_FLAG_2 == mParseBuffer[0] && HEAD_FLAG_3 == mParseBuffer[1] && HEAD_FLAG_4 == mParseBuffer[2])
//                            {
//                                m_curState = PARSE_SER_BUFFER_STATE.PARSE_TYPE_PHASE;
//                            }else
//                            {
//                                Log.w(TAG, "============not valid===============: PARSE_HEAD_PHASE2");
//                                m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;
//                            }
//                            break;
//                        case PARSE_TYPE_PHASE:
////                        Log.i(PARSE_TAG,m_curState.toString());
//                            readRet = mSerialBuffer.readBuffer(mParseBuffer,2);
//                            if(!readRet)
//                            {
////                            Log.d(PARSE_TAG,"未读取到数据");
//                                waitForData();
//                                continue;
//                            }
//                            m_curCmdType = COMMAND_TYPE.fromValue(convert_u16(mParseBuffer));
//                            m_curState = PARSE_SER_BUFFER_STATE.PARSE_USB_PACKAGE_SIZE;
//                            break;
//                        case PARSE_USB_PACKAGE_SIZE:
////                        Log.i(PARSE_TAG,m_curState.toString());
//                            readRet = mSerialBuffer.readBuffer(mParseBuffer,2);
//                            if(!readRet)
//                            {
////                            Log.d(PARSE_TAG,"未读取到数据");
//                                waitForData();
//                                continue;
//                            }
//                            usbPackageSize = convert_u16(mParseBuffer);
////                        receive(Arrays.copyOf(buffer, 2));
////                        Log.i(PARSE_TAG,String.format("包大小为 %d",usbPackageSize));
//                            m_curState = PARSE_SER_BUFFER_STATE.PARSE_USB_PACKAGE_DATA;
//                            break;
//                        case PARSE_USB_PACKAGE_DATA:
////                        Log.i(PARSE_TAG,m_curState.toString());
////                        Log.i(PARSE_TAG,"==========" + m_curCmdType.toString());
//                            readRet = mSerialBuffer.readBuffer(mParseBuffer, usbPackageSize);
//                            if(!readRet)
//                            {
//                                Log.d(TAG,"未读取到数据");
//                                waitForData();
//                                continue;
//                            }
//                            if(m_curCmdType.code <= 0x1000)
//                            {
//
//                                if(mAppLevel >= 0x1040)
//                                {
//                                    parseUsbPackage_v3(mParseBuffer,usbPackageSize);
//                                }else
//                                {
//                                    parseUsbPackage_v2(mParseBuffer,usbPackageSize,true);
//                                }
//                            }else if(m_curCmdType.code == 0x3000)
//                            {
//                                Log.i(TAG,"错误帧处理逻辑");
//                            }
//                            else
//                            {
//                                parseUsbCmdPackage_v2(m_curCmdType,mParseBuffer,usbPackageSize);
//                            }
//                            m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;
//                            break;
//                        default:
//                            Log.e(TAG,"default is not designed!");
//
//                    }
//                }
//
//                Log.i(TAG,"ParseSerial thread is exit");
//
//            }
//        },"ParseSerial");
//
//        m_parseThread.start();

        m_readPortThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                while (g_notExitFlag.get())
                {
                    readPort();
                }
                Log.w(TAG,"ReadPort thread is exit");

            }
        },"ReadPort");
        m_readPortThread.start();

//        usbIoManager = new SerialInputOutputManager(mSerial, this);
//        usbIoManager.start();
        // 开始解析数据
    }

    public boolean readPort()
    {
//        if(mMcuIndex > 2)
//        {
//            waitForData();
//            return  false;
//        }
        try {
            if(mSerial == null)
            {
                Log.w(TAG,"m_port is null");
                return false;
            }
            int len = mSerial.read(mReadBuffer,100);
            //Log.d(TAG,"read num " + len);
            if(len >0)
            {
                mSerialBuffer.writeBuffer(mReadBuffer,len);
              //  Log.d(TAG, "read num " +  len + " wirte num : " + mReadBuffer);
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG,"readPort error!!");
            g_notExitFlag.set(false);
        }
        return false;
    }

    public boolean parseSerial()
    {
        boolean readRet = false;
        switch (m_curState){
            case PARSE_HEAD_PHASE1:
//                        Log.i(PARSE_TAG,m_curState.toString());
                readRet = mSerialBuffer.readBuffer(mParseBuffer,1);
                if(!readRet)
                {
//                            Log.d(PARSE_TAG,"未读取到数据");
//                    waitForData();
                   break;
                }
                if(HEAD_FLAG_1 == mParseBuffer[0])
                {
                    m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE2;
                }else{
                    break;
//                    Log.w(TAG,String.format( "============not valid===============: %d",mParseBuffer[0]));
                }
                break;
            case PARSE_HEAD_PHASE2:
//                        Log.i(PARSE_TAG,m_curState.toString());
                readRet = mSerialBuffer.readBuffer(mParseBuffer,3);
                if(!readRet)
                {
//                            Log.d(PARSE_TAG,"未读取到数据");
//                    waitForData();
                    break;
                }
                if(HEAD_FLAG_2 == mParseBuffer[0] && HEAD_FLAG_3 == mParseBuffer[1] && HEAD_FLAG_4 == mParseBuffer[2])
                {
                    m_curState = PARSE_SER_BUFFER_STATE.PARSE_TYPE_PHASE;
                }else
                {
                    Log.w(TAG, "============not valid===============: PARSE_HEAD_PHASE2");
                    m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;
                }
                break;
            case PARSE_TYPE_PHASE:
//                        Log.i(PARSE_TAG,m_curState.toString());
                readRet = mSerialBuffer.readBuffer(mParseBuffer,2);
                if(!readRet)
                {
//                            Log.d(PARSE_TAG,"未读取到数据");
//                    waitForData();
                   break;
                }
                m_curCmdType = COMMAND_TYPE.fromValue(convert_u16(mParseBuffer));
                m_curState = PARSE_SER_BUFFER_STATE.PARSE_USB_PACKAGE_SIZE;
                break;
            case PARSE_USB_PACKAGE_SIZE:
//                        Log.i(PARSE_TAG,m_curState.toString());
                readRet = mSerialBuffer.readBuffer(mParseBuffer,2);
                if(!readRet)
                {
//                            Log.d(PARSE_TAG,"未读取到数据");
//                    waitForData();
                   break;
                }
                mUsbPackageSize = convert_u16(mParseBuffer);
//                        receive(Arrays.copyOf(buffer, 2));
//                        Log.i(PARSE_TAG,String.format("包大小为 %d",usbPackageSize));
                m_curState = PARSE_SER_BUFFER_STATE.PARSE_USB_PACKAGE_DATA;
                break;
            case PARSE_USB_PACKAGE_DATA:
//                        Log.i(PARSE_TAG,m_curState.toString());
//                        Log.i(PARSE_TAG,"==========" + m_curCmdType.toString());
                readRet = mSerialBuffer.readBuffer(mParseBuffer, mUsbPackageSize);
                if(!readRet)
                {
                    Log.d(TAG,"未读取到数据");
//                    waitForData();
                   break;
                }
                if(m_curCmdType.code <= 0x1000)
                {

                    if(mAppLevel >= 0x1040)
                    {
                        parseUsbPackage_v3(mParseBuffer,mUsbPackageSize);
                    }else
                    {
//                        Log.d(TAG,"parseUsbPackage_v2");
                        parseUsbPackage_v2(mParseBuffer,mUsbPackageSize,true);
                    }
                }else if(m_curCmdType.code == 0x3000)
                {
                    Log.i(TAG,"错误帧处理逻辑");
                }
                else
                {
                     parseUsbCmdPackage_v2(m_curCmdType,mParseBuffer,mUsbPackageSize);
                }
                m_curState = PARSE_SER_BUFFER_STATE.PARSE_HEAD_PHASE1;
                break;
            default:
                Log.e(TAG,"default is not designed!");

        }
        return readRet;
    }

    public void close() {
        try {
            this.mSerial.close();
            Log.i(TAG,"serial is close");
        } catch (IOException e) {
            Log.e(TAG,e.toString());
        }
    }

    public void init()
    {
        getAppLevel();
        getMCUIndex();
        getAppVersion();

    }

    public void sendHeartBeat()
    {
        sendCmd(COMMAND_TYPE.SET_HEART_BEATS,null);
    }

    public void startCANFD()
    {
        sendCmd(COMMAND_TYPE.START_CAN_ALL,null);
    }

    public void stopCANFD()
    {
        sendCmd(COMMAND_TYPE.STOP_CAN_ALL,null);
    }


    public void SendOnce(JsonElement data)
    {
        sendCmd(COMMAND_TYPE.PERIOD_SEND_ONCE,data);
    }

    public void monitor()
    {
        Log.d(TAG,String.format("mMcuIndex: %d, mSerialBuffer: %d mErrorNum: %d",mMcuIndex,mSerialBuffer.size(),mErrorNum));
    }

    private void getAppVersion() {
        sendCmd(COMMAND_TYPE.GET_APP_VERSION,null);
    }


    private void getAppLevel() {
        sendCmd(COMMAND_TYPE.GET_APP_LEVEL,null);
    }

    void getMCUIndex() {
        sendCmd(COMMAND_TYPE.GET_DEVICE_CONFIG,null);
    }

    private boolean sendCmd(COMMAND_TYPE cmd, JsonElement data)
    {
        mLock.lock();
        Log.d(TAG,cmd.toString());
        switch (cmd){
            case START_CAN_ALL:
            case STOP_CAN_ALL:
            case GET_APP_VERSION:
            case GET_APP_LEVEL:
            case GET_APP_BUILD_TIME:
            case GET_DEVICE_CONFIG:
            case SET_HEART_BEATS:
                cmd_genCommonCmd(cmd);
                break;
            case PERIOD_SEND_ONCE:
                cmd_periodSendOnce(cmd,data);
                break;

            default:
                mCmdData = new byte[]{};
                break;
        }

        boolean ret = writeSerial();
        mLock.unlock();

        return true;
    }

    private void cmd_genCommonCmd(COMMAND_TYPE cmd) {
        mCmdData = new byte[]{0x5a,0x5a,0x5a,0x5a,(byte)(cmd.code & 0xff),(byte)(cmd.code >> 8 & 0xff),0,0};
    }

    private void cmd_periodSendOnce(COMMAND_TYPE cmd,JsonElement data){
        //{"row":1,"_id":"2_RLEDS_PTFusionCANFD_0x76","id":"2_RLEDS_PTFusionCANFD_0x76",
        // "text":"RLEDS_PTFusionCANFD_0x76",
        // "node_type":"msg","channel":1,"checked":true,"
        // children":[{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotSigGrpChks","id":"RLMotSigGrpChks","msg":118,"channel":2,"text":"RLMotSigGrpChks","node_type":"signal","comment":" ","remark":"信号remark","canId":177682,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":7,"length":8,"physStep":1,"dlc":1,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotSigGrpChks"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotSigGrpCntr","id":"RLMotSigGrpCntr","msg":118,"channel":2,"text":"RLMotSigGrpCntr","node_type":"signal","comment":" ","remark":"信号remark","canId":177683,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":11,"length":4,"physStep":1,"dlc":1,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotSigGrpCntr"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotActTq","id":"RLMotActTq","msg":118,"channel":2,"text":"RLMotActTq","node_type":"signal","comment":" ","remark":"信号remark","canId":177684,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":23,"length":15,"physStep":1,"dlc":0.1,"canType":-1638.3,"periodic":false,"eteDisable":false,"name":"RLMotActTq"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotActSpd","id":"RLMotActSpd","msg":118,"channel":2,"text":"RLMotActSpd","node_type":"signal","comment":" ","remark":"信号remark","canId":177685,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":39,"length":16,"physStep":1,"dlc":1,"canType":-32768,"periodic":false,"eteDisable":false,"name":"RLMotActSpd"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotMaxDynTqCp","id":"RLMotMaxDynTqCp","msg":118,"channel":2,"text":"RLMotMaxDynTqCp","node_type":"signal","comment":" ","remark":"信号remark","canId":177686,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":55,"length":10,"physStep":1,"dlc":1,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotMaxDynTqCp"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotMinDynTqCp","id":"RLMotMinDynTqCp","msg":118,"channel":2,"text":"RLMotMinDynTqCp","node_type":"signal","comment":" ","remark":"信号remark","canId":177687,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":71,"length":10,"physStep":1,"dlc":1,"canType":-1023,"periodic":false,"eteDisable":false,"name":"RLMotMinDynTqCp"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotFltLvlIndcn","id":"RLMotFltLvlIndcn","msg":118,"channel":2,"text":"RLMotFltLvlIndcn","node_type":"signal","comment":" ","remark":"信号remark","canId":177688,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":87,"length":3,"physStep":1,"dlc":1,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotFltLvlIndcn"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotAglRslvr","id":"RLMotAglRslvr","msg":118,"channel":2,"text":"RLMotAglRslvr","node_type":"signal","comment":" ","remark":"信号remark","canId":177689,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":95,"length":12,"physStep":1,"dlc":0.1,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotAglRslvr"},{"_id":"2_RLEDS_PTFusionCANFD_0x76_RLMotActSfSt","id":"RLMotActSfSt","msg":118,"channel":2,"text":"RLMotActSfSt","node_type":"signal","comment":" ","remark":"信号remark","canId":177690,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":111,"length":3,"physStep":1,"dlc":1,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotActSfSt"}],"name":"RLEDS_PTFusionCANFD_0x76","from":"dbc","e2e":false,
        // "periodic":0,"canType":"CAN","dlc":1,"canId":118,"dirty":"raw","isSending":false}

        mCmdData = new byte[]{};
//        mCmdData = new byte[]{0x5a,0x5a,0x5a,0x5a,(byte)(cmd.code & 0xff),(byte)(cmd.code >> 8 & 0xff),76 &0xFF,(76 >>8) &0xFF};

        JsonObject jsonObject = data.getAsJsonObject();

        //int channel = jsonObject.get("channel").getAsInt();
        int BusId = jsonObject.get("channel").getAsInt();
        int CanId = jsonObject.get("canId").getAsInt();
        String canType = jsonObject.get("canType").getAsString();
        int dlc = jsonObject.get("dlc").getAsInt();
        JsonArray rawDataJsonArray = jsonObject.getAsJsonArray("rawData");
        int FDFormat = "CAN".equals(canType) ? 0 : 1;

        // Convert JsonArray to int[]
        int[] rawData = new int[rawDataJsonArray.size()];
        for (int i = 0; i < rawDataJsonArray.size(); i++) {
            rawData[i] = rawDataJsonArray.get(i).getAsInt();
        }

        Log.w(TAG, "TTTTTTT mMcuIndex  " + mMcuIndex + "BusId  " + BusId);
        if (((BusId - 1) / 3) == mMcuIndex)
        {
//            JSONObject content = new JSONObject();
//                try {
//                    content.put("CANId", CanId);
//                    content.put("BUSId", BusId);
//                    content.put("dataLength", dlc);
//                    content.put("FDFormat", FDFormat);
//                    content.put("period", 0); // Set period to 0
//                    content.put("isReady", ""); // Default value for isReady
//                    content.put("slot", ""); // Default value for slot
//                    content.put("unused_2", ""); // Default value for unused_2
//
//                    // Convert the int[] rawData to a JSONArray and put it into the JSONObject
//                    JSONArray dataJsonArray = new JSONArray(rawData);
//                    content.put("data", dataJsonArray);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                Log.d(TAG, "TTTTTTTTTTTTT content: " + content);
            //BusId = (int)(BusId - 3 * mMcuIndex);

        }else
        {

            mCmdData = new byte[]{};

        }





    }
    private boolean writeSerial() {
        if (mSerial != null && mCmdData.length > 0 )
        {
            try {
                mSerial.write(mCmdData,2000);
                //Log.e(TAG, " TTTTT mCmdData: " + mCmdData );
                if(mAppLevel<0x1040)
                {
                    // 适配之前无buffer缓存的电脑
                    wait100ms();
                }
            } catch (IOException e) {
                Log.e(TAG,"writeSerial failed!");
            }
            return true;
        }
        return false;
    }

    private void waitForData()
    {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseUsbPackage_v2(byte[] data,int num, boolean ifCheckCrc)
    {
//
//            Log.i(PARSE_TAG,"parseUsbPackage_v2");
        // 等待处理的剩余个数
        int remainNum = num;
        if(ifCheckCrc) {
            // check crc32的值
            /// 取出最后4个字节,计算crc的值
            long crc = convert_u32(Arrays.copyOfRange(data, num - 4, num));

//

            long calCrc = myCrc32(Arrays.copyOf(data, num - 4));
//
            if (crc != calCrc) {
                Log.e(TAG, "crc from board is " + crc);
                Log.e(TAG, "crc from calculate is " + calCrc);
                Log.e(TAG, "crc check is error!!!!!!");
                return;
            }
            remainNum = num - 4;
        }

        int currentIndex = 0;
        int CAN_num  = 0;

        /// 先取出4个字节,代表usb包的index
        long currentUsbPackageIndex = convert_u32(data);
        currentIndex += 4;
        /// 代表还未收到usb包
        if(mUsbPackageIndex == 0)
        {
            mUsbPackageIndex = currentUsbPackageIndex;
        }else{
            if(currentUsbPackageIndex != (mUsbPackageIndex +1))
            {
                Log.e(TAG,"last: " + mUsbPackageIndex + "\t current: " + currentUsbPackageIndex + "\t fatal error usb dismiss!" + "\n");
                mUsbPackageIndex = currentUsbPackageIndex;
                return;
            }
            mUsbPackageIndex = currentUsbPackageIndex;
        }


        CanMessage msg = new CanMessage();
        while(currentIndex < remainNum )
        {
            int CAN_ID = (data[currentIndex] & 0xff) | ( (data[currentIndex +1 ]  & 0xff) << 8);
            currentIndex += 2;
            byte direct = (byte) (data[currentIndex] & 0x01);
            byte is_can = (byte) ((data[currentIndex] & 0x02) >> 1);
            currentIndex += 2;
            long timestamp = convert_u64(Arrays.copyOfRange(data, currentIndex, currentIndex + 8));
            currentIndex += 8;
            byte busid = data[currentIndex];
            currentIndex += 1;
            byte datalength = data[currentIndex];
            currentIndex += 1;
            byte[] can_data =  Arrays.copyOfRange(data, currentIndex, currentIndex + datalength);
            currentIndex += datalength;
            if(msg == null)
            {
                Log.d(TAG,"gCanQueue1 is full");
            }else {
                msg.setIndex(gRecvMsgNum.get());
                msg.setCAN_ID(CAN_ID);
                msg.setDirect(direct);
                msg.setCAN_TYPE(is_can);
                msg.setTimestamp(timestamp);
                msg.setBUS_ID( (byte) (busid +3*mMcuIndex));
                msg.setDataLength(datalength);
                msg.setData(can_data);
            }

            CAN_num ++;
            // 这边会抛出异常
//            gCanQueue.add(msg);
            gCanQueue1.write_deepcopy(msg);

            gRecvMsgNum.incrementAndGet();
//            Log.i(TAG,msg.toString());
        }
//                Log.i(TAG,"当前usb包包含 " + CAN_num);


    }

    private void parseUsbPackage_v3(byte[] data,int num)
    {
        // check crc32的值
        /// 取出最后4个字节,计算crc的值
        long crc = convert_u32(Arrays.copyOfRange(data, num-4, num));
        long calCrc = myCrc32(Arrays.copyOf(data,num-8));
        if(crc != calCrc)
        {
            Log.e(TAG,"crc from board is " +crc);
            Log.e(TAG,"crc from calculate is " +calCrc);
            Log.e(TAG,"crc check is error!!!!!!");
            mErrorNum ++;
            return;
        }
        int unCompressDataSize = (int)convert_u32(Arrays.copyOfRange(data, num-8, num-4));
//        Log.d(TAG,"compressDataSize: " + (num-8));
//        Log.d(TAG,"unCompressDataSize: " + unCompressDataSize);
        byte[] unCompressData = new byte[unCompressDataSize];
        // 进行解压
        long ret = decompress(Arrays.copyOf(data,num-8),unCompressData);
        if(ret != 0)
        {
            Log.e(TAG,"decompress ret is failed");
            return;
        }
        parseUsbPackage_v2(unCompressData,unCompressDataSize, false);

    }

    private void parseUsbCmdPackage_v2(COMMAND_TYPE cmd,byte[] data,int num ) {
        Log.d(TAG,"parseUsbCmdPackage_v2 " + cmd.toString());
        switch (cmd) {
            case PING_PONG_ACK:
            case START_CAN_ALL_ACK:
                break;

            case GET_DEVICE_CONFIG_ACK:
                mTotalOTANumber = convert_u64(Arrays.copyOfRange(data, 16, 24));
                mMcuIndex  = convert_u64(Arrays.copyOfRange(data, 24, 32));
                mSN = new String(data,32,7);
                Log.d(TAG,"mTotalOTANumber: " + mTotalOTANumber);
                Log.d(TAG,"mMcuIndex: " + mMcuIndex);
                Log.d(TAG,"mSN: " + mSN);
                break;
            case GET_APP_VERSION_ACK:
                mAppVersion = new String(data,0,num);
                Log.d(TAG,"mAppVersion: " + mAppVersion);
                break;
            case GET_APP_BUILD_TIME_ACK:
                mAppBuildTime = new String(data,0,num);
                Log.d(TAG,"mAppBuildTime: " + mAppBuildTime);
            case GET_APP_LEVEL_ACK:
                mAppLevel = convert_u32(data);
                Log.d(TAG,"mAppLevel: 0x" + Long.toHexString(mAppLevel));
            default:
                break;
        }
    }

}
