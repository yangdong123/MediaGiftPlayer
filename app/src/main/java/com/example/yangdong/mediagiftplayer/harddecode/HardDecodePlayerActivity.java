package com.example.yangdong.mediagiftplayer.harddecode;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.yangdong.mediagiftplayer.R;
import com.example.yangdong.mediagiftplayerlibrary.gift.utils.FileUtil;
import com.example.yangdong.mediagiftplayerlibrary.gift.view.GiftViewPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MediaCodec 硬解码的方式
 * Created by MrDong on 2019/2/11.
 */
public class HardDecodePlayerActivity extends Activity implements View.OnClickListener {

    private GiftViewPlayer giftView;

    //    public static final String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "rocket.mp4";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hard_decode);
        findViewById(R.id.bt_start_add).setOnClickListener(this);
        initView();

    }

    Map<String, Integer> fileMap = new HashMap<>();
    List<String> videoPaths = new ArrayList<>();

    private void initView() {

        giftView = findViewById(R.id.id_textureview);

        fileMap.put("championship.mp4", R.raw.championship);
        fileMap.put("fly.mp4", R.raw.fly);
        fileMap.put("money.mp4", R.raw.money);
        fileMap.put("rocket.mp4", R.raw.rocket);
        fileMap.put("shipe.mp4", R.raw.ship);
        for (Map.Entry<String, Integer> entry : fileMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            String videoPath = FileUtil.copyFile(getApplicationContext(), key, value);
            videoPaths.add(videoPath);
        }

    }


    int index = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start_add:
                if (index > videoPaths.size()-1) {
                    index = 0;
                }
                giftView.play(videoPaths.get(index), false, new GiftViewPlayer.GiftViewPlayerInterface() {
                    @Override
                    public void onCompleted(String path) {
                        Log.e("yd","onCompleted  " +path );
                    }

                    @Override
                    public void onTextureAvailable(String path) {
                        Log.e("yd","onTextureAvailable  " +path );
                    }

                    @Override
                    public void onFail(String path) {
                        Log.e("yd","onFail  " +path );
                    }
                });
                index++;
                break;
        }
    }
}
