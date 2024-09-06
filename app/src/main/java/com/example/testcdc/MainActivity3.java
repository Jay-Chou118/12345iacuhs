package com.example.testcdc;

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
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import com.example.testcdc.MiCAN.DataWrapper;
import com.example.testcdc.MiCAN.DeviceInfo;
import com.example.testcdc.Utils.ResponseData;
import com.example.testcdc.Utils.Result;
import com.example.testcdc.Utils.Utils;
import com.example.testcdc.database.MX11E4Database;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;
import com.example.testcdc.entity.UserMsgEntity;
import com.example.testcdc.entity.UserSignalEntity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class MainActivity3 extends AppCompatActivity {

    private static final String TAG = "MICAN_MainActivity3";

    private static final String BRIDGE_NAME = "Android";


    public static final LinkedBlockingQueue<String> showLoggingMessageQueue = new LinkedBlockingQueue<>(10);

    private final Map<String, BridgeHandler> messageHandlers = new HashMap<>();

    private MyService.MiCANBinder mMiCANBinder;


    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final int READ_REQUEST_CODE = 1;
//    private static final int READ_REQUEST_CHNANEL2_CODE = 2;

    private static String mCallbackId;

    private String selectedFilePath; // 添加成员变量来保存选中的文件路径

    private String selectedCallback; // 用于存储从 JavaScript 调用过来的回调函数名称

    private JsCallResult<Result<Object>> selectedJsCallResult; // 用于存储 JsCallResult 实例

    //切换通道
    private int BusId;

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
            Python.start(new AndroidPlatform(this));
        }

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
                        Log.d(TAG, "callbackJs1 " + callbackJs);
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
        database = MyApplication.getInstance().getMx11E4Database();
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
                    Log.d(TAG, "callbackJs2 " + callbackJs);
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
                            Log.i(TAG, "callbackJs " + callbackJs);
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
                    Log.d(TAG, "callbackJs 44: " + result);
                    final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                    Log.d(TAG, "callbackJs 4" + callbackJs);
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
                ArrayList<Integer> BUSIdList = new ArrayList<>();;

                // STEP 1 查找cid
                long cid = database.carTypeDao().getCidByName(carType, sdb);

                if (mMiCANBinder != null) {
                    //  i am recv {"method":"getDBC","data":{"sdb":"默认视图","carType":"custom","dbcChn":[1,2],"ldfChn":[],"files":["MS11_DCDCANFD_230807.dbc","MS11_ADASCANFD_220603.dbc","","","","","","","","","","","","","",""],"ldfFiles":["","","","","","","","","","","","","","","",""],"comments":["","","","","","","","","","","","","","","",""],"ldfComments":["","","","","","","","","","","","","","","",""]},"callback":"cb_1725500583492"}
                    if (carType.equals("custom")) {
                        // 如果是自定义，则要先把文件调用python方法解析入库，然后统一查找库

                        Log.e(TAG, "I am called 1");
                        //清库操作
//                        database.signalInfoDao().deleteBycid(cid);
//                        database.msgInfoDao().deleteBycid(cid);

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

                    }else{

                        Log.d(TAG, "I am called 2");
                        JsonArray dbcChn = data.getAsJsonObject().get("dbcChn").getAsJsonArray();
                        for (JsonElement element : dbcChn) {
                            int value = element.getAsInt();
                            BUSIdList.add(value);
                        }

                        Log.e(TAG, "BUSIdList: " + BUSIdList + "CID " + cid);
                    }


                        // step 2 查找入参要提取的信号数据
                        // 首先要确定要提取哪几路bus信息
                        for (int busId : BUSIdList) {
                            Map<String, List<List<Object>>> subMap = new HashMap<>();
                            List<MsgInfoEntity> userMsgs = database.msgInfoDao().getMsgBycidBusId(busId, cid);
                            for (MsgInfoEntity usermsg : userMsgs) {
                                List<List<Object>> subList = new ArrayList<>();
                                List<SignalInfo> userSignalInfos = database.signalInfoDao().getSignalBycid(cid, busId, usermsg.CANId);
                                for (SignalInfo signalInfo : userSignalInfos) {
                                    List<Object> subListItem = Arrays.asList(
                                            signalInfo.name,
                                            signalInfo.comment,
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
                    JsCallResult<Result<Map<Integer, Map<String, List<List<Object>>>>>> jsCallResult = new JsCallResult<>(callback);
                    Result<Map<Integer, Map<String, List<List<Object>>>>> result = ResponseData.success(maps);
                    Log.d(TAG, "ZZZZZZZZZZ : result  " + result);
                    jsCallResult.setData(result);
                    Log.d(TAG, "ZZZZZZZZZZ : jsCallResult   " + jsCallResult.toString());
                    callJs(jsCallResult);
                }





//                if (mMiCANBinder != null) {
//                        Log.d(TAG, "I am called 2 ");
//                        Map<Integer, Map<String, List<List<Object>>>> maps = new HashMap<>();
//
//
//                        // step1 查找要解析的bus通道。
//                        // 从入参获取 dbcChal，1，2，
////                        BUSIdList = [1,2]
//                        ArrayList<Integer> BUSIdList = userdatabase.userSignalInfoDao().getBusIdsAsArrayList();
//
//                        BUSIdList.forEach(Busid -> {
//                            Map<String, List<List<Object>>> subMap = new HashMap<>();
//                            List<UserMsgEntity> usermsgs = userdatabase.userMsgInfoDao().getUserMsg(Busid);
//                            usermsgs.forEach(usermsg -> {
//                                List<List<Object>> subList = new ArrayList<>();
//                                // 根据busid 和 canid查询
//                                List<UserSignalEntity> UsersignalInfos = userdatabase.userSignalInfoDao().getUserSignal(Busid, usermsg.CANId);
//                                UsersignalInfos.forEach(UsersignalInfo -> {
//                                    List<Object> subList_ = new ArrayList<>();
//                                    subList_.add(UsersignalInfo.name);
//                                    subList_.add("信号comment");
//                                    subList_.add("信号remark");
//                                    subList_.add(UsersignalInfo.id);
//                                    subList_.add(0);    // 初始值
//                                    subList_.add(0);    // 最大
//                                    subList_.add(0);    // 最小值
//                                    subList_.add(0);    // max
//                                    subList_.add(0);    // min
//                                    subList_.add(0);    // values
//                                    subList_.add("m");    // values
//                                    subList_.add(UsersignalInfo.bitStart);    // startBit
//                                    subList_.add(UsersignalInfo.bitLength);    // bitLength
//                                    subList_.add(1);    // factory
//                                    subList_.add(32);    // factory
//                                    subList_.add(usermsg.CANType);    // factory
//                                    subList_.add(0);    // factory
//                                    subList_.add(false);    // factory
////                                subList_.add(signalInfo.id);    // factory
//                                    subList.add(subList_);
//
//                                });
//                                subMap.put(usermsg.name, subList);
//
//                            });
//                            maps.put(Busid, subMap);
//
//                        });
//
//                        JsCallResult<Result<Map<Integer, Map<String, List<List<Object>>>>>> jsCallResult = new JsCallResult<>(callback);
//                        Result<Map<Integer, Map<String, List<List<Object>>>>> result = ResponseData.success(maps);
//                        jsCallResult.setData(result);
//                        Log.d(TAG, "ZZZZZZZZZZZZZZZZZZZ2: " + result + " ZZZZZZZZZC C CC " + jsCallResult + "");
//                        callJs(jsCallResult);
//
//
//                }
                Log.i(TAG, "getDBC finish");
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

                startActivityForResult(intent, READ_REQUEST_CODE);
                Log.i(TAG, "start file select activity");

            }
        });

//        messageHandlers.put("getUserDBC2", new BridgeHandler() {
//            @Override
//            public void handle(JsonElement data, String callback) {
//                mCallbackId = callback;
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//                startActivityForResult(intent, READ_REQUEST_CHNANEL1_CODE);
//            }
//        });


        messageHandlers.put("parsedSignal", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                Log.d(TAG, "TTTTTTTTTTTTTT: " + data);
                int BUSId = data.getAsJsonObject().get("channel").getAsInt();
                int CANId = data.getAsJsonObject().get("canId").getAsInt();
                JsonArray dataArray = data.getAsJsonObject().get("canData").getAsJsonArray();
                int length = dataArray.size();
                byte[] CANData = new byte[length];

                for (int i = 0; i < length; i++) {
                    CANData[i] = (byte) Integer.parseInt(dataArray.get(i).getAsString(), 16);
                }
                Log.d(TAG, "CANData " + Arrays.toString(CANData));
                Log.d(TAG, "BUSId " + BUSId + " CANId " + CANId);

                List<Map<String, Object>> maps = new ArrayList<>();
                Map<String, Object> titleMap = new HashMap<>();
                titleMap.put("canId", String.valueOf(CANId));
                titleMap.put("channel", BUSId);
                titleMap.put("id", "18028-");
                titleMap.put("isChildTit", true);
                titleMap.put("isExpand", true);
                titleMap.put("isParent", false);
                maps.add(titleMap);
//                maps.addAll(mMiCANBinder.parseMsgData(2, 0x90,CANData));
                maps.addAll(mMiCANBinder.parseMsgData(BUSId, CANId, CANData));
                Log.e(TAG, maps.toString());

                JsCallResult<Result<List<Map<String, Object>>>> jsCallResult = new JsCallResult<>(callback);
                Result<List<Map<String, Object>>> success = ResponseData.success(maps);
                jsCallResult.setData(success);

                callJs(jsCallResult);
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

    private MX11E4Database database;


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
        void handle(JsonElement data, String callback);
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

            Log.e(TAG, "I am in channel 1 ");
            Uri uri = data.getData();
            // 获取文件名
            String fileName = getFileNameFromUri(this, uri);
            // 获取文件路径
            String filePath = getPathFromUri(this, uri);
            Log.d(TAG, "Parse DBC1 by User finished");

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

    public static void runInBackground(Runnable task) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(task);
        executorService.shutdown();
    }

}