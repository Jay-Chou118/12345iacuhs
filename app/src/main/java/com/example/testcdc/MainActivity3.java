package com.example.testcdc;

import static com.google.gson.JsonParser.parseString;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.testcdc.MiCAN.DataWrapper;
import com.example.testcdc.MiCAN.DeviceInfo;
import com.example.testcdc.Utils.DataBaseUtil;
import com.example.testcdc.Utils.ResponseData;
import com.example.testcdc.Utils.Result;
import com.example.testcdc.Utils.ToastUtil;
import com.example.testcdc.Utils.Utils;
import com.example.testcdc.database.MX11E4Database;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainActivity3 extends AppCompatActivity {

    private static final String TAG = "MICAN_MainActivity3";

    private static final String BRIDGE_NAME = "Android";


    public static final LinkedBlockingQueue<String> showLoggingMessageQueue = new LinkedBlockingQueue<>(10);

    private final Map<String, BridgeHandler> messageHandlers = new HashMap<>();

    private MyService.MiCANBinder mMiCANBinder;


    private ServiceConnection mSC = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            Log.i(TAG,"onServiceConnected");
            // 我们已经绑定了LocalService，强制类型转换IBinder对象并存储MyService的实例
            mMiCANBinder = (MyService.MiCANBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG,"onServiceDisconnected");
            mMiCANBinder = null;
        }
    };
    private WebView webView;

    MyApplication instance = MyApplication.getInstance();;

    private static final String CALLBACK_JS_FORMAT = "javascript:JSBridge.handleNativeResponse('%s')";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initWebView();

        Thread m = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);

                    }
                    if(mMiCANBinder!=null)
                    {
                        mMiCANBinder.printInfo();
                    }

                }
            }
        });
        m.start();
        showLoggingMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    String callback = showLoggingMessageQueue.poll();
                    if(callback==null)
                    {
                        Utils.wait10ms();
                        continue;
                    }
                    if(mMiCANBinder != null)
                    {
                        JsCallResult<Result<DataWrapper>> jsCallResult = new JsCallResult<>(callback);
                        Result<DataWrapper> result = ResponseData.success(mMiCANBinder.getCurrentMsgs());
                        jsCallResult.setData(result);
                        final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                        Log.d(TAG,"callbackJs "+ callbackJs );
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


    private void initWebView()
    {
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

        bindService(new Intent(this, MyService.class),mSC, Context.BIND_AUTO_CREATE);

        messageHandlers.put("initDevice", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                if(mMiCANBinder != null)
                {
                    JsCallResult<Result<DeviceInfo>> jsCallResult = new JsCallResult<>(callback);
                    boolean ret = mMiCANBinder.InitModule();
                    if(ret)
                    {
                        instance.say("恭喜,初始化设备成功拉");
                    }else{
                        instance.say("抱歉,未能找到MiCAN设备,请重新插拔下设备试试看");
                    }
                    Result<DeviceInfo> result = ResponseData.ret(mMiCANBinder.getDeviceInfo(),ret);
                    jsCallResult.setData(result);
                    final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                    Log.d(TAG,"callbackJs "+ callbackJs );
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


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG,"thread: " + Thread.currentThread().getId());
                        if(mMiCANBinder != null)
                        {
                            JsCallResult<Result<DeviceInfo>> jsCallResult = new JsCallResult<>(callback);
                            boolean ret = mMiCANBinder.InitModule();
                            if(ret)
                            {
                                instance.say("恭喜,初始化设备成功拉");
                            }else{
                                instance.say("抱歉,未能找到MiCAN设备,请重新插拔下设备试试看");
                            }
                            Result<DeviceInfo> result = ResponseData.ret(mMiCANBinder.getDeviceInfo(),ret);
                            jsCallResult.setData(result);
                            final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                            Log.i(TAG,"callbackJs "+ callbackJs );
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

//                showLoggingMessageQueue.add(callback);
                if(mMiCANBinder != null)
                {
                    JsCallResult<Result<DataWrapper>> jsCallResult = new JsCallResult<>(callback);
                    Result<DataWrapper> result = ResponseData.success(mMiCANBinder.getCurrentMsgs());
                    jsCallResult.setData(result);
                    final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                    Log.d(TAG,"callbackJs "+ callbackJs );
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
                Log.d(TAG,"stopDevice ");
                if(mMiCANBinder != null)
                {
                    Log.d(TAG,"i am called");

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

                Log.i(TAG,"getDBC ");
                if(mMiCANBinder != null)
                {
                    Log.d(TAG,"i am called");
                    // 进行报文查询
                    Map<Integer,Map<String,List<List<Object>>>> maps = new HashMap<>();

                    ArrayList<Integer> BUSIdList = new ArrayList<>();
                    BUSIdList.add(1);
                    BUSIdList.add(2);
                    BUSIdList.add(3);
                    BUSIdList.add(4);
                    BUSIdList.add(6);
                    BUSIdList.add(7);
                    BUSIdList.forEach(id->{
                        Map<String,List<List<Object>>> subMap = new HashMap<>();
                        List<MsgInfoEntity> msgs = database.msgInfoDao().getMsg(id);
                        msgs.forEach(msg->{
                            List<List<Object>> subList = new ArrayList<>();
                            // 根据busid 和 canid查询
                            List<SignalInfo> signalInfos = database.signalInfoDao().getSignal(id, msg.CANId);
                            signalInfos.forEach(signalInfo -> {
                                List<Object> subList_ = new ArrayList<>();
                                subList_.add(signalInfo.name);
                                subList_.add("信号comment");
                                subList_.add("信号remark");
                                subList_.add(signalInfo.id);
                                subList_.add(0);    // 初始值
                                subList_.add(0);    // 最大
                                subList_.add(0);    // 最小值
                                subList_.add(0);    // max
                                subList_.add(0);    // min
                                subList_.add(0);    // values
                                subList_.add("m");    // values
                                subList_.add(27);    // startBit
                                subList_.add(12);    // bitLength
                                subList_.add(1);    // factory
                                subList_.add(32);    // factory
                                subList_.add("CANFD");    // factory
                                subList_.add(0);    // factory
                                subList_.add(false);    // factory
                                subList_.add(signalInfo.id);    // factory
                                subList.add(subList_);
                            });
                            subMap.put(msg.name,subList);
                        });
                        maps.put(id,subMap);
                    });

                    JsCallResult<Result<Map<Integer,Map<String,List<List<Object>>>>>> jsCallResult = new JsCallResult<>(callback);
                    Result<Map<Integer,Map<String,List<List<Object>>>>> result = ResponseData.success(maps);
                    jsCallResult.setData(result);
                    callJs(jsCallResult);
                }
                Log.i(TAG,"getDBC finish");
            }
        });

        messageHandlers.put("selectShowSignals", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {

                List<Long> ids = new ArrayList<>();

                JsonArray array = data.getAsJsonArray();
                array.forEach(item->{
                    int id = item.getAsJsonObject().get("id").getAsInt();
                    SignalInfo signalInfo = database.signalInfoDao().getSignalById(id);
                    ids.add(signalInfo.id);
                });
                mMiCANBinder.monitorSignal(ids);

                JsCallResult<Result<Object>> jsCallResult = new JsCallResult<>(callback);
                Result<Object> success = ResponseData.success();
                jsCallResult.setData(success);

                callJs(jsCallResult);
            }
        });

        messageHandlers.put("parsedSignal", new BridgeHandler() {
            @Override
            public void handle(JsonElement data, String callback) {
                int BUSId = data.getAsJsonObject().get("channel").getAsInt();
                int CANId = data.getAsJsonObject().get("canId").getAsInt();
                JsonArray dataArray = data.getAsJsonObject().get("canData").getAsJsonArray();
                int length = dataArray.size();
                byte[] CANData = new byte[length];

                for(int i=0;i<length;i++)
                {
                    CANData[i] =  (byte) Integer.parseInt(dataArray.get(i).getAsString(),16);
                }
                Log.e(TAG,"CANData " + Arrays.toString(CANData));
                Log.e(TAG,"BUSId " + BUSId + " CANId" +CANId);

                List<Map<String, Object>> maps = new ArrayList<>();
                Map<String,Object> titleMap = new HashMap<>();
                titleMap.put("canId",String.valueOf(CANId));
                titleMap.put("channel",BUSId);
                titleMap.put("id","18028-");
                titleMap.put("isChildTit",true);
                titleMap.put("isExpand",true);
                titleMap.put("isParent",false);
                maps.add(titleMap);
                maps.addAll(mMiCANBinder.parseMsgData(2, 0x90,CANData));
                Log.e(TAG,maps.toString());

                JsCallResult<Result<List<Map<String, Object>>>> jsCallResult = new JsCallResult<>(callback);
                Result<List<Map<String, Object>>> success = ResponseData.success(maps);
                jsCallResult.setData(success);

                callJs(jsCallResult);
            }
        });
    }


    private <T> void callJs(T result)
    {
        final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(result));
        Log.d(TAG,"callbackJs "+ callbackJs );
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
            Log.d(TAG,"i am recv "+ message);

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
            Log.i(TAG,"method is : " + method );
            Log.i(TAG,"callback is : " + callback );
            Log.i(TAG,"data is : " + data );
            if(handler!=null)
            {
                handler.handle(data,callback);
            }else
            {
                Log.w(TAG,"BridgeHandler is null");
            }


        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }
    }

    public interface BridgeHandler {
        void handle(JsonElement data,String callback);
    }

    public interface MyRunnable extends Runnable {
        public MyRunnable setParam(String param);
    }

    private void sharedFile(String filePath)
    {
        // 获取要分享的文件
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this,"fileprovider",file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("*/*");
        startActivity(Intent.createChooser(intent, "分享录制文件"));
    }

    private void checkPermission()
    {
        boolean externalStorageManager = Environment.isExternalStorageManager();
        Log.e(TAG,"externalStorageManager: " +externalStorageManager );
        if(!externalStorageManager)
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()) );
            startActivity(intent);
        }
    }

    private void handleFile(Uri fileUri) {
        Log.i(TAG,"uri getAuthority: " + fileUri.getAuthority());
        Log.i(TAG,"uri schema: " + fileUri.getScheme());
        Log.i(TAG,"Uri: " + fileUri + "\t path: " + fileUri.getPath());
        Log.i(TAG,"Uri: " + fileUri + "\t path: " + getRealPathFromURI(fileUri));
//        readFileFromUri(this,fileUri);
        // 在此处处理BLF文件，例如读取文件内容或进行解析

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
            Log.e(TAG,"mimeType: " +mimeType );
            // 处理你的文件信息
            // ...
        }


        Log.e(TAG,"cursor " + cursor.toString());
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public String getPathFromUri(Context context, Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        String path = documentFile.getUri().getPath();
        return path;
    }

    public String readFileFromUri(Context context, Uri fileUri) {
        String fileContent = "";
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = context.getContentResolver().openInputStream(fileUri);
            Log.d(TAG,"inputStream " + inputStream.toString());
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






    }