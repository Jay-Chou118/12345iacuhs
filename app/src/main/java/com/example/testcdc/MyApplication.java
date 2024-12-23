package com.example.testcdc;

import static com.example.testcdc.Utils.DataBaseUtil.copyDataBase;

import android.app.Application;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.example.testcdc.Utils.DataBaseUtil;
import com.example.testcdc.database.Basic_DataBase;
import com.example.testcdc.entity.SignalInfo;
import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.auth.AuthApi;
import com.xiaomi.xms.wearable.auth.Permission;
import com.xiaomi.xms.wearable.message.MessageApi;
import com.xiaomi.xms.wearable.message.OnMessageReceivedListener;
import com.xiaomi.xms.wearable.node.DataItem;
import com.xiaomi.xms.wearable.node.DataQueryResult;
import com.xiaomi.xms.wearable.node.Node;
import com.xiaomi.xms.wearable.node.NodeApi;
import com.xiaomi.xms.wearable.notify.NotifyApi;
import com.xiaomi.xms.wearable.service.OnServiceConnectionListener;
import com.xiaomi.xms.wearable.service.ServiceApi;
import com.xiaomi.xms.wearable.tasks.OnFailureListener;
import com.xiaomi.xms.wearable.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG = "MICAN_application";

    private Basic_DataBase Database = null;

    private static MyApplication sInstance;

    private TextToSpeech textToSpeech;

    private NodeApi wearApi;

    private AuthApi authApi;

    MessageApi messageApi;

    ServiceApi serviceApi;

    NotifyApi notifyApi;
    private String wearId;


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Log.d(TAG, "i am on create");
        Database = Room.databaseBuilder(this, Basic_DataBase.class, "basic_database")
                .addMigrations()
                .build();

//        Database.clearAllTables();
//        Log.d(TAG, "PPPPPPPPPP: ");
        initDatabase_Basic();


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int langResult = textToSpeech.setLanguage(Locale.US);
                    Log.i(TAG, "successful");
                } else {
                    Log.e(TAG, "no!!!!!!");
                }
            }
        });

        wearApi = Wearable.getNodeApi(this);
        authApi = Wearable.getAuthApi(this);
        messageApi = Wearable.getMessageApi(this);
        serviceApi = Wearable.getServiceApi(this);
        notifyApi = Wearable.getNotifyApi(this);
        test();
