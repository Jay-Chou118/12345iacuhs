package com.example.testcdc;

import static com.example.testcdc.Utils.Utils.formatTime;
import static com.example.testcdc.Utils.Utils.getCurTime;
import static com.example.testcdc.Utils.Utils.getKey;
import static com.example.testcdc.Utils.Utils.getSignal;
import static com.example.testcdc.Utils.Utils.wait100ms;
import static com.example.testcdc.Utils.Utils.wait10ms;
import static com.example.testcdc.Utils.Utils.wait200ms;
import static com.google.gson.JsonParser.parseString;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.testcdc.MiCAN.DataWrapper;
import com.example.testcdc.MiCAN.DeviceInfo;
import com.example.testcdc.MiCAN.ShowCANMsg;
import com.example.testcdc.MiCAN.ShowSignal;
import com.example.testcdc.database.Basic_DataBase;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MyService extends Service {

    static {
        System.loadLibrary("MyLibrary");
    }

    private static final String TAG = "MICAN_SERVICE";

    private static final String PARSE_TAG = "MICAN_PARSE";
    private static final int NOTIFICATION_ID = 1;


    public MyService() {
    }

    public static final NoLockCANBuffer gCanQueue1 = new NoLockCANBuffer(100000);

    public static final NoLockShowCANBuffer gDealQueue = new NoLockShowCANBuffer(100000);

    public static AtomicLong gRecvMsgNum = new AtomicLong(0);

    public static AtomicBoolean g_notExitFlag = new AtomicBoolean(true);
    private final IBinder m_binder = new MiCANBinder();

    private Basic_DataBase database;
    public class MiCANBinder extends Binder {

        private Thread mReadPortThread = null;

        private Thread mHeartBeatThread = null;

        private Thread mParseThread = null;

        private Thread mCostMsgThread = null;

        private String filePath;

        private long startCANTime;


        private Map<Long,List<SignalInfo>> monitorSignalMap = new HashMap<>();
        private Map<Long,List<SignalInfo>> monitorSignalMapShadow = new HashMap<>();


        Map<Integer, Integer> BUSRedirectMap = MainActivity3.BUSRedirectMap;

        public String getFilePath() {
            return filePath;
        }

        private void resetModuleMem()
        {
            gRecvMsgNum.set(0);
            gCanQueue1.clear();
            gDealQueue.clear();


            // 让线程退出
            g_notExitFlag.set(false);
            wait200ms();

            for(MCUHelper mcuHelper : mMcuHelperList)
            {
                mcuHelper.close();
            }
            mMcuHelperList.clear();
            System.gc();


            g_notExitFlag.set(true);
        }

        private List<MCUHelper> mMcuHelperList = new ArrayList<>();


        public void sayHello() {
            Log.i(TAG, "sayHello");
        }

        public boolean InitModule()
        {
            resetModuleMem();
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                Log.e(TAG,"USB devices is empty");
            }else {
                for (UsbSerialDriver driver : availableDrivers) {
                    Log.i(TAG, driver.getDevice().toString());
                    if (driver.getDevice().getVendorId() == 1155) {
                        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
                        if (connection == null) {
                            Log.e(TAG, "connection failed!!");
                            return false;
                        }

                        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)

                        try {
                            port.open(connection);
                            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                            Log.i(TAG,"open port successful~~~~~");
                            byte[] buffer = new byte[1024];
                            while(port.read(buffer,100) > 0)
                            {
                                Log.i(TAG,"clear history buffer before start");
                            }
                            mMcuHelperList.add(new MCUHelper(port,null,null));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if(mMcuHelperList.isEmpty())
            {
                return false;
            }

            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.init();

            }
            mParseThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean ret = false;
                    while (g_notExitFlag.get())
                    {
                        ret = false;
                        for(MCUHelper mcuHelper : mMcuHelperList)
                        {
                            ret = mcuHelper.parseSerial() || ret;
                        }
                        if(!ret)
                        {
                            wait10ms();
                        }
                    }
                    Log.w(TAG,"ParseSerial thread is exit");
                }
            },"ParseSerial");
            mParseThread.start();
            mHeartBeatThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long index = 0;
                    while (g_notExitFlag.get())
                    {
                        if(index % 100 == 0)
                        {
                            for(MCUHelper mcuHelper : mMcuHelperList)
                            {
                                mcuHelper.sendHeartBeat();
                            }
                        }
                        index += 1;
                        wait10ms();
                    }
                    Log.w(TAG,"HeartBeat thread is exit");
                }
            },"HeartBeat");
            mHeartBeatThread.start();

            mCostMsgThread = new Thread(new Runnable() {
                // 该线程为消费者队列

                private Map<Long,String> msgNameMap = new HashMap<>();


                private String findName(int BUSId,int CANId)
                {
//                    Log.e(TAG,msgNameMap.toString());
                    long key = getKey(BUSId, CANId);
                    String name = msgNameMap.get(key);
                    if(name != null) return name;
                    MsgInfoEntity msg = database.msgInfoDao().getMsg(BUSId, CANId);
                    if(msg == null)
                    {
                        msgNameMap.put(key,"");
                        return "";
                    }
                    msgNameMap.put(key,msg.name);
                    return msg.name;
                }
                @Override
                public void run() {
                    String[] directMap = {"rx","tx"};
                    String[] CANTypeMap = {"CANFD","CAN"};
                    ShowCANMsg showCANMsg = new ShowCANMsg();
                    // 获取设备canid和界面上配置的通道转换

                    boolean ret;
                    while (g_notExitFlag.get())
                    {
//                        CanMessage poll = gCanQueue.poll();
                        // 从队列中获取一条数据
                        CanMessage poll = gCanQueue1.read();

                        if(poll == null)
                        {
//                            Log.d(TAG,"poll is null");
                            wait100ms();
                            continue;
                        }

                        ShowSignal[] tmp = new ShowSignal[1];
                        tmp[0] = new ShowSignal();
                        tmp[0].setName("111");
                        showCANMsg.setTimestamp((double) (poll.timestamp + startCANTime) /1000000);
                        showCANMsg.setSqlId(poll.getIndex());

                        Integer redirctBUSID = BUSRedirectMap.get(Integer.valueOf(poll.getBUS_ID()));
                        if(redirctBUSID == null)
                        {
                            redirctBUSID = (int) poll.getBUS_ID();
                        }
////                        Log.e(TAG,"============busid: " +poll.getBUS_ID() +" 修正后: " + redirctBUSID);
//                        // 前端显示不需要修正
                        showCANMsg.setChannel(poll.getBUS_ID());
                        showCANMsg.setArbitrationId(poll.getCAN_ID());
                        showCANMsg.setName(findName(redirctBUSID,poll.CAN_ID));
                        showCANMsg.setCanType(CANTypeMap[poll.getCAN_TYPE()]);
                        showCANMsg.setDir(directMap[poll.getDirect()]);
                        showCANMsg.setDlc(String.valueOf(poll.getDataLength()));
                        showCANMsg.setData(Arrays.copyOf(poll.getData(),poll.getDataLength()));
//                        showCANMsg.setParsedData(tmp);
                        ret = gDealQueue.write_deepcopy(showCANMsg);

                        /***************************************************************************************/
                        synchronized (MiCANBinder.this)
                        {
//                            // 如果存在要解析的信号，则将信号解析出来
                            checkIfMonitor(poll);
                        }
                        /**************************************************************************************/
                        record(poll.timestamp,poll.BUS_ID,poll.dataLength,poll.CAN_ID,poll.CAN_TYPE,
                                poll.data);
                    }
                }
            },"CostMsg");
            mCostMsgThread.start();

            wait200ms();
            String firstSn = mMcuHelperList.get(0).getmSN();
            // 判断后续几个mcu是否为一个产品
            Log.d(TAG,"firstsn " +firstSn);
            // 如果是CR开头的，就是单设备
            if(firstSn == null)
            {
                return false;
            }

            if(firstSn.startsWith("CR"))
            {
                if(mMcuHelperList.size() != 1)
                {
                    Log.e(TAG, "当前为CR设备，但个数不为1");
                    return false;
                }
            }else if(firstSn.startsWith("DR4"))
            {
                if(mMcuHelperList.size() != 4)
                {
                    Log.e(TAG, "当前为DR4设备，但个数不为4");
                    return false;
                }
            }else if(firstSn.startsWith("DR2"))
            {
                if(mMcuHelperList.size() != 2)
                {
                    Log.e(TAG, "当前为DR2设备，但个数不为2");
                    return false;
                }
            }
            return true;
        }

        public boolean InitModule2()
        {
            resetModuleMem();
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            Log.e(TAG,"=========================InitModule2========================");
            for (UsbDevice device : usbDevices.values()) {
                // 检查设备的VID和PID是否匹配你的USB设备
                Log.e(TAG,device.toString());
                if (device.getVendorId() == 1155 || device.getVendorId() == 4236) {
//                PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
//                usbManager.requestPermission(device, permissionIntent);
                    Log.e(TAG,"==========================================================");
                    mMcuHelperList.add(new MCUHelper(null,device,usbManager));
                }
            }
            wait10ms();
            Log.e(TAG,"=====================step1============================");
            if(mMcuHelperList.isEmpty())
            {
                return false;
            }

            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.init();

            }
            mParseThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean ret = false;
                    while (g_notExitFlag.get())
                    {
                        ret = false;
                        for(MCUHelper mcuHelper : mMcuHelperList)
                        {
                            ret = mcuHelper.parseSerial() || ret;
                        }
                        if(!ret)
                        {
                            wait10ms();
                        }
                    }
                    Log.w(TAG,"ParseSerial thread is exit");
                }
            },"ParseSerial");
            mParseThread.start();
            mHeartBeatThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long index = 0;
                    while (g_notExitFlag.get())
                    {
                        if(index % 100 == 0)
                        {
                            for(MCUHelper mcuHelper : mMcuHelperList)
                            {
                                mcuHelper.sendHeartBeat();
                            }
                        }
                        index += 1;
                        wait10ms();
                    }
                    Log.w(TAG,"HeartBeat thread is exit");
                }
            },"HeartBeat");
            mHeartBeatThread.start();

            mCostMsgThread = new Thread(new Runnable() {
                // 该线程为消费者队列

                private Map<Long,String> msgNameMap = new HashMap<>();


                private String findName(int BUSId,int CANId)
                {
//                    Log.e(TAG,msgNameMap.toString());
                    long key = getKey(BUSId, CANId);
                    String name = msgNameMap.get(key);
                    if(name != null) return name;
                    MsgInfoEntity msg = database.msgInfoDao().getMsg(BUSId, CANId);
                    if(msg == null)
                    {
                        msgNameMap.put(key,"");
                        return "";
                    }
                    msgNameMap.put(key,msg.name);
                    return msg.name;
                }
                @Override
                public void run() {
                    String[] directMap = {"rx","tx"};
                    String[] CANTypeMap = {"CANFD","CAN"};
                    ShowCANMsg showCANMsg = new ShowCANMsg();
                    // 获取设备canid和界面上配置的通道转换

                    boolean ret;
                    while (g_notExitFlag.get())
                    {
//                        CanMessage poll = gCanQueue.poll();
                        // 从队列中获取一条数据
                        CanMessage poll = gCanQueue1.read();

                        if(poll == null)
                        {
//                            Log.d(TAG,"poll is null");
                            wait100ms();
                            continue;
                        }

                        ShowSignal[] tmp = new ShowSignal[1];
                        tmp[0] = new ShowSignal();
                        tmp[0].setName("111");
                        showCANMsg.setTimestamp((double) (poll.timestamp + startCANTime) /1000000);
                        showCANMsg.setSqlId(poll.getIndex());

                        Integer redirctBUSID = BUSRedirectMap.get(Integer.valueOf(poll.getBUS_ID()));
                        if(redirctBUSID == null)
                        {
                            redirctBUSID = (int) poll.getBUS_ID();
                        }
////                        Log.e(TAG,"============busid: " +poll.getBUS_ID() +" 修正后: " + redirctBUSID);
//                        // 前端显示不需要修正
                        showCANMsg.setChannel(poll.getBUS_ID());
                        showCANMsg.setArbitrationId(poll.getCAN_ID());
                        showCANMsg.setName(findName(redirctBUSID,poll.CAN_ID));
                        showCANMsg.setCanType(CANTypeMap[poll.getCAN_TYPE()]);
                        showCANMsg.setDir(directMap[poll.getDirect()]);
                        showCANMsg.setDlc(String.valueOf(poll.getDataLength()));
                        showCANMsg.setData(Arrays.copyOf(poll.getData(),poll.getDataLength()));
//                        showCANMsg.setParsedData(tmp);
                        ret = gDealQueue.write_deepcopy(showCANMsg);

                        /***************************************************************************************/
                        synchronized (MiCANBinder.this)
                        {
//                            // 如果存在要解析的信号，则将信号解析出来
                            checkIfMonitor(poll);
                        }
                        /**************************************************************************************/
//                        record(poll.timestamp,poll.BUS_ID,poll.dataLength,poll.CAN_ID,poll.CAN_TYPE,
//                                poll.data);
                    }
                }
            },"CostMsg");
            mCostMsgThread.start();

            wait200ms();
            String firstSn = mMcuHelperList.get(0).getmSN();
            // 判断后续几个mcu是否为一个产品
            Log.e(TAG,"firstsn " +firstSn);
            // 如果是CR开头的，就是单设备
            if(firstSn == null)
            {
                return false;
            }

            if(firstSn.startsWith("CR"))
            {
                if(mMcuHelperList.size() != 1)
                {
                    Log.e(TAG, "当前为CR设备，但个数不为1");
                    return false;
                }
            }else if(firstSn.startsWith("DR4"))
            {
                if(mMcuHelperList.size() != 4)
                {
                    Log.e(TAG, "当前为DR4设备，但个数不为4");
                    return false;
                }
            }else if(firstSn.startsWith("DR2"))
            {
                if(mMcuHelperList.size() != 2)
                {
                    Log.e(TAG, "当前为DR2设备，但个数不为2");
                    return false;
                }
            }

            return true;
        }

        private void checkIfMonitor(CanMessage canMessage)
        {

            int BUSId = canMessage.getBUS_ID();
            int CANId = canMessage.getCAN_ID();
            Integer redirctBUSID = BUSRedirectMap.get(BUSId);
            if(redirctBUSID == null)
            {
                redirctBUSID = BUSId;
            }
            double timestamp = (double) (canMessage.timestamp + startCANTime) /1000000 ;
            long uniqueKey = getKey(redirctBUSID,CANId);
            byte[] data = canMessage.getData();
            // 看该报文是否存在要解析的信号
//            if(monitorSignalMap.containsKey(uniqueKey))
//            {
                List<SignalInfo> signalInfos = monitorSignalMap.get(uniqueKey);
                if(signalInfos != null)
                {
                    signalInfos.forEach(signalInfo -> {
                        int startBit = signalInfo.bitStart;
                        int bitLength = signalInfo.bitLength;
                        signalInfo.times.add(timestamp);
                        signalInfo.values.add((double) getSignal(startBit,bitLength,data));
                    });
//                    Log.d(TAG,"该信号已成功解析");
                }else {
//                    Log.w(TAG,"uniqueKey in msgSignalMap is null");
                }
//            }
        }

        private int modifyBUSID(int BUSId)
        {
            Integer redirctBUSID = BUSRedirectMap.get(BUSId);
            if(redirctBUSID == null)
            {
                redirctBUSID = BUSId;
            }
            return redirctBUSID;
        }
        public String getAppVersion()
        {
            if(mMcuHelperList.isEmpty())
            {
                return "";
            }else{
                return mMcuHelperList.get(0).getmAppVersion();
            }
        }

        public String getSN()
        {
            if(mMcuHelperList.isEmpty())
            {
                return "";
            }else{
                return mMcuHelperList.get(0).getmSN();
            }
        }


        public boolean CANOnBus()
        {
            if(mMcuHelperList.isEmpty())
            {
                return false;
            }
            startCANTime = getCurTime();
            Log.d(TAG,"startCANTime " + startCANTime);
            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.startCANFD();
            }

            return true;
        }

        public boolean SendOnce(JsonElement data)
        {
            if(mMcuHelperList.isEmpty())
            {
                return false;
            }

            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.SendOnce(data);
            }

            return true;


        }

        public boolean SendPeriodsConfig(JsonElement data){
            if(mMcuHelperList.isEmpty())
            {
                return false;
            }
            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.loadPeriodsConfig(data);
            }
            return true;
        }


        public boolean SendPeriods(JsonElement data)
        {
            if(mMcuHelperList.isEmpty())
            {
                return false;
            }

            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.SendPeriodsConfig(data);
                mcuHelper.StartSendPeriods();
            }

            return true;


        }

        public boolean CANOffBus()
        {
            if(mMcuHelperList.isEmpty())
            {
                return false;
            }
            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.stopCANFD();
            }

            return true;
        }


        public void printInfo()
        {
            for(MCUHelper mcuHelper: mMcuHelperList)
            {
                mcuHelper.monitor();
            }
            Log.e(TAG,String.format("已录制 %d 报文 gCanQueue1 %d gDealQueue %d",gRecvMsgNum.get(),
                    gCanQueue1.size(),gDealQueue.size()
                    ));
            monitorSignalMap.values().forEach(signalInfos -> {
                signalInfos.forEach(signalInfo -> {
                    Log.e(TAG,String.format("collect CANID: %d,name: %s, count: %d",signalInfo.CANId,signalInfo.name,signalInfo.times.size()));
                });
            });
        }

        public void startSaveBlf(Context ctx)
        {
            filePath = getWorkHomeDir(ctx) + "MiCAN_record_" + formatTime() +".blf";
            Log.e(TAG,"====startSaveBlf====" + filePath);
            startRecord(filePath);
        }

        public void stopSaveBlf()
        {
            stopRecord();
        }

        public CanMessage getMessage() {
            return new CanMessage();
        }

        /**
         * @brief 获取最后的100条数据
         * @return 封装的数据包
         */
        public DataWrapper getCurrentMsgs()
        {

            // 获取最新的100条数据
            List<ShowCANMsg> showCANMsgs = gDealQueue.readAll();
            int num = showCANMsgs.size();
            Log.d(TAG,"showCANMsgs is " + num);
            if(num > 100)
            {
                showCANMsgs = showCANMsgs.subList(num-100,num);
            }
            // 将 monitorSignal 里面的数据都返回出来，这里要设计为线程安全
            List<ShowSignal> showSignals = new ArrayList<>();
//            synchronized (MiCANBinder.this)
//            {
//                Map<Long, List<SignalInfo>> tmp = monitorSignalMap;
//                // step 1 将 monitorSignalMap 指向shadow
//                monitorSignalMap = monitorSignalMapShadow;
//                monitorSignalMapShadow = tmp;
//
//            }
//
            synchronized (MiCANBinder.this) {
                monitorSignalMap.values().forEach(value -> {
                    value.forEach(signalInfo -> {
                        ShowSignal showSignal = new ShowSignal();
                        showSignal.setName(signalInfo.name);
                        showSignal.setBusId(signalInfo.BUSId);
                        showSignal.setCanId(signalInfo.CANId);
                        showSignal.setValues(signalInfo.values);
                        showSignal.setTimes(signalInfo.times);
                        showSignal.setRaw_values(signalInfo.values);
                        showSignals.add(showSignal);
////                        // 将观察表中的数据复位
                        signalInfo.values = new ArrayList<>();
                        signalInfo.times = new ArrayList<>();
                    });
                });
            }


            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setStart_time((double) startCANTime /1000000);
            dataWrapper.setFrame_data(showCANMsgs);
            dataWrapper.setSignal_data(showSignals);
            return dataWrapper;
        }

        public DeviceInfo getDeviceInfo()
        {
            DeviceInfo deviceInfo = new DeviceInfo();
            if(!mMcuHelperList.isEmpty())
            {
                deviceInfo.setSn(mMcuHelperList.get(0).getmSN());
                deviceInfo.setVersion(mMcuHelperList.get(0).getmAppVersion());
            }
            Log.d(TAG,deviceInfo.toString());
            return deviceInfo;
        }
        /**
         * @param ids 用于监控信号
         */
        public void monitorSignal(List<Long> ids)
        {
            // 获取canid 映射表
            // 初始化监控
            monitorSignalMap = new HashMap<>();
            ids.forEach(id->{
                SignalInfo signalInfo = database.signalInfoDao().getSignalById(id);
                long key = getKey(signalInfo.BUSId, signalInfo.CANId);
                if(monitorSignalMap.containsKey(key))
                {
                    List<SignalInfo> signalInfos = monitorSignalMap.get(key);
                    if(signalInfos == null)
                    {
                        signalInfos = new ArrayList<>();
                    }
                    signalInfos.add(signalInfo);
                }else {
                    List<SignalInfo> signalInfos = new ArrayList<>();
                    signalInfos.add(signalInfo);
                    monitorSignalMap.put(key,signalInfos);
                }
                Log.e(TAG,"添加 " + signalInfo.name + " 成功");
            });
        }

        public void monitorSignalShadow(List<Long> ids)
        {
            // 获取canid 映射表
            // 初始化监控
            monitorSignalMapShadow = new HashMap<>();
            ids.forEach(id->{
                SignalInfo signalInfo = database.signalInfoDao().getSignalById(id);
                long key = getKey(signalInfo.BUSId, signalInfo.CANId);
                if(monitorSignalMapShadow.containsKey(key))
                {
                    List<SignalInfo> signalInfos = monitorSignalMapShadow.get(key);
                    if(signalInfos == null)
                    {
                        signalInfos = new ArrayList<>();
                    }
                    signalInfos.add(signalInfo);
                }else {
                    List<SignalInfo> signalInfos = new ArrayList<>();
                    signalInfos.add(signalInfo);
                    monitorSignalMapShadow.put(key,signalInfos);
                }
                Log.e(TAG,"添加 " + signalInfo.name + " 成功");
            });
        }

        public  List<Map<String, Object>> parseMsgData(int BUSId, int CANId, long cid, byte[] data)
        {
            // 根据BUSID 和CANID找到里面有多少个signal
            // 进行busid修正
            List<SignalInfo> signalInfos = database.signalInfoDao().getSignalBycid(cid, modifyBUSID(BUSId), CANId);
            List<Map<String, Object>> infos = new ArrayList<>();
            signalInfos.forEach(signalInfo -> {
                Log.e(TAG,"signalInfo: " + signalInfo);
                Map<String,Object> info = new HashMap<>();
                info.put("canId",signalInfo.CANId);
                info.put("channel",signalInfo.BUSId);
                info.put("comment",signalInfo.comment);
                long rawData = getSignal(signalInfo.bitStart,signalInfo.bitLength,data);
                info.put("hex",rawData);
                String choices = signalInfo.choices;
                if (choices != null) {
                    JsonObject jsonObject = parseString(choices).getAsJsonObject();
                    Log.d(TAG, "jsonObject    " + jsonObject);

                    if (jsonObject != null) {
                        JsonElement valueElement = jsonObject.get(String.valueOf(rawData));
                        if (valueElement != null) {
                            String enumStr = valueElement.getAsString();
                            Log.d(TAG, "enumStr    " + enumStr);
                            info.put("value", "0x" + Long.toHexString(rawData) + "-" + enumStr);
                        } else {
                            Log.e(TAG, "Value not found in JSON for rawData: " + rawData);
                            info.put("value", "0x" + Long.toHexString(rawData));
                        }
                    } else {
                        Log.e(TAG, "jsonObject is null");
                        info.put("value", "0x" + Long.toHexString(rawData));
                    }
                } else {
                    info.put("value", "0x" + Long.toHexString(rawData));
                }

                info.put("id", String.valueOf(signalInfo.id));
                info.put("isChildTit", false);
                info.put("isExpand", true);
                info.put("isParent", false);
                info.put("name", signalInfo.getName());
                infos.add(info);
            });
            Log.e(TAG,"infos   " + infos);
            return infos;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        database = MyApplication.getInstance().getDatabase();
        return m_binder;
    }



    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate");
        super.onCreate();
        sayHello();


