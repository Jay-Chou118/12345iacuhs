package com.example.testcdc;

import static android.database.sqlite.SQLiteDatabase.openDatabase;
import static com.chaquo.python.Python.start;
import static com.example.testcdc.Utils.Utils.parseDBCByPython;
import static com.example.testcdc.Utils.Utils.updateCustomData;
import static com.google.gson.JsonParser.parseString;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import com.example.testcdc.MiCAN.DataWrapper;
import com.example.testcdc.MiCAN.DeviceInfo;
import com.example.testcdc.Utils.BlfRequest;
import com.example.testcdc.Utils.DataBaseUtil;
import com.example.testcdc.Utils.ResponseData;
import com.example.testcdc.Utils.Result;
import com.example.testcdc.Utils.Utils;
import com.example.testcdc.database.Basic_DataBase;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;
import com.example.testcdc.httpServer.BlfService;
import com.example.testcdc.httpServer.FlaskService;
import com.example.testcdc.httpServer.HttpServer;
import com.example.testcdc.httpServer.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.http.POST;


public class MainActivity3 extends AppCompatActivity {

    private static final String TAG = "MICAN_MainActivity3";

    private static final String BRIDGE_NAME = "Android";


    public static final LinkedBlockingQueue<String> showLoggingMessageQueue = new LinkedBlockingQueue<>(10);

    private final Map<String, BridgeHandler> messageHandlers = new HashMap<>();

    private MyService.MiCANBinder mMiCANBinder;


    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final int READ_REQUEST_CODE = 1;
    private static final int CHOOSE_REQUEST_CODE = 2;

    private static long Current_cid;

    private static String mCallbackId;


    private ServiceConnection mSC = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            Log.i(TAG, "onServiceConnected");
            // 我们已经绑定了LocalService，强制类型转换IBinder对象并存储MyService的实例
            mMiCANBinder = (MyService.MiCANBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "onServiceDisconnected");
            mMiCANBinder = null;
        }
    };
    private WebView webView;

    MyApplication instance = MyApplication.getInstance();
    ;

    private static final String CALLBACK_JS_FORMAT = "javascript:JSBridge.handleNativeResponse('%s')";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        if (!Python.isStarted()) {
            start(new AndroidPlatform(this));
        }
        Python python = Python.getInstance();
        PyObject pyObject = python.getModule("app");
        //pyObject.callAttr("main");

        initWebView();

        Thread m = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);

                    }
                    if (mMiCANBinder != null) {
                        mMiCANBinder.printInfo();
                    }

                }
            }
        });
        m.start();
        showLoggingMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String callback = showLoggingMessageQueue.poll();
                    if (callback == null) {
                        Utils.wait10ms();
                        continue;
                    }
                    if (mMiCANBinder != null) {
                        JsCallResult<Result<DataWrapper>> jsCallResult = new JsCallResult<>(callback);
                        Result<DataWrapper> result = ResponseData.success(mMiCANBinder.getCurrentMsgs());
                        jsCallResult.setData(result);
                        final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                        webView.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl(callbackJs);
                            }
                        });
                    }
                }

            }
        });
        showLoggingMessage.start();
        checkPermission();

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            // 处理文件
            handleFile(data);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        database = MyApplication.getInstance().getDatabase();
    }


    private void initWebView() {
        webView = findViewById(R.id.webView1);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);

        // 添加Java对象到JavaScript的window对象
//        webView.addJavascriptInterface(this, "Android");

        webView.addJavascriptInterface(new JsInterface(), BRIDGE_NAME);

        // 加载页面
        webView.loadUrl("file:///android_asset/index.html");
