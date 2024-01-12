package com.example.yangdong.mediagiftplayer.playvideo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangdong.mediagiftplayer.R;
import com.example.yangdong.mediagiftplayerlibrary.gift.bean.GiftBean;
import com.example.yangdong.mediagiftplayerlibrary.gift.utils.FileUtil;
import com.example.yangdong.mediagiftplayerlibrary.gift.view.GiftViewPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MediaPlayer 的方式
 * Created by MrDong on 2019/1/28.
 */
public class TextureViewMediaActivity extends Activity {
    private static final String TAG = "yd";

    //https://s17.aconvert.com/convert/p3r68-cdx67/niqqg-461m9.gif
    public static String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ship.mp4";
    //    public static final String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"championship.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mediapalyer);

        TextView tv_background = (TextView) findViewById(R.id.tv_background);
        Button bt_start_add = (Button) findViewById(R.id.bt_start_add);
        GiftViewPlayer gift_player = (GiftViewPlayer) findViewById(R.id.gift_player);
        tv_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TextureViewMediaActivity.this, "这是背景", Toast.LENGTH_SHORT).show();
            }
        });

        initView();

        bt_start_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * @param videoPath  路劲
                 * @param type       返回不同type
                 * @param isAddFirst 是否加到队头
                 * @param isResource 是否资源文件
                 */
                gift_player.play(videoPath, index, true, true, false, new GiftViewPlayer.GiftViewPlayerInterface() {
                    @Override
                    public void onCompleted(GiftBean giftBean) {
                        if (index == videoPaths.size() - 1) {
                            index = 0;
                        }
                        videoPath = videoPaths.get(index);
                        index++;
                        gift_player.play(videoPath, index, true, true, false);
                    }

                    @Override
                    public void onTextureAvailable(GiftBean giftBean) {

                    }

                    @Override
                    public void onFail(GiftBean giftBean) {

                    }
                });
            }
        });
    }

    Map<String, Integer> fileMap = new HashMap<>();
    List<String> videoPaths = new ArrayList<>();
    int index = 0;

    private void initView() {


        fileMap.put("championship.mp4", R.raw.championship);
        fileMap.put("fly.mp4", R.raw.fly);
        fileMap.put("money.mp4", R.raw.money);
        fileMap.put("rocket.mp4", R.raw.rocket);
        fileMap.put("ship.mp4", R.raw.ship);
        for (Map.Entry<String, Integer> entry : fileMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            String videoPath = FileUtil.copyFile(getApplicationContext(), key, value);
            videoPaths.add(videoPath);
        }

        videoPath = videoPaths.get(videoPaths.size() - 1);
    }


    public void showErrorMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "message === " + message);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "GLViewMediaActivity::onResume()");
        super.onResume();
    }


    @Override
    protected void onStart() {
        Log.v(TAG, "GLViewMediaActivity::onStart()");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "GLViewMediaActivity::onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "GLViewMediaActivity::onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "GLViewMediaActivity::onDestroy()");
        super.onDestroy();
    }


}
