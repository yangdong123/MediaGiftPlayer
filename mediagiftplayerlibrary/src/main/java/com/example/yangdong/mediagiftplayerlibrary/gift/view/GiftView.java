package com.example.yangdong.mediagiftplayerlibrary.gift.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.yangdong.mediagiftplayerlibrary.gift.VideoTextureSurfaceRenderer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftView extends TextureView implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {

    private static String TAG = "yd";
    private boolean isPlaying;
    private PlayerThread playerThread;
    private VideoTextureSurfaceRenderer videoRenderer;
    private String videoPath;


    public GiftView(Context context) {
        this(context, null);
    }

    public GiftView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    private void init() {
        setSurfaceTextureListener(this);
        setOpaque(false);

    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        SurfaceTexture surfaceTexture = getSurfaceTexture();
        videoRenderer = new VideoTextureSurfaceRenderer(getContext().getApplicationContext(), surfaceTexture, 0, 0);
        videoRenderer.setOnFrameAvailableListener(new VideoTextureSurfaceRenderer.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (onTextureListener != null) {
                    onTextureListener.onTextureAvailable();
                }
            }
        });

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (playerThread != null) {
            playerThread.interrupt();
        }
        isPlaying = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void playAnim(boolean isResource) {
        if (videoRenderer == null) {
            return;
        }
        isPlaying = true;
        Surface surface = new Surface(videoRenderer.getVideoTexture());
        if (isResource) {
            playerThread = new PlayerThread(getContext().getApplicationContext(),surface, videoPath);
        } else {
            playerThread = new PlayerThread(surface, videoPath);
        }
        playerThread.start();
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private class PlayerThread extends Thread {
        private Context context;
        private Surface surface;
        private String filePath;

        public PlayerThread(Surface surface, String filePath) {
            this.surface = surface;
            this.filePath = filePath;
        }

        public PlayerThread(Context context, Surface surface, String filePath) {
            this.context = context;
            this.surface = surface;
            this.filePath = filePath;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            MediaCodec decoder = null;
            MediaExtractor extractor = new MediaExtractor();
            try {
                if (context != null) {
                    extractor.setDataSource(context, Uri.parse(filePath), null);
                } else {
                    extractor.setDataSource(filePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    try {
                        decoder = MediaCodec.createDecoderByType(mime);
                        decoder.configure(format, surface, null, 0);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (decoder == null) {
                Log.e(TAG, "Can't find video info!");
                if (onTextureListener != null) {
                    onTextureListener.onFail();
                }
                return;
            }

            decoder.start();

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean isEOS = false;
            long startMs = System.currentTimeMillis();

            while (!Thread.interrupted()) {
                if (!isEOS) {
                    //1 准备填充器
                    int inIndex = -1;
                    try {
                        inIndex = decoder.dequeueInputBuffer(10000);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        Log.e(TAG, "IllegalStateException dequeueInputBuffer ");
                    }
                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }
                //4 开始解码
                int outIndex = MediaCodec.INFO_TRY_AGAIN_LATER;
                try {
                    outIndex = decoder.dequeueOutputBuffer(info, 10000);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IllegalStateException dequeueOutputBuffer " + e.getMessage());
                }
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = decoder.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "New format " + decoder.getOutputFormat());
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "dequeueOutputBuffer timed out!");
                        break;
                    default:
                        ByteBuffer buffer = outputBuffers[outIndex];
//                        Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);

                        // We use a very simple clock to keep the video FPS, or the video
                        // playback will be too fast
                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        decoder.releaseOutputBuffer(outIndex, true);
                        break;
                }

                // All decoded frames have been rendered, we can stop playing now
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.e(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");

                    break;
                }
            }
            try {
                decoder.stop();
            } catch (Exception e) {
                Log.e(TAG, "IllegalStateException decoder.stop ");
            } finally {
                decoder.release();
                extractor.release();
            }
            isPlaying = false;
            if (videoRenderer != null) {
                playerThread.interrupt();
                videoRenderer.onPause();
                videoRenderer = null;
            }
            if (onTextureListener != null) {
                onTextureListener.onCompleted();
                surface.release();
                setSurfaceTextureListener(null);

            }
        }
    }

    private OnTextureListener onTextureListener;

    public void setOnTextureListener(OnTextureListener onTextureListener) {
        this.onTextureListener = onTextureListener;
    }

    public interface OnTextureListener {
        /**
         * 播放完成
         */
        void onCompleted();

        /**
         * 初始化完成
         */
        void onTextureAvailable();


        /**
         * 出错
         */
        void onFail();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }
}
