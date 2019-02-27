package com.example.yangdong.mediagiftplayerlibrary.gift.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.LinkedList;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftViewPlayer extends FrameLayout {
    private int childViewCount;

    private LinkedList<String> giftQueue = new LinkedList<>();

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
        giftQueue.addLast(videoPath);
        setPlayParam();
    }

    public void play(String videoPath, boolean isAddFirst) {
        if (isAddFirst) {
            giftQueue.addFirst(videoPath);
        } else {
            giftQueue.addLast(videoPath);
        }
        setPlayParam();
    }

    private void setPlayParam() {
        if (getChildCount() >= childViewCount) {
            return;
        }

        final GiftView giftView = new GiftView(getContext());
        String videoPath = null;
        if (!giftQueue.isEmpty()) {
            videoPath = giftQueue.removeFirst();
        }
        if (videoPath == null || videoPath.length() == 0) {
            return;
        }
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

            @Override
            public void onFail() {
                getRootView().post(new Runnable() {
                    @Override
                    public void run() {
                        GiftViewPlayer.this.removeView(giftView);
                        setPlayParam();
                    }
                });
            }
        });

        addView(giftView);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllViews();
    }
}