//        try (ZContext context = new ZContext()) {
//            // Socket to talk to clients
//            ZMQ.Socket socket = context.createSocket(SocketType.REP);
//            socket.bind("tcp://*:5555");
//
//            while (!Thread.currentThread().isInterrupted()) {
//                // Block until a message is received
//                byte[] reply = socket.recv(0);
//
//                // Print the message
//                System.out.println(
//                        "Received: [" + new String(reply, ZMQ.CHARSET) + "]"
//                );
//
//                // Send a response
//                String response = "Hello, world!";
//                socket.send(response.getBytes(ZMQ.CHARSET), 0);
//            }
//        }
//

    }

    public Basic_DataBase getDatabase()
    {

        return Database;
    }


    public void say(String content)
    {
        textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    public static MyApplication getInstance()
    {
        return sInstance;
    }


    public void initDatabase_Basic()
    {
        boolean ret = DataBaseUtil.checkDataBase(this,"basic_database");
        Log.e(TAG, "FFFFFF " + ret );
        if(ret)
        {
            Log.i(TAG,"数据库存在");
//            Database = Room.databaseBuilder(this, Basic_DataBase.class, "basic_database")
////                .createFromAsset("databases/basic_database.db") // 从assets加载数据库文件
//                .addMigrations()
//                .build();
//            Database.clearAllTables();
//            List<SignalInfo> all = MyApplication.getInstance().getDatabase().signalInfoDao().getAll();
//            Log.i(TAG,"num is " + all.size());
//            List<SignalInfo> data = MyApplication.getInstance().getDatabase().signalInfoDao().getSignal(6,0x1a9);
//            for(SignalInfo element : data) {
////                Log.i(TAG, element.toString());
//            }

            boolean open = MyApplication.getInstance().getDatabase().isOpen();
            if(open)
            {
                Log.i(TAG,"database is open");
            }else {

                Log.i(TAG,"database is not open");
            }
        }else{
            Log.d(TAG,"数据库不存在");
//            DataBaseUtil.copyDataBase(this,"basic_database");
            //修改
//            Basic_DataBase db = Basic_DataBase.getDatabase(getApplicationContext());
//            Log.e(TAG,"111111111111111" );
            new Thread(()->{
//                DataBaseUtil.init_carType();
//                //Log.e(TAG, "222222222222222222222222222  数据库不存在  remake " );
//                DataBaseUtil.initDataFromCsv(this);
//                DataBaseUtil.initMsgFromCsv(this);
                copyDataBase(this, "basic_database");
            }).start();


        }
//        Database = Room.databaseBuilder(this, Basic_DataBase.class, "basic_database")
//                .addMigrations()
//                .build();
//        Database.clearAllTables();
    }


    void test()
    {
        //调⽤getConnectedNodes⽅法获取已连接设备
        wearApi.getConnectedNodes().addOnSuccessListener(new OnSuccessListener<List<Node>>()
        {
            @Override
            public void onSuccess(List<Node> nodes) {
                Log.i(TAG,nodes.toString());
                if(!nodes.isEmpty())
                {
                    say("识别到" + nodes.get(0).name);
                    wearId = nodes.get(0).id;
                    test3();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,e.toString());

            }
        });
    }

    void test2()
    {
        authApi.checkPermission(wearId, Permission.DEVICE_MANAGER)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.i(TAG,"permission " + result);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e ) {
                        Log.e(TAG,e.toString());
                    }
                });
    }

    void test3()
    {
        authApi.requestPermission(wearId,Permission.DEVICE_MANAGER,Permission.NOTIFY)
        .addOnSuccessListener(new OnSuccessListener<Permission[]>() {
            @Override
            public void onSuccess(Permission[] permissions) {
                Log.i(TAG,"申请权限成功");
                test8();
                test6();
//                test7();
                test9();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"申请权限成功");
            }
        });
    }


    void test6()
    {
        wearApi.query(wearId, DataItem.ITEM_CONNECTION)
                .addOnSuccessListener(new OnSuccessListener<DataQueryResult>() {
                    @Override
                    public void onSuccess(DataQueryResult result) {
                        boolean connectionStatus = result.isConnected;//DataQueryResult定义了各种状态的状态值，和DataItem⼀⼀对应
                        Log.i(TAG,"当前设备处于 " + connectionStatus);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.toString());
                    }
                });
    }

    void test5()
    {
        wearApi.launchWearApp(wearId,"pages/index").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void var1) {
                Log.i(TAG,"打开app成功");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception var1) {
            }
        });;
    }

    public void test7()
    {
        byte[] messageBytes = new byte[1024];
        //调⽤sendMessage⽅法⽤来发送数据给穿戴设备端应⽤
        messageApi.sendMessage(wearId,messageBytes)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG,"发送数据成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//发送数据失败
                    }
                });
    }


    void test8()
    {
        OnServiceConnectionListener onServiceConnectionListener = new
                OnServiceConnectionListener() {
                    @Override
                    public void onServiceConnected() {
                        Log.i(TAG,"手机与手表连接建立成功");
                    }
                    @Override
                    public void onServiceDisconnected() {
                        Log.i(TAG,"手机与手表断开连接");
                    }
                };
        serviceApi.registerServiceConnectionListener(onServiceConnectionListener);
    }

    void test9()
    {
        OnMessageReceivedListener onMessageReceivedListener = new
                OnMessageReceivedListener() {
                    @Override
                    public void onMessageReceived(@NotNull String nodeId, @NotNull byte[]
                            message) {
                        Log.i(TAG,"收到消息 " + nodeId + " 内容 " + new String(message));
                    }
                };
//监听穿戴设备端应⽤发来的消息
        messageApi.addListener(wearId, onMessageReceivedListener)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void var1) {
                        Log.i(TAG,"添加消息成功");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception var1) {
                        Log.e(TAG,"添加消息失败");
                    }
                });
    }


}