//        webView.loadUrl("http://192.168.215.240:5173/#/");

        bindService(new Intent(this, MyService.class), mSC, Context.BIND_AUTO_CREATE);

        messageHandlers.put("initDevice", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                if (mMiCANBinder != null) {
                    JsCallResult<Result<DeviceInfo>> jsCallResult = new JsCallResult<>(callback);
                    boolean ret = mMiCANBinder.InitModule();
                    if (ret) {
                        instance.say("恭喜,初始化设备成功啦");
                    } else {
                        instance.say("抱歉,未能找到MiCAN设备,请重新插拔下设备试试看");
                    }
                    Result<DeviceInfo> result = ResponseData.ret(mMiCANBinder.getDeviceInfo(), ret);
                    jsCallResult.setData(result);
                    final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                    //Log.d(TAG, "callbackJs2 " + callbackJs);
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(callbackJs);
                        }
                    });
                    // 打开CANFD设备
                    mMiCANBinder.CANOnBus();
                    mMiCANBinder.startSaveBlf();
                }
            }
        });

        messageHandlers.put("checkHardware", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {

                Log.d(TAG, "TTTTTTTTTTTTTT: " + data);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "thread: " + Thread.currentThread().getId());
                        if (mMiCANBinder != null) {
                            JsCallResult<Result<DeviceInfo>> jsCallResult = new JsCallResult<>(callback);
                            boolean ret = mMiCANBinder.InitModule();
                            if (ret) {
                                instance.say("恭喜,初始化设备成功啦");
                            } else {
                                instance.say("抱歉,未能找到MiCAN设备,请重新插拔下设备试试看");
                            }
                            Result<DeviceInfo> result = ResponseData.ret(mMiCANBinder.getDeviceInfo(), ret);
                            jsCallResult.setData(result);
                            final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));

                            // 打开CANFD设备
                            mMiCANBinder.CANOnBus();
                            mMiCANBinder.startSaveBlf();


                            webView.post(new Runnable() {
                                @Override
                                public void run() {
                                    webView.loadUrl(callbackJs);
                                }
                            });
                        }

                    }
                }).start();
            }
        });

        messageHandlers.put("showLoggingMessage", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {

                showLoggingMessageQueue.add(callback);
                if (mMiCANBinder != null) {
                    JsCallResult<Result<DataWrapper>> jsCallResult = new JsCallResult<>(callback);
                    Result<DataWrapper> result = ResponseData.success(mMiCANBinder.getCurrentMsgs());
                    jsCallResult.setData(result);
                    final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(callbackJs);
                        }
                    });
                }
            }
        });

        messageHandlers.put("stopDevice", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                Log.d(TAG, "stopDevice ");
                if (mMiCANBinder != null) {
                    Log.d(TAG, "i am called");

                    mMiCANBinder.CANOffBus();
                    mMiCANBinder.stopSaveBlf();
                    sharedFile(mMiCANBinder.getFilePath());

//                    JsCallResult<Result<String>> jsCallResult = new JsCallResult<>(callback);
//                    final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
//
//                    webView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            webView.loadUrl(callbackJs);
//                        }
//                    });
                }
            }
        });

        messageHandlers.put("getDBC", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {


                String carType = data.getAsJsonObject().get("carType").getAsString();
                String sdb = data.getAsJsonObject().get("sdb").getAsString();
                JsonArray files = data.getAsJsonObject().get("files").getAsJsonArray();

                Map<Integer, Map<String, List<List<Object>>>> maps = new HashMap<>();
                ArrayList<Integer> BUSIdList = new ArrayList<>();

                JsCallResult<Result<Map<Integer, Map<String, List<List<Object>>>>>> jsCallResult = new JsCallResult<>(callback);

                // STEP 1 查找cid
                long cid = database.carTypeDao().getCidByName(carType, sdb);

                Current_cid = cid;


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "thread: " + Thread.currentThread().getId());
                        if (mMiCANBinder != null) {
                            //  i am recv {"method":"getDBC","data":{"sdb":"默认视图","carType":"custom","dbcChn":[1,2],"ldfChn":[],"files":["MS11_DCDCANFD_230807.dbc","MS11_ADASCANFD_220603.dbc","","","","","","","","","","","","","",""],"ldfFiles":["","","","","","","","","","","","","","","",""],"comments":["","","","","","","","","","","","","","","",""],"ldfComments":["","","","","","","","","","","","","","","",""]},"callback":"cb_1725500583492"}
                            if (carType.equals("custom")) {
                                // 如果是自定义，则要先把文件调用python方法解析入库，然后统一查找库

                                Log.e(TAG, "I am called 1");
                                //清库操作
                                database.signalInfoDao().deleteBycid(cid);
                                database.msgInfoDao().deleteBycid(cid);

                                AtomicInteger BUSId = new AtomicInteger();
                                files.forEach(file -> {
                                    String filePath = file.getAsString();
                                    BUSId.addAndGet(1);
                                    if (filePath.isEmpty()) return;
                                    BUSIdList.add(BUSId.get());
                                    // 解析dbc
                                    String content = parseDBCByPython(filePath);
                                    updateCustomData(content, cid, BUSId.get());

                                });
                                Log.e(TAG, "BUSIdList: " + BUSIdList + "CID " + cid);

                            } else {

                                Log.d(TAG, "I am called 2");
                                JsonArray dbcChn = data.getAsJsonObject().get("dbcChn").getAsJsonArray();
                                for (JsonElement element : dbcChn) {
                                    int value = element.getAsInt();
                                    BUSIdList.add(value);
                                }

                                Log.e(TAG, "BUSIdList: " + BUSIdList + "CID " + cid);
                            }



                            for (int busId : BUSIdList) {
                                Map<String, List<List<Object>>> subMap = new HashMap<>();
                                List<MsgInfoEntity> userMsgs = database.msgInfoDao().getMsgBycidBusId(busId, cid);
                                for (MsgInfoEntity usermsg : userMsgs) {
                                    List<List<Object>> subList = new ArrayList<>();
                                    List<SignalInfo> userSignalInfos = database.signalInfoDao().getSignalBycid(cid, busId, usermsg.CANId);
                                    for (SignalInfo signalInfo : userSignalInfos) {
                                        List<Object> subListItem = Arrays.asList(
                                                signalInfo.name,
//                                                signalInfo.comment,
                                                " ",
                                                "信号remark",
                                                signalInfo.id,
                                                signalInfo.initial,
                                                signalInfo.maximum,
                                                signalInfo.minimum,
                                                0,
                                                0,
                                                0,
                                                "m",
                                                signalInfo.bitStart,
                                                signalInfo.bitLength,
                                                1,
                                                signalInfo.scale,
                                                signalInfo.offset,
                                                signalInfo.byteOrder,
                                                signalInfo.isSigned
                                        );
                                        subList.add(subListItem);
                                    }
                                    subMap.put(usermsg.name, subList);
                                }
                                maps.put(busId, subMap);
                            }
                            Result<Map<Integer, Map<String, List<List<Object>>>>> result = ResponseData.success(maps);
                            jsCallResult.setData(result);
                            callJs(jsCallResult);

                        }
                        final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));

                        webView.post(new Runnable() {
                            @Override
                            public void run() {

                                webView.loadUrl(callbackJs);
                            }
                        });
                    }

                }).start();

                // step 2 查找入参要提取的信号数据
                // 首先要确定要提取哪几路bus信息
            }
        });

        messageHandlers.put("selectShowSignals", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                Log.d(TAG, "TTTTTTTTTTTTTT: " + data);
                List<Long> ids = new ArrayList<>();

                JsonArray array = data.getAsJsonArray();
                array.forEach(item -> {
                    int id = item.getAsJsonObject().get("id").getAsInt();
                    SignalInfo signalInfo = database.signalInfoDao().getSignalById(id);
                    ids.add(signalInfo.id);
                });
                mMiCANBinder.monitorSignal(ids);

                JsCallResult<Result<Object>> jsCallResult = new JsCallResult<>(callback);
                Result<Object> success = ResponseData.success();
                jsCallResult.setData(success);
//                Log.d(TAG, "GGGGGGGGGGG   " + jsCallResult);
                callJs(jsCallResult);
            }
        });


        messageHandlers.put("getUserDBC", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                // 用于文件回调时候调用js方法。
                mCallbackId = callback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/octet-stream");

                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.putExtra("filter", "*.dbc");

                startActivityForResult(intent, READ_REQUEST_CODE);
                Log.i(TAG, "start file select activity");

            }
        });

        messageHandlers.put("chooseBlfPath", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) throws IOException {
                mCallbackId = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/octet-stream");

                intent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(intent,READ_REQUEST_CODE);


                Python py = Python.getInstance();

                PyObject pyObject = py.getModule("test");
                new Thread(() -> pyObject.callAttr("run")).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pyObject.callAttr("run");
                        postBlfData("/storage/emulated/0/Download/Lark/MiCAN_record_2024-08-01_17_32_40_1.blf", "");
                    }
                });
