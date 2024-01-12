package com.example.yangdong.mediagiftplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.example.yangdong.mediagiftplayer.harddecode.HardDecodePlayerActivity;
import com.example.yangdong.mediagiftplayer.playvideo.TextureViewMediaActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int REQUEST_CODE_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    Intent mIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntent = new Intent();
        findViewById(R.id.button_texture_view).setOnClickListener(this);
        findViewById(R.id.button_hard_texture_view).setOnClickListener(this);
    }





    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_texture_view:
                verifyStoragePermission(this);

                mIntent.setClass(this, TextureViewMediaActivity.class);
                startActivity(mIntent);
                Toast.makeText(this, "Play Video on TextureView", Toast.LENGTH_SHORT).show();
                break;


            case R.id.button_hard_texture_view:
//
                verifyStoragePermission(this);

                mIntent.setClass(this, HardDecodePlayerActivity.class);
                startActivity(mIntent);
                Toast.makeText(this, "Play Video on HardDecode", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void verifyStoragePermission(Activity activity) {
        //1.检测权限
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PermissionChecker.PERMISSION_GRANTED) {
            //2.没有权限，弹出对话框申请
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_CODE_STORAGE);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            //权限申请成功
            Toast.makeText(this, "授权SD卡权限成功", Toast.LENGTH_SHORT).show();
        } else {
            //权限申请失败
            Toast.makeText(this, "授权SD卡权限失败，可能会影响使用", Toast.LENGTH_SHORT).show();
        }
    }
}