//        findCDCDevice();
//        createWorkThread();
//        startCAN();
//        startRecord(getWorkHomeDir());
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("channel_id", "前台 Service 通知", NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("前台服务")
                .setContentText("服务正在运行")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // 设置通知的小图标
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        startForeground(NOTIFICATION_ID, notificationBuilder.build());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    public native String stringFromJNI();

    public native void testCreateFile(String dir);

    public native void startRecord(String dir);

    public native void stopRecord();

    public native void record(long timestamp,short can_channel,short can_dlc,int can_id,
                              int can_type,byte[] data);

    public static native int decompress(byte[] compressDataBuffer,byte[] unCompressDataBuffer);


    public native void sayHello();

    private String getWorkHomeDir(Context ctx)
    {
        String micanPath  = ctx.getFilesDir().getAbsolutePath() + "/MICAN/";
        File micanFile = new File(micanPath);
        if (!micanFile.exists()) {
            boolean created = micanFile.mkdir();
            if (created) {
                Log.i(TAG, "====getWorkHomeDir===创建成功");
            } else {
                Log.e(TAG, "====getWorkHomeDir===创建失败");
                micanPath = ctx.getFilesDir().getAbsolutePath();
            }
        }
//        return Environment.getExternalStorageDirectory()+"/MICAN/";
        Log.e(TAG,"====getWorkHomeDir===" + micanPath) ;
        return micanPath;
    }
}