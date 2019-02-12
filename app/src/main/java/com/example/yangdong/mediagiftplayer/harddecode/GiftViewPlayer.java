package com.example.yangdong.mediagiftplayer.harddecode;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftViewPlayer extends FrameLayout {
    private int childViewCount;

    private Queue<String> giftQueue = new LinkedBlockingDeque<>();

    public GiftViewPlayer(Context context) {
        this(context, null);
    }

    public GiftViewPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftViewPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        setMaxChildView(1);//设置显示的view个数,默认是1个
    }

    public void setMaxChildView(int childViewCount) {
        this.childViewCount = childViewCount;
    }


    public void play(String videoPath) {
        giftQueue.add(videoPath);
        setPlayParam();
    }

    private void setPlayParam() {
        if (getChildCount() >= childViewCount) {
            return;
        }
        String videoPath = giftQueue.poll();
        if (videoPath == null || videoPath.length() == 0) {
            return;
        }

        final GiftView giftView = new GiftView(getContext());
        giftView.setVideoPath(videoPath);
        giftView.setOnTextureListener(new GiftView.OnTextureListener() {
            @Override
            public void onCompleted() {
                getRootView().post(new Runnable() {
                    @Override
                    public void run() {
                        GiftViewPlayer.this.removeView(giftView);
                        setPlayParam();
                    }
                });
            }

            @Override
            public void onTextureAvailable() {

                giftView.playAnim();
            }
        });

        addView(giftView);
    }
}
