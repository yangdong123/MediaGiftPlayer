package com.example.yangdong.mediagiftplayer.harddecode;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.example.yangdong.mediagiftplayer.R;

/**
 * Created by MrDong on 2019/2/11.
 */
public class HardDecodePlayerActivity extends Activity implements View.OnClickListener {

    private GiftView giftView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hard_decode);
        findViewById(R.id.tv_background).setOnClickListener(this);
        initView();

    }

    private void initView() {

        giftView = findViewById(R.id.id_textureview);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_background:
                giftView.playAnim();
                break;
        }
    }
}
