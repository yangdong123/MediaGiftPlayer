package com.example.yangdong.mediagiftplayerlibrary.gift.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.yangdong.mediagiftplayerlibrary.gift.VideoTextureSurfaceRenderer;
import com.example.yangdong.mediagiftplayerlibrary.gift.utils.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftView extends TextureView implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnPreparedListener {

    private static String TAG = "yd";
    private boolean isPlaying;
    private PlayerThread playerThread;
    private VideoTextureSurfaceRenderer videoRenderer;
    private String videoPath;
    private MediaPlayer mediaPlayer;
    private int surfaceWidth;
    private int surfaceHeight;


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
        surfaceWidth = width;
        surfaceHeight = height;
        SurfaceTexture surfaceTexture = getSurfaceTexture();
        videoRenderer = new VideoTextureSurfaceRenderer(getContext().getApplicationContext(), surfaceTexture, surfaceWidth, surfaceHeight);
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

    public void playAnim(boolean isResource, boolean isMediaPlayer) {
        if (videoRenderer == null) {
            return;
        }
        isPlaying = true;
        Surface surface = new Surface(videoRenderer.getVideoTexture());
        if (isMediaPlayer) {
            playMediaPlayer(surface);
        } else {
            if (isResource) {
                playerThread = new PlayerThread(getContext().getApplicationContext(), surface, videoPath);
            } else {
                playerThread = new PlayerThread(surface, videoPath);
            }
            playerThread.start();
        }
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }


    private void playMediaPlayer(Surface surface) {
        try {
            if (mediaPlayer == null) {
                this.mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        onTextureListener.onCompleted();
                    }
                });
            }
            if (!new File(videoPath).exists()) {
                onTextureListener.onFail();
            }

            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setSurface(surface);
            surface.release();

            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
            onTextureListener.onFail();
        } catch (SecurityException e1) {
            e1.printStackTrace();
            onTextureListener.onFail();
        } catch (IllegalStateException e1) {
            e1.printStackTrace();
            onTextureListener.onFail();
        } catch (IOException e1) {
            e1.printStackTrace();
            onTextureListener.onFail();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            if (mp != null) {
                mp.start();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
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
                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    if (surfaceWidth != 0 && surfaceHeight > 0) {
                        format.setInteger(MediaFormat.KEY_MAX_WIDTH, surfaceWidth);
                        format.setInteger(MediaFormat.KEY_MAX_HEIGHT, surfaceHeight);
                        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, surfaceWidth * surfaceHeight);
                    }
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

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
