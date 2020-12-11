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
    private boolean isResource;
    private GiftViewPlayerInterface mGiftViewPlayerInterface;

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

    public void play(String videoPath, boolean isResource, GiftViewPlayerInterface giftViewPlayerInterface) {
        this.mGiftViewPlayerInterface = giftViewPlayerInterface;
        this.isResource = isResource;
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

    public void play(String videoPath, boolean isAddFirst, boolean isResource, GiftViewPlayerInterface giftViewPlayerInterface) {
        this.mGiftViewPlayerInterface = giftViewPlayerInterface;
        this.isResource = isResource;
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
                        if (mGiftViewPlayerInterface != null) {
                            mGiftViewPlayerInterface.onCompleted();
                        }
                        GiftViewPlayer.this.removeView(giftView);
                        setPlayParam();
                    }
                });
            }

            @Override
            public void onTextureAvailable() {
                if (mGiftViewPlayerInterface != null) {
                    mGiftViewPlayerInterface.onTextureAvailable();
                }
                giftView.playAnim(isResource);
            }

            @Override
            public void onFail() {
                getRootView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mGiftViewPlayerInterface != null) {
                            mGiftViewPlayerInterface.onFail();
                        }
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

    public void cleanData() {
        giftQueue.clear();
    }

    public interface GiftViewPlayerInterface {
        void onCompleted();

        void onTextureAvailable();

        void onFail();
    }
}
