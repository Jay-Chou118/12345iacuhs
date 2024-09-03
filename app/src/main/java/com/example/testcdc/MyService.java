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
import android.hardware.usb.UsbDeviceConnection;
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
import com.example.testcdc.database.MX11E4Database;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;
import com.google.gson.JsonObject;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

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

    private MX11E4Database database;
    public class MiCANBinder extends Binder {

        private Thread mReadPortThread = null;

        private Thread mHeartBeatThread = null;

        private Thread mParseThread = null;

        private Thread mCostMsgThread = null;

        private String filePath;

        private long startCANTime;


        private Map<Long,List<SignalInfo>> monitorSignalMap = new HashMap<>();




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
                            port.setParameters(1382400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                            Log.i(TAG,"open port successful~~~~~");
                            byte[] buffer = new byte[1024];
                            while(port.read(buffer,100) > 0)
                            {
                                Log.i(TAG,"clear history buffer before start");
                            }
                            mMcuHelperList.add(new MCUHelper(port));
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

                private Map<Long,String> msgNameMap = new HashMap<>();


                private String findName(int BUSId,int CANId)
                {
                    long key = getKey(BUSId, CANId);
                    if(msgNameMap.containsKey(key))
                    {
                        return msgNameMap.get(key);
                    }
                    MsgInfoEntity msg = database.msgInfoDao().getMsg(BUSId, CANId);
                    if(msg == null)
                    {
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

                    boolean ret;
                    while (g_notExitFlag.get())
                    {
//                        CanMessage poll = gCanQueue.poll();
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
                        showCANMsg.setChannel(poll.getBUS_ID());
                        showCANMsg.setArbitrationId(poll.getCAN_ID());
                        showCANMsg.setName(findName(poll.BUS_ID,poll.CAN_ID));
                        showCANMsg.setCanType(CANTypeMap[poll.getCAN_TYPE()]);
                        showCANMsg.setDir(directMap[poll.getDirect()]);
                        showCANMsg.setDlc(String.valueOf(poll.getDataLength()));
                        showCANMsg.setData(Arrays.copyOf(poll.getData(),poll.getDataLength()));
                        showCANMsg.setParsedData(tmp);
                        ret = gDealQueue.write_deepcopy(showCANMsg);

                        /***************************************************************************************/
                        synchronized (MiCANBinder.this)
                        {
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

        private void checkIfMonitor(CanMessage canMessage)
        {

            int BUSId = canMessage.getBUS_ID();
            int CANId = canMessage.getCAN_ID();
            double timestamp = (double) (canMessage.timestamp + startCANTime) /1000000 ;
            long uniqueKey = getKey(BUSId,CANId);
            byte[] data = canMessage.getData();
            // 看该报文是否存在要解析的信号
            if(monitorSignalMap.containsKey(uniqueKey))
            {
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
                    Log.w(TAG,"uniqueKey in msgSignalMap is null");
                }
            }
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
            Log.d(TAG,String.format("已录制 %d 报文 gCanQueue1 %d gDealQueue %d",gRecvMsgNum.get(),
                    gCanQueue1.size(),gDealQueue.size()
                    ));
//            Log.i(TAG,msgSignalMap.toString());

        }

        public void startSaveBlf()
        {
            filePath = getWorkHomeDir() + "MiCAN_record_" + formatTime() +".blf";
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
//            Log.d(TAG,"showCANMsgs is " + num);
            if(num > 500)
            {
                showCANMsgs = showCANMsgs.subList(num-500,num);
            }
            // 将 monitorSignal 里面的数据都返回出来，这里要设计为线程安全
            List<ShowSignal> showSignals = new ArrayList<>();
            synchronized (MiCANBinder.this)
            {
                monitorSignalMap.values().forEach(value->{
                    value.forEach(signalInfo -> {
                        Log.d(TAG, "callbackjs  444444: " + signalInfo.name);
                        ShowSignal showSignal = new ShowSignal();
                        showSignal.setName(signalInfo.name);
                        showSignal.setBusId(signalInfo.BUSId);
                        showSignal.setCanId(signalInfo.CANId);
                        showSignal.setValues(signalInfo.values);
                        showSignal.setTimes(signalInfo.times);
                        showSignal.setRaw_values(signalInfo.values);
                        showSignals.add(showSignal);
                        // 将观察表中的数据复位
                        signalInfo.values = new ArrayList<>();
                        signalInfo.times = new ArrayList<>();
                    });
                });
            }


            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setStart_time((double) startCANTime /1000000);
            dataWrapper.setFrame_data(showCANMsgs);
            dataWrapper.setSignal_data(showSignals);
            Log.d(TAG, "callbackjs  444444: " + dataWrapper.getFrame_data().toString()  );
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

        public  List<Map<String, Object>> parseMsgData(int BUSId, int CANId,byte[] data)
        {
            // 根据BUSID 和CANID找到里面有多少个signal
            List<SignalInfo> signalInfos = database.signalInfoDao().getSignal(BUSId, CANId);
            Log.d(TAG,"signalInfos " + signalInfos);
            List<Map<String, Object>> infos = new ArrayList<>();
            signalInfos.forEach(signalInfo -> {
                Map<String,Object> info = new HashMap<>();
                info.put("canId",signalInfo.CANId);
                info.put("channel",signalInfo.BUSId);
                info.put("comment",signalInfo.comment);
                long rawData = getSignal(signalInfo.bitStart,signalInfo.bitLength,data);
                info.put("hex",rawData);
                Log.d(TAG,"Info 11111111   " + info);
                String choices = signalInfo.choices;
                if(choices != null)
                {
                    Log.d(TAG,"rawDATA   222222    " + rawData);
                    JsonObject jsonObject = parseString(choices).getAsJsonObject();
                    Log.d(TAG,"jsonObject    " + jsonObject);
                    String enumStr = jsonObject.get(String.valueOf(rawData)).getAsString();
                    Log.d(TAG,"enumStr    " + enumStr);
                    info.put("value","0x" + Long.toHexString(rawData) +"-" +enumStr);
                }else {
                    info.put("value",String.valueOf(rawData));
                }
                info.put("id",String.valueOf(signalInfo.id));
                info.put("isChildTit",false);
                info.put("isExpand",true);
                info.put("isParent",false);
                info.put("name",signalInfo.getName());
                infos.add(info);
            });
            Log.d(TAG,"infos   " + infos);
            return infos;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        database = MyApplication.getInstance().getMx11E4Database();
        return m_binder;
    }



    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate");
        super.onCreate();


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

    private String getWorkHomeDir()
    {
        return Environment.getExternalStorageDirectory()+"/MICAN/";
    }


}