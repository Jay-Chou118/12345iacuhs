package com.example.testcdc;

import static com.example.testcdc.MyService.gRecvMsgNum;
import static com.google.gson.JsonParser.parseString;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.testcdc.MiCAN.DataWrapper;
import com.example.testcdc.MiCAN.DeviceInfo;
import com.example.testcdc.MiCAN.ShowCANMsg;
import com.example.testcdc.Utils.ResponseData;
import com.example.testcdc.Utils.Result;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MICAN_MainActivity2";

    private static final String BRIDGE_NAME = "Android";

    private static final String CALLBACK_JS_FORMAT = "javascript:JSBridge.handleNativeResponse('%s')";
    public WebView webView;


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
    ArrayList<String> dataList;
    ListView listView;
    ArrayAdapter<String> adapter;

    private final Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkPermission();
        webView = findViewById(R.id.webView);
        Intent intent = new Intent(this, MyService.class);
//        startService(intent);
        startForegroundService(intent);

        checkNotify();
//        checkPower();

        findViewById(R.id.button11).setOnClickListener(v -> {
            Intent intent1 = new Intent(MainActivity2.this, MyService.class);
            startForegroundService(intent1);
        });
        bindService(new Intent(MainActivity2.this, MyService.class),mSC, Context.BIND_AUTO_CREATE);

        findViewById(R.id.button12).setOnClickListener(v -> {
            bindService(new Intent(MainActivity2.this, MyService.class),mSC, Context.BIND_AUTO_CREATE);
        });

        findViewById(R.id.button13).setOnClickListener(v -> {
            if(mMiCANBinder == null)
            {
                bindService(new Intent(MainActivity2.this, MyService.class),mSC, Context.BIND_AUTO_CREATE);
            }else{
                mMiCANBinder.sayHello();
            }
        });
        TextView tv = findViewById(R.id.textView2);
        findViewById(R.id.button14).setOnClickListener(v -> {
            dataList.clear();
            adapter.notifyDataSetChanged(); // 通知ListView刷新显示
            if(mMiCANBinder.InitModule())
            {
                Log.i(TAG,"InitModule ret ok");
                tv.setText("版本号: " + mMiCANBinder.getAppVersion() + "\t SN: " + mMiCANBinder.getSN());
                mMiCANBinder.CANOnBus();
                mMiCANBinder.startSaveBlf(MainActivity2.this);
            }else{
                Log.e(TAG,"InitModule ret failed");
            }
        });
        findViewById(R.id.button15).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMiCANBinder.stopSaveBlf();
                sharedFile(mMiCANBinder.getFilePath());
            }
        });
        findViewById(R.id.button16).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileManager();
            }
        });
        dataList = new ArrayList<>();
        listView = findViewById(R.id.listview);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);


        // 启用JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);

        // 添加Java对象到JavaScript的window对象
//        webView.addJavascriptInterface(this, "Android");

        webView.addJavascriptInterface(new JsInterface(), BRIDGE_NAME);

        // 加载页面
        webView.loadUrl("file:///android_asset/jsbridge.html");

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
                    Message msg = Message.obtain();
                    msg.what = 1;
                    mHandler.sendMessage(msg);

                }
            }
        });
        m.start();
//        PackageManager packageManager = getPackageManager();
//        try {
//            ActivityInfo activityInfo = packageManager.getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
//            Bundle metaData = activityInfo.metaData;
//            Log.i("YULAI","GET " +metaData.getString("yy"));
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        // 进行回调函数的注册

        messageHandlers.put("method1", new BridgeHandler() {
            @Override
            public void handle(JsonObject data, String callback) {
                JsCallResult<List<CanMessage>> jsCallResult = new JsCallResult<>(callback);
                jsCallResult.setData(new ArrayList<>(20000));
                for (int i = 0; i < 20000; i++) {
                    jsCallResult.getData().add(new CanMessage()); // 使用默认值0初始化
                }


                final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
                Log.d(TAG,data.toString());
                Log.d(TAG,"method1 "+ callback );
//                MainActivity2.this.webView.loadUrl(callbackJs);
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(callbackJs);
                    }
                });

            }
        });

        messageHandlers.put("getDeviceInfo", new BridgeHandler() {
            @Override
            public void handle(JsonObject data, String callback) {
                if(mMiCANBinder != null)
                {
                    JsCallResult<DeviceInfo> jsCallResult = new JsCallResult<>(callback);
                    jsCallResult.setData(mMiCANBinder.getDeviceInfo());
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
                }
            }
        });

        messageHandlers.put("getCurrentCANMsg", new BridgeHandler() {
            @Override
            public void handle(JsonObject data, String callback) {
            }
        });

