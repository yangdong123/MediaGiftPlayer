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
    private boolean isMediaPlayer;
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

    /**
     * 本地路劲
     *
     * @param videoPath
     */
    public void play(String videoPath) {
        giftQueue.addLast(videoPath);
        setPlayParam();
    }

    /**
     * @param videoPath               路径
     * @param isResource              是否资源文件
     * @param giftViewPlayerInterface
     */
    public void play(String videoPath, boolean isResource, GiftViewPlayerInterface giftViewPlayerInterface) {
        this.mGiftViewPlayerInterface = giftViewPlayerInterface;
        this.isResource = isResource;
        giftQueue.addLast(videoPath);
        setPlayParam();
    }

    /**
     * @param videoPath  路径
     * @param isAddFirst 是否加到队头
     */
    public void play(String videoPath, boolean isAddFirst) {
        if (isAddFirst) {
            giftQueue.addFirst(videoPath);
        } else {
            giftQueue.addLast(videoPath);
        }
        setPlayParam();
    }

    /**
     * @param videoPath               路劲
     * @param isAddFirst              是否加到队头
     * @param isResource              是否资源文件
     * @param giftViewPlayerInterface 回调状态
     */
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

    /**
     * @param videoPath               路劲
     * @param isAddFirst              是否加到队头
     * @param isResource              是否资源文件
     * @param giftViewPlayerInterface 回调状态
     */
    public void play(String videoPath, boolean isAddFirst, boolean isResource,boolean isMediaPlayer, GiftViewPlayerInterface giftViewPlayerInterface) {
        this.mGiftViewPlayerInterface = giftViewPlayerInterface;
        this.isResource = isResource;
        this.isMediaPlayer = isMediaPlayer;
        if (isAddFirst) {
            giftQueue.addFirst(videoPath);
        } else {
            giftQueue.addLast(videoPath);
        }
        setPlayParam();
    }

    /**
     * @param videoPath               路劲
     * @param isAddFirst              是否加到队头
     * @param isResource              是否资源文件
     */
    public void play(String videoPath, boolean isAddFirst, boolean isResource,boolean isMediaPlayer) {
        this.isResource = isResource;
        this.isMediaPlayer = isMediaPlayer;
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
        final String finalVideoPath = videoPath;
        giftView.setOnTextureListener(new GiftView.OnTextureListener() {
            @Override
            public void onCompleted() {
                getRootView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mGiftViewPlayerInterface != null) {
                            mGiftViewPlayerInterface.onCompleted(finalVideoPath);
                        }
                        GiftViewPlayer.this.removeView(giftView);
                        setPlayParam();
                    }
                });
            }

            @Override
            public void onTextureAvailable() {
                if (mGiftViewPlayerInterface != null) {
                    mGiftViewPlayerInterface.onTextureAvailable(finalVideoPath);
                }
                giftView.playAnim(isResource,isMediaPlayer);
            }

            @Override
            public void onFail() {
                getRootView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mGiftViewPlayerInterface != null) {
                            mGiftViewPlayerInterface.onFail(finalVideoPath);
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
        void onCompleted(String path);

        void onTextureAvailable(String path);

        void onFail(String path);
    }
}
