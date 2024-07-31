package com.example.testcdc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class WebAppInterface {



    Context mContext;

    // 实例化接口时需要传入Context
    WebAppInterface(Context c) {
        mContext = c;
    }

    // 显示Toast的方法
    @JavascriptInterface
    public void showToast(String toast) {
        Log.e("YULAI","==============showToast");
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }





}