//        messageHandlers.put("showLoggingMessage", new BridgeHandler() {
//            @Override
//            public void handle(JsonObject data, String callback) {
//                Log.d(TAG,"showLoggingMessage ");
//                if(mMiCANBinder != null)
//                {
//                    JsCallResult<Result<DataWrapper>> jsCallResult = new JsCallResult<>(callback);
//                    Result<DataWrapper> result = ResponseData.success(mMiCANBinder.getCurrentMsgs());
//                    jsCallResult.setData(result);
//                    final String callbackJs = String.format(CALLBACK_JS_FORMAT, new Gson().toJson(jsCallResult));
//                    Log.d(TAG,"callbackJs "+ callbackJs );
//                    webView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            webView.loadUrl(callbackJs);
//                        }
//                    });
//                }
//            }
//        });

    }



    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
//                Log.d("YULAI","============handleMessage==============");
//                MainActivity2.this.webView.loadUrl("javascript:showAndroidToast('ddd')");

                if(mMiCANBinder !=null)
                {
                    mMiCANBinder.printInfo();
//                    List<CanMessage> messages = mMiCANBinder.getMessages();
//                    Log.d(TAG,"read num " + messages.size());
//                    dataList.clear();
//                    for(CanMessage canMessage:messages)
//                    {
//                        dataList.add(canMessage.toString());
//                    }
//                    adapter.notifyDataSetChanged(); // 通知ListView刷新显示

//                    CanMessage canMessage = mMiCANBinder.getMessage();
//                    if( canMessage != null)
//                    {
//                        Log.d(TAG,"read " + canMessage.toString());
//                        dataList.add(canMessage.toString()); // 添加新项到数据集合中
//                        adapter.notifyDataSetChanged(); // 通知ListView刷新显示
//                    }
                }


//                Toast.makeText(MainActivity2.this, String.format("已录制 %d 报文",gRecvMsgNum.get()), Toast.LENGTH_SHORT).show();
            }
        }
    };

//    private MyAdapter myAdapter;
    private List<Uri> fileList;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri fileUri = data.getClipData().getItemAt(i).getUri();
                            fileList.add(fileUri);
                        }
                    } else if (data.getData() != null) {
                        Uri fileUri = data.getData();
                        fileList.add(fileUri);
                    }

//                    myAdapter.notifyDataSetChanged();

                }
            });

//    @JavascriptInterface
//    public void openFileManager() {
//        Log.e("YULAI","==============openFileManager");
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.setType("*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
////        startActivityForResult(intent, PICK_FILES_REQUEST_CODE);
//        Uri downloadsUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
//
//        intent.putExtra("android.provider.extra.INITIAL_URI", downloadsUri);
//
//        filePickerLauncher.launch(intent);
//    }

    @JavascriptInterface
    public void showToast(String toast) {
        Log.e("YULAI","==============showToast");
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }
    @JavascriptInterface
    public void openFileManager()
    {
        String absolutePath = getWorkHomeDir();
        String path;
        String externalStorageDir = "/storage/emulated/0";
        if (absolutePath.startsWith(externalStorageDir)) {
            path = absolutePath
                    .substring(externalStorageDir.length())
                    .replace("/", "%2f");
        } else {
            path = absolutePath.replace("/", "%2f");
        }
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:"
                + path);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        intent.addCategory(Intent.CATEGORY_OPENABLE); //表示可以打开的文件
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true); // 只显示本地文件
        startActivity(intent);


    }
    private String getWorkHomeDir()
    {
        return Environment.getExternalStorageDirectory()+"/MICAN/";
    }


    private void checkNotify()
    {
        // 检查是否拥有通知权限
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();

// 打开应用设置页面或通知设置页面
        if (!areNotificationsEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 跳转到应用设置页面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                this.startActivity(intent);
            } else {
                // 跳转到通知设置页面
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                this.startActivity(intent);
            }
        }
    }


    private void checkPower()
    {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity");
        intent.setComponent(componentName);
        intent.putExtra("package_name", this.getPackageName());
        intent.putExtra("package_label", this.getResources().getString(R.string.app_name));
        //检测是否有能接受该Intent的Activity存在
        List<ResolveInfo> resolveInfos = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos.size() > 0) {
            this.startActivity(intent);
        }
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


}