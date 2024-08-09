package com.example.testcdc.Utils;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.example.testcdc.MyApplication;

public class ToastUtil {

    private static final String TAG = "MICAN_ToastUtil";

    private static TextToSpeech textToSpeech;

    public static void show(Context ctx, String desc)
    {
        Toast.makeText(ctx,desc,Toast.LENGTH_SHORT).show();
    }

    public static void vibrate(Context ctx)
    {
        Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        Log.i(TAG,"vibrator " + vibrator);
        String mString = vibrator.hasVibrator() ? "当前设备有振动器" : "当前设备无振动器";
        Log.i(TAG,"vibrator " + mString);
        vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE));
    }

}
