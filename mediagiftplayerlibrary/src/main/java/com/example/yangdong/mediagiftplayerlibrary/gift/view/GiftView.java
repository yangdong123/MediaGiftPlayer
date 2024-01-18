package com.example.yangdong.mediagiftplayerlibrary.gift.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;


import com.example.yangdong.mediagiftplayerlibrary.gift.VideoTextureSurfaceRenderer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftView extends TextureView implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnPreparedListener {

    private static String TAG = "yd";
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
            Thread.interrupted();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void playAnim(ExecutorService singleThreadPool, boolean isResource, boolean isMediaPlayer) {
        if (videoRenderer == null || videoRenderer.getVideoTexture() == null) {
            return;
        }
        WeakReference<Surface> surfaceWeakReference = new WeakReference<>(new Surface(videoRenderer.getVideoTexture()));
        Surface surface = surfaceWeakReference.get();
        if (isMediaPlayer) {
            playMediaPlayer(surface);
        } else {
            if (isResource) {
                playerThread = new PlayerThread(singleThreadPool, getContext().getApplicationContext(), surface, videoPath);
            } else {
                playerThread = new PlayerThread(singleThreadPool, surface, videoPath);
            }
        }
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }


    private void playMediaPlayer(final Surface surface) {
        try {
            if (mediaPlayer == null) {
                this.mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        surface.release();
                        if (videoRenderer != null) {
                            Thread.interrupted();
                            videoRenderer.onPause();
                            videoRenderer = null;
                        }
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

    private class PlayerThread {
        private Context context;
        private Surface surface;
        private String filePath;

        public PlayerThread(ExecutorService singleThreadPool, Surface surface, String filePath) {
            this.surface = surface;
            this.filePath = filePath;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                run(singleThreadPool);
            }
        }

        public PlayerThread(ExecutorService singleThreadPool, Context context, Surface surface, String filePath) {
            this.context = context;
            this.surface = surface;
            this.filePath = filePath;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                run(singleThreadPool);
            }
        }


        public void run(ExecutorService singleThreadPool) {
            singleThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "线程：" + Thread.currentThread().getName());
                    MediaCodec decoder = null;
                    MediaFormat outputFormat = null;
                    MediaExtractor extractor = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        extractor = new MediaExtractor();

                        try {
                            if (context != null) {
                                extractor.setDataSource(context, Uri.parse(filePath), null);
                            } else {
                                extractor.setDataSource(filePath);
                            }
                            for (int i = 0; i < extractor.getTrackCount(); i++) {
                                MediaFormat format = extractor.getTrackFormat(i);
                                if (surfaceWidth > 0 && surfaceHeight > 0) {
                                    format.setInteger(MediaFormat.KEY_WIDTH, surfaceWidth);
                                    format.setInteger(MediaFormat.KEY_HEIGHT, surfaceHeight);
                                    format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, surfaceWidth * surfaceHeight);
                                    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                                }
                                String mime = format.getString(MediaFormat.KEY_MIME);
                                if (mime.startsWith("video/")) {
                                    extractor.selectTrack(i);
                                    decoder = MediaCodec.createDecoderByType(mime);
                                    decoder.configure(format, surface, null, 0);
                                    outputFormat = decoder.getOutputFormat();
                                    break;
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

                            boolean isEOS = false;
                            long startMs = System.currentTimeMillis();
                            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                            while (!Thread.interrupted()) {
                                if (!isEOS) {
                                    //1 准备填充器
                                    int inIndex = -1;
                                    inIndex = decoder.dequeueInputBuffer(10000);
                                    if (inIndex >= 0 && decoder != null) {
                                        ByteBuffer buffer = null;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                            buffer = decoder.getInputBuffer(inIndex);
                                        }
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
                                int outIndex = decoder.dequeueOutputBuffer(info, 10000);
                                if (outIndex >= 0) {
//                        ByteBuffer outputBuffers = decoder.getOutputBuffer(outIndex);
//                        MediaFormat bufferFormat = decoder.getOutputFormat(outIndex); // option A
                                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                                        Thread.sleep(10);
                                    }
                                    decoder.releaseOutputBuffer(outIndex, true);
                                } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                    outputFormat = decoder.getOutputFormat(); // option B
                                }
                                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                    Log.e(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                                    break;
                                }
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "GiftView decoder " + e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.e(TAG, "GiftView decoder " + e.getMessage());
                            e.printStackTrace();
                        }
                        try {
                            decoder.stop();
                        } catch (Exception e) {
                            Log.e(TAG, "IllegalStateException decoder.stop ");
                        } finally {
                            if (decoder != null) {
                                decoder.release();
                            }
                            if (extractor != null) {
                                extractor.release();
                            }

                            if (videoRenderer != null) {
                                Thread.interrupted();
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

                }
            });

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
        if (playerThread != null) {
            playerThread = null;
        }
        if (videoRenderer != null) {
            videoRenderer = null;
        }
    }
}
