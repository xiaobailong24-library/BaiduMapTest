package com.xiaobailong24.baidumaptest;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.baidu.mapapi.SDKInitializer;
import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import java.io.File;

/**
 * Created by xiaobailong24 on 2017/3/1.
 * 主程序配置
 */
public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getName();

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mContext == null) {
            mContext = getApplicationContext();
        }
        initXlog();
        //百度API初始化
        SDKInitializer.initialize(this);
        Log.d(TAG, "onCreate: 主程序初始化完成");
    }

    //xlog初始化
    private void initXlog() {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("marsxlog");
        final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String logPath = SDCARD + "/CloudBill/log";
        File logFile = new File(logPath);
        if (!logFile.exists()) {
            boolean is = logFile.mkdirs();
        }
        //init xlog
        if (BuildConfig.DEBUG) {
            Xlog.appenderOpen(Xlog.LEVEL_DEBUG, Xlog.AppednerModeAsync, "", logPath, "CloudBill");
            Xlog.setConsoleLogOpen(true);
        } else {
            Xlog.appenderOpen(Xlog.LEVEL_INFO, Xlog.AppednerModeAsync, "", logPath, "CloudBill");
            Xlog.setConsoleLogOpen(false);
        }

        Log.setLogImp(new Xlog());
        Log.appenderFlush(true);
        Log.d(TAG, "xlog初始化成功！");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate: 主程序配置");
        Log.appenderClose();
    }

    public static Context getContext() {
        return mContext;
    }

}