//                sendBLFData("/storage/emulated/0/Download/Lark/MiCAN_record_2024-08-01_17_32_40_1.blf");

            }
        });

        messageHandlers.put("getBLFData", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                JsCallResult<Result<Map<Integer, Map<String, List<List<Object>>>>>> jsCallResult = new JsCallResult<>(callback);
                String filePath = data.getAsString();

            }
        });

        messageHandlers.put("chooseDBCPath", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                mCallbackId = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/octet-stream");

                intent.addCategory(Intent.CATEGORY_OPENABLE);

//                startActivityForResult(intent, CHOOSE_REQUEST_CODE);
                startActivityForResult(intent,READ_REQUEST_CODE);

            }
        });

        messageHandlers.put("parsedSignal", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                Log.d(TAG, "TTTTTTTTTTTTTT: " + data);

                if (data == null || !data.isJsonObject()) {
                    Log.e(TAG, "Received data is not a JSON object.");
                    return;
                }

                int BUSId = data.getAsJsonObject().get("channel").getAsInt();
                int CANId = data.getAsJsonObject().get("canId").getAsInt();
                JsonElement canDataElement = data.getAsJsonObject().get("canData");
                if (canDataElement == null || !canDataElement.isJsonArray()) {
                    Log.e(TAG, "No or invalid 'canData' found in the JSON object.");
                    return;
                }

                JsonArray dataArray = canDataElement.getAsJsonArray();
                int length = dataArray.size();
                byte[] CANData = new byte[length];
                Log.e(TAG, "0911" + "length:   " + length );
                Log.d(TAG, "0911" + data);
                for (int i = 0; i < length; i++) {
                    CANData[i] = (byte) Integer.parseInt(dataArray.get(i).getAsString(), 16);
                }

                Log.d(TAG, "CANData " + Arrays.toString(CANData));
                Log.d(TAG, "BUSId " + BUSId + " CANId " + CANId);
                List<Map<String, Object>> maps = new ArrayList<>();
                Map<String, Object> titleMap = new HashMap<>();
//                titleMap.put("canId", String.valueOf(CANId));
//                titleMap.put("channel", BUSId);
//                titleMap.put("id", "18028-");
//                titleMap.put("isChildTit", true);
//                titleMap.put("isExpand", true);
//                titleMap.put("isParent", false);
//                maps.add(titleMap);
//                maps.addAll(mMiCANBinder.parseMsgData(2, 0x90,CANData));
//                maps.addAll(mMiCANBinder.parseMsgData(BUSId, CANId, CANData));
                maps.addAll(mMiCANBinder.parseMsgData(BUSId, CANId, Current_cid, CANData));
//                Log.e(TAG, " 111111 444444 " + maps.toString());

                JsCallResult<Result<List<Map<String, Object>>>> jsCallResult = new JsCallResult<>(callback);
                Result<List<Map<String, Object>>> success = ResponseData.success(maps);
                jsCallResult.setData(success);
                Log.e(TAG, "1111111 222222 3333: " + jsCallResult.getData() );
                callJs(jsCallResult);
            }
        });

        messageHandlers.put("getManualSend", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) throws IOException {
                //Log.d(TAG, "TTTTTTTTTTTTTT: " + data);
                //{"row":1,"name":"","e2e":false,
                // "periodic":0,"canId":111,
                // "channel":1,
                // "canType":"CAN",
                // "dlc":8,
                // "isSending":false,
                // "from":"CAN",
                // "rawData":[0,0,0,0,0,0,0,0],
                // "children":[{"eteDisable":false}],"dirty":false,"raw":1}

//                mMiCANBinder.CANOnBus();
                Log.d(TAG, "TTTTTTT get in Once");
                mMiCANBinder.SendOnce(data);


            }
        });
        //getPeriodicSend
        messageHandlers.put("getPeriodicSend", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) throws IOException {

//                mMiCANBinder.CANOnBus();
                Log.d(TAG, "RRRRR get in Periods" + data);
                JsonObject jsonObject = data.getAsJsonObject();
                JsonArray dataArray = jsonObject.getAsJsonArray("data");

//                Boolean isSending = JsonElement
                mMiCANBinder.SendPeriods(data);


            }
        });

        messageHandlers.put("getRawValueToPhysValue", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) throws IOException {
                //{"dbcChannel":2,"canId":971,
                // "signalsData":[{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotLftRemaDCLk","id":"RLMotLftRemaDCLk","msg":971,"channel":2,"text":"RLMotLftRemaDCLk","node_type":"signal","comment":" ","remark":"信号remark","canId":162469,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":7,"length":16,"physStep":1,"dlc":0.002,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotLftRemaDCLk"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotLftRemaPwrModl","id":"RLMotLftRemaPwrModl","msg":971,"channel":2,"text":"RLMotLftRemaPwrModl","node_type":"signal","comment":" ","remark":"信号remark","canId":162470,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":23,"length":16,"physStep":1,"dlc":0.002,"canType":0,"periodic":false,"eteDisable":false,"name":"RLMotLftRemaPwrModl"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTAvrgDiodeMin","id":"RLMotTAvrgDiodeMin","msg":971,"channel":2,"text":"RLMotTAvrgDiodeMin","node_type":"signal","comment":" ","remark":"信号remark","canId":162471,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":39,"length":8,"physStep":1,"dlc":1,"canType":-40,"periodic":false,"eteDisable":false,"name":"RLMotTAvrgDiodeMin"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTAvrgMosMax","id":"RLMotTAvrgMosMax","msg":971,"channel":2,"text":"RLMotTAvrgMosMax","node_type":"signal","comment":" ","remark":"信号remark","canId":162472,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":47,"length":8,"physStep":1,"dlc":1,"canType":-40,"periodic":false,"eteDisable":false,"name":"RLMotTAvrgMosMax"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTAvrgMosMin","id":"RLMotTAvrgMosMin","msg":971,"channel":2,"text":"RLMotTAvrgMosMin","node_type":"signal","comment":" ","remark":"信号remark","canId":162473,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":55,"length":8,"physStep":1,"dlc":1,"canType":-40,"periodic":false,"eteDisable":false,"name":"RLMotTAvrgMosMin"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTCoolOilMax","id":"RLMotTCoolOilMax","msg":971,"channel":2,"text":"RLMotTCoolOilMax","node_type":"signal","comment":" ","remark":"信号remark","canId":162474,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":63,"length":8,"physStep":1,"dlc":1,"canType":-40,"periodic":false,"eteDisable":false,"name":"RLMotTCoolOilMax"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTCoolOilMin","id":"RLMotTCoolOilMin","msg":971,"channel":2,"text":"RLMotTCoolOilMin","node_type":"signal","comment":" ","remark":"信号remark","canId":162475,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":71,"length":8,"physStep":1,"dlc":1,"canType":-40,"periodic":false,"eteDisable":false,"name":"RLMotTCoolOilMin"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTCoolWtrMax","id":"RLMotTCoolWtrMax","msg":971,"channel":2,"text":"RLMotTCoolWtrMax","node_type":"signal","comment":" ","remark":"信号remark","canId":162476,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":79,"length":8,"physStep":1,"dlc":1,"canType":-40,"periodic":false,"eteDisable":false,"name":"RLMotTCoolWtrMax"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTCoolWtrMin","id":"RLMotTCoolWtrMin","msg":971,"channel":2,"text":"RLMotTCoolWtrMin","node_type":"signal","comment":" ","remark":"信号remark","canId":162477,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"unit":"m","startBit":87,"length":8,"physStep":1,"dlc":1,"canType":-40,"periodic":false,"eteDisable":false,"name":"RLMotTCoolWtrMin"},{"_id":"2_RLEDS_PTFusionCANFD_0x3CB_RLMotTDCLkCapMax","id":"RLMotTDCLkCapMax","msg":971,"channel":2,"text":"RLMotTDCLkCapMax","node_type":"signal","comment":" ","remark":"信号remark","canId":162478,"rawValue":0,"maxRaw":0,"minRaw":0,"maxPhys":0,"minPhys":0,"selectPhys":0,"un

                Log.d(TAG, "getRawValueToPhysValue data :" + data);

            }
        });


    }

    public void postBlfData(String filePath, String mode) {
        OkHttpClient client = new OkHttpClient();

        // 创建请求体
        BlfRequest requestBody = new BlfRequest(filePath);
        Gson gson = new Gson();
        String json = gson.toJson(requestBody);

        // 创建 POST 请求
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url("http://127.0.0.1:8080/blft/getBLFdata") // 替换为你的服务器 IP
                .post(body)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // 处理响应数据
                    Log.d("Response", responseData);
                } else {
                    Log.e("Error", "请求失败: " + response.code());
                }
            }
        });
    }

    private <T> void callJs(T result) {
        final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(result));
        Log.d(TAG, "callbackJs 5" + callbackJs);
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(callbackJs);
            }
        });
    }

    private class JsInterface {
        @JavascriptInterface
        public void send(String message) {
            Log.d(TAG, "i am recv " + message);

//            instance.test7();
            handleNativeResponse(message);
        }
    }

    private Basic_DataBase database;


    private Thread showLoggingMessage;

    private void handleNativeResponse(String responseData) {
        try {
            JsonObject jsonObject = parseString(responseData).getAsJsonObject();
            String method = jsonObject.get("method").getAsString();
            String callback = jsonObject.get("callback").getAsString();
            JsonElement data = jsonObject.get("data");
            BridgeHandler handler = messageHandlers.get(method);
            Log.i(TAG, "method is : " + method);
            Log.i(TAG, "callback is : " + callback);
            Log.i(TAG, "data is : " + data);
            if (handler != null) {
                handler.handle(data, callback);
            } else {
                Log.w(TAG, "BridgeHandler is null");
            }


        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public interface BridgeHandler {
        void handle(JsonElement data, String callback) throws IOException;
    }

    public interface MyRunnable extends Runnable {
        public MyRunnable setParam(String param);
    }

    private void sharedFile(String filePath) {
        // 获取要分享的文件
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, "fileprovider", file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("*/*");
        startActivity(Intent.createChooser(intent, "分享录制文件"));
    }

    private void checkPermission() {
        boolean externalStorageManager = Environment.isExternalStorageManager();
        Log.e(TAG, "externalStorageManager: " + externalStorageManager);
        if (!externalStorageManager) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void handleFile(Uri fileUri) {
        Log.i(TAG, "uri getAuthority: " + fileUri.getAuthority());
        Log.i(TAG, "uri schema: " + fileUri.getScheme());
        Log.i(TAG, "Uri: " + fileUri + "\t path: " + fileUri.getPath());
        Log.i(TAG, "Uri: " + fileUri + "\t path: " + getRealPathFromURI(fileUri));
//        readFileFromUri(this,fileUri);
        // 在此处处理BLF文件，例如读取文件内容或进行解析
        // 检查是否为 content:// 方式的 Uri
        if (fileUri.getScheme().equals("content")) {
            String path = getRealPathFromURI(fileUri);
            Log.i(TAG, "Uri: " + fileUri + "\t real path: " + path);
            // 使用 path 进行后续操作
        } else if (fileUri.getScheme().equals("file")) {
            // 直接使用 file 方式的 Uri
            String path = fileUri.getPath();
            Log.i(TAG, "Uri: " + fileUri + "\t path: " + path);
            // 使用 path 进行后续操作
        } else {
            // 不支持的 Uri 方式
            Log.e(TAG, "Unsupported URI scheme: " + fileUri.getScheme());
        }

//        // 进行文件读取
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(fileUri, "*/*");
//        startActivity(intent);
    }

    public String getFilePathFromUri(Uri uri) {
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

        cursor.moveToFirst();
//        String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        cursor.close();
        return "";
    }

    private String getRealPathFromURI(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.moveToFirst()) {
            // 获取你需要的列信息，例如文件的MIME类型
            @SuppressLint("Range") String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
            Log.e(TAG, "mimeType: " + mimeType);
            // 处理你的文件信息
            // ...
        }


        Log.e(TAG, "cursor " + cursor.toString());
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public String getPathFromUri(Context context, Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        String path = documentFile.getUri().getPath();
        Log.d(TAG, "EEEEEEE : " + path);
        if (path.startsWith("/document/raw:")) {
            path = path.substring("/document/raw:".length());
            Log.d("pathTest: ", path);
        } else {
            path = "/storage/emulated/0/Download/Lark/" + getFileNameFromUri(this, uri);
            Log.d("pathTest: ", path);
        }
        return path;
    }

    private String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;
        String[] projection = {MediaStore.Files.FileColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
            fileName = cursor.getString(columnIndex);
            cursor.close();
        }
        return fileName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri uri = data.getData();
            // 获取文件名
            String fileName = getFileNameFromUri(this, uri);
            // 获取文件路径
            String filePath = getPathFromUri(this, uri);

            if (mCallbackId == null) {
                Log.e(TAG, "mCallbackId is null,please check it");
                return;
            }
            JsCallResult<Result<Object>> jsCallResult = new JsCallResult<>(mCallbackId);
            Result<Object> success = ResponseData.success();
            success.setData(filePath);
            jsCallResult.setData(success);
            callJs(jsCallResult);
            mCallbackId = null;

        }
    }

    public String readFileFromUri(Context context, Uri fileUri) {
        String fileContent = "";
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = context.getContentResolver().openInputStream(fileUri);
            Log.d(TAG, "inputStream " + inputStream.toString());
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            fileContent = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileContent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}