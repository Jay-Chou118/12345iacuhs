package com.example.testcdc;

import static com.google.gson.JsonParser.parseString;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.testcdc.MiCAN.DataWrapper;
import com.example.testcdc.MiCAN.DeviceInfo;
import com.example.testcdc.Utils.ResponseData;
import com.example.testcdc.Utils.Result;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity3 extends AppCompatActivity {

    private static final String TAG = "MICAN_MainActivity3";

    private static final String BRIDGE_NAME = "Android";

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

        webView = findViewById(R.id.webView1);
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
        checkPermission();
    }

    private void initWebView()
    {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);

        // 添加Java对象到JavaScript的window对象
//        webView.addJavascriptInterface(this, "Android");

        webView.addJavascriptInterface(new JsInterface(), BRIDGE_NAME);

        // 加载页面
        webView.loadUrl("file:///android_asset/index.html");

        bindService(new Intent(this, MyService.class),mSC, Context.BIND_AUTO_CREATE);

        messageHandlers.put("initDevice", new BridgeHandler() {
            @Override
            public void handle(JsonObject data, String callback) {
                if(mMiCANBinder != null)
                {
                    JsCallResult<Result<DeviceInfo>> jsCallResult = new JsCallResult<>(callback);
                    boolean ret = mMiCANBinder.InitModule();
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

        messageHandlers.put("showLoggingMessage", new BridgeHandler() {
            @Override
            public void handle(JsonObject data, String callback) {
                Log.d(TAG,"showLoggingMessage ");
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
            public void handle(JsonObject data, String callback) {
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


    }

    private class JsInterface {
        @JavascriptInterface
        public void send(String message) {
            Log.d(TAG,"i am recv "+ message);
            handleNativeResponse(message);
        }
    }

    private void handleNativeResponse(String responseData) {
        try {
            JsonObject jsonObject = parseString(responseData).getAsJsonObject();
            String method = jsonObject.get("method").getAsString();
            String callback = jsonObject.get("callback").getAsString();
            JsonObject data = jsonObject.get("data").getAsJsonObject();
            BridgeHandler handler = messageHandlers.get(method);
            Log.d(TAG,"method is : " + method );
            Log.d(TAG,"callback is : " + callback );
            Log.d(TAG,"data is : " + data );
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
        void handle(JsonObject data,String callback);
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



}