package com.example.testcdc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.testcdc.Utils.ToastUtil;
import com.example.testcdc.database.MX11E4Database;
import com.example.testcdc.database.UserDBHelper;
import com.example.testcdc.entity.SignalInfo;
import com.example.testcdc.entity.User;

import java.util.List;

public class MainActivity4 extends AppCompatActivity implements View.OnClickListener {

    private Button button;
    private TextView showContent;
    private String mDatabaseName;
    private SQLiteDatabase db;
    private UserDBHelper userDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        showContent = findViewById(R.id.textView3);
        button = findViewById(R.id.button17);
        button.setOnClickListener(this);
        findViewById(R.id.button18).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.query).setOnClickListener(this);
        mDatabaseName = getFilesDir() +"/user.db";
    }

    @Override
    protected void onStart() {
        super.onStart();

//        insert into signal_info values(null,'LonAccr',6,0x1a9,0,0,23,16,0.0002,-5.0,'Longititude Acceleration\n纵向加速度');
//        MX11E4Database.(this).signalInfoDao().insert(signalInfo);
        userDBHelper = UserDBHelper.getInstance(this);
        userDBHelper.openReadLink();
        userDBHelper.openWriteLink();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userDBHelper.closeLink();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button17)
        {
            // 创建数据库
            db = openOrCreateDatabase(mDatabaseName, Context.MODE_PRIVATE, null);
            String desc = String.format("数据库%s创建%s",db.getPath(),(db !=null)?"成功":"失败");
            showContent.setText(desc);
        } else if (v.getId() == R.id.button18) {
            boolean result = deleteDatabase(mDatabaseName);
            String desc = String.format("数据库删除%s",result?"成功":"失败");
            showContent.setText(desc);
        }else if (v.getId() == R.id.add) {
            long ret = userDBHelper.insert(new User("yulai"));

            String desc = String.format("插入%s",(ret>0)?"成功":"失败");
            ToastUtil.show(this,desc);
//            showContent.setText(desc);
        }else if (v.getId() == R.id.delete) {
            long ret = userDBHelper.delete("yulai");
            String desc = String.format("删除行数 %d",ret);
            ToastUtil.show(this,desc);
        }else if (v.getId() == R.id.update) {
            boolean result = deleteDatabase(mDatabaseName);
            String desc = String.format("数据库%s删除%s",db.getPath(),result?"成功":"失败");
            showContent.setText(desc);
        }else if (v.getId() == R.id.query) {
            List<User> users = userDBHelper.query();
            showContent.setText(users.toString());
        }
    }
}