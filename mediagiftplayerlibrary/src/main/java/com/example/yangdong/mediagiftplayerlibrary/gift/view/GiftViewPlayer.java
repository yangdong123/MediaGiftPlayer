package com.example.yangdong.mediagiftplayerlibrary.gift.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.example.yangdong.mediagiftplayerlibrary.gift.bean.GiftBean;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftViewPlayer extends FrameLayout {
    private int childViewCount;
    private GiftViewPlayerInterface mGiftViewPlayerInterface;
    private ExecutorService singleThreadPool;
    private LinkedList<GiftBean> giftQueue = new LinkedList<>();

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
        if (childViewCount == 1) {
            singleThreadPool = Executors.newSingleThreadExecutor();
        } else {
            singleThreadPool = Executors.newCachedThreadPool();
        }
    }


    /**
     * @param videoPath  路劲
     * @param type       返回不同type
     * @param isAddFirst 是否加到队头
     * @param isResource 是否资源文件
     */
    public void play(String videoPath, int type, boolean isAddFirst, boolean isResource, boolean isMediaPlayer) {
        GiftBean giftBean = new GiftBean(videoPath, type, isResource, isMediaPlayer);
        if (isAddFirst) {
            giftQueue.addFirst(giftBean);
        } else {
            giftQueue.addLast(giftBean);
        }
        setPlayParam();
    }

    /**
     * @param videoPath  路劲
     * @param type       返回不同type
     * @param isAddFirst 是否加到队头
     * @param isResource 是否资源文件
     */
    public void play(String videoPath, int type, boolean isAddFirst, boolean isResource, boolean isMediaPlayer, GiftViewPlayerInterface giftViewPlayerInterface) {
        this.mGiftViewPlayerInterface = giftViewPlayerInterface;
        GiftBean giftBean = new GiftBean(videoPath, type, isResource, isMediaPlayer);
        if (isAddFirst) {
            giftQueue.addFirst(giftBean);
        } else {
            giftQueue.addLast(giftBean);
        }
        setPlayParam();
    }

    private void setPlayParam() {
        GiftBean giftBean = null;
        if (getChildCount() >= childViewCount) {
            return;
        }


        if (!giftQueue.isEmpty()) {
            giftBean = giftQueue.removeFirst();
        }
        if (giftBean == null || giftBean.path == null || giftBean.path.length() == 0) {
            removeAllViews();
            return;
        }

        final WeakReference<GiftView> giftViewWeakReference = new WeakReference<>(new GiftView(getContext()));
        final GiftView giftView = giftViewWeakReference.get();
        giftView.setVideoPath(giftBean.path);
        final GiftBean finalGiftBean = giftBean;
        giftView.setOnTextureListener(new GiftView.OnTextureListener() {

            @Override
            public void onCompleted() {
                try {

                    getRootView().post(new Runnable() {
                        @Override
                        public void run() {
                            if (mGiftViewPlayerInterface != null) {
                                mGiftViewPlayerInterface.onCompleted(finalGiftBean);
                            }
                            GiftViewPlayer.this.removeView(giftView);
                            setPlayParam();
                        }
                    });
                } catch (Exception e) {
                    Log.e("yd", "onCompleted" + e.getMessage());
                }

            }

            @Override
            public void onTextureAvailable() {
                try {
                    if (mGiftViewPlayerInterface != null) {
                        mGiftViewPlayerInterface.onTextureAvailable(finalGiftBean);
                    }
                    giftView.playAnim(singleThreadPool, finalGiftBean.isResource, finalGiftBean.isMediaPlayer);
                } catch (Exception e) {
                    Log.e("yd", "onTextureAvailable" + e.getMessage());
                }
            }

            @Override
            public void onFail() {
                try {
                    getRootView().post(new Runnable() {
                        @Override
                        public void run() {
                            if (mGiftViewPlayerInterface != null) {
                                mGiftViewPlayerInterface.onFail(finalGiftBean);
                            }
                            GiftViewPlayer.this.removeView(giftView);
                            setPlayParam();
                        }
                    });
                } catch (Exception e) {
                    Log.e("yd", "onFail" + e.getMessage());
                }

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
        void onCompleted(GiftBean giftBean);

        void onTextureAvailable(GiftBean giftBean);

        void onFail(GiftBean giftBean);
    }
}
