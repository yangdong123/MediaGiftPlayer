package com.example.yangdong.mediagiftplayerlibrary.gift.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.example.yangdong.mediagiftplayerlibrary.gift.bean.GiftBean;

import java.util.LinkedList;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftViewPlayer extends FrameLayout {
    private int childViewCount;
    private GiftViewPlayerInterface mGiftViewPlayerInterface;

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
    }


    /**
     * @param videoPath               路劲
     * @param type                    返回不同type
     * @param isAddFirst              是否加到队头
     * @param isResource              是否资源文件
     */
    public void play(String videoPath, int type ,boolean isAddFirst, boolean isResource,boolean isMediaPlayer) {
        GiftBean giftBean = new GiftBean(videoPath, type, isResource, isMediaPlayer);
        if (isAddFirst) {
            giftQueue.addFirst(giftBean);
        } else {
            giftQueue.addLast(giftBean);
        }
        setPlayParam();
    }

    /**
     * @param videoPath               路劲
     * @param type                    返回不同type
     * @param isAddFirst              是否加到队头
     * @param isResource              是否资源文件
     */
    public void play(String videoPath, int type ,boolean isAddFirst, boolean isResource,boolean isMediaPlayer, GiftViewPlayerInterface giftViewPlayerInterface) {
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

        final GiftView giftView = new GiftView(getContext());

        if (!giftQueue.isEmpty()) {
            giftBean = giftQueue.removeFirst();
        }
        if (giftBean == null || giftBean.path.length() == 0) {
            return;
        }
        giftView.setVideoPath(giftBean.path);
        final GiftBean finalGiftBean = giftBean;
        giftView.setOnTextureListener(new GiftView.OnTextureListener() {
            @Override
            public void onCompleted() {
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
            }

            @Override
            public void onTextureAvailable() {
                if (mGiftViewPlayerInterface != null) {
                    mGiftViewPlayerInterface.onTextureAvailable(finalGiftBean);
                }
                giftView.playAnim(finalGiftBean.isResource, finalGiftBean.isMediaPlayer);
            }

            @Override
            public void onFail() {
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
