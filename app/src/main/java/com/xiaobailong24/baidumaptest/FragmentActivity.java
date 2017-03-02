package com.xiaobailong24.baidumaptest;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.mars.xlog.Log;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;


public class FragmentActivity extends AppCompatActivity {
    private static final String TAG = FragmentActivity.class.getName();

    //检查权限
    private static final int REQUEST_CODE_LOCATION = 100;
    private static final int REQUEST_CODE_PHONE = 200;
    private static final int REQUEST_CODE_STORAGE = 300;

    private int location = 0;
    private int phone = 0;
    private int storage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        if (Build.VERSION.SDK_INT >= 23) {
            obtainPermissions();
        } else {
            initView();
        }

    }

    private void initView(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.layout, new MapFragment());
        transaction.commit();
    }

    private void obtainPermissions() {
        MPermissions.requestPermissions(FragmentActivity.this, REQUEST_CODE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @PermissionGrant(REQUEST_CODE_LOCATION)
    public void requestLocationSuccess() {
        Log.d(TAG, "requestLocationSuccess: ");
        location = 1;
        MPermissions.requestPermissions(FragmentActivity.this, REQUEST_CODE_PHONE,
                Manifest.permission.READ_PHONE_STATE);
    }

    @PermissionDenied(REQUEST_CODE_LOCATION)
    public void requestLocationFailed() {
        Toast.makeText(FragmentActivity.this, "DENY ACCESS Location!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "requestLocationFailed: ");
        location = 0;
        MPermissions.requestPermissions(FragmentActivity.this, REQUEST_CODE_PHONE,
                Manifest.permission.READ_PHONE_STATE);
    }

    @PermissionGrant(REQUEST_CODE_PHONE)
    public void requestPhoneSuccess() {
        Log.d(TAG, "requestPhoneSuccess: ");
        phone = 1;
        MPermissions.requestPermissions(FragmentActivity.this, REQUEST_CODE_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @PermissionDenied(REQUEST_CODE_PHONE)
    public void requestPhoneFailed() {
        Toast.makeText(FragmentActivity.this, "DENY ACCESS PHONE!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "requestPhoneFailed: ");
        phone = 0;
        MPermissions.requestPermissions(FragmentActivity.this, REQUEST_CODE_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @PermissionGrant(REQUEST_CODE_STORAGE)
    public void requestStorageSuccess() {
        Log.d(TAG, "requestStorageSuccess: ");
        storage = 1;
        Log.d(TAG, "onViewCreated: permissions->" + location + phone + storage);
        if (location + phone + storage == 3) {
            new Handler().post(new Runnable() {
                public void run() {
                    initView();
                }
            });
        } else {
            Toast.makeText(FragmentActivity.this, "权限不够！", Toast.LENGTH_SHORT).show();
            FragmentActivity.this.finish();
        }
    }

    @PermissionDenied(REQUEST_CODE_STORAGE)
    public void requestStorageFailed() {
        Toast.makeText(this, "DENY ACCESS STORAGE!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "requestStorageFailed: ");
        storage = 0;
        Toast.makeText(this, "权限不够！", Toast.LENGTH_SHORT).show();
        this.finish();
    }
}
