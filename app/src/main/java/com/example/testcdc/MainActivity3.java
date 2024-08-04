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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.testcdc.MiCAN.DataWrapper;
import com.example.testcdc.MiCAN.DeviceInfo;
import com.example.testcdc.Utils.ResponseData;
import com.example.testcdc.Utils.Result;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            // 处理文件
            handleFile(data);
        }
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