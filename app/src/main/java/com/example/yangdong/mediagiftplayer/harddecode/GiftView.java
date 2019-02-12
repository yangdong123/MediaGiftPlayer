package com.example.yangdong.mediagiftplayer.harddecode;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.yangdong.mediagiftplayer.playvideo.VideoTextureSurfaceRenderer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by MrDong on 2019/2/11.
 */
public class GiftView extends TextureView implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {


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
        videoRenderer = new VideoTextureSurfaceRenderer(getContext(), surfaceTexture, 0, 0);
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
        isPlaying = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void playAnim() {

        isPlaying = true;
        Surface surface = new Surface(videoRenderer.getVideoTexture());
        playerThread = new PlayerThread(surface, videoPath);
        playerThread.start();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private class PlayerThread extends Thread {
        private MediaExtractor extractor;
        private MediaCodec decoder;
        private Surface surface;
        private String filePath;

        public PlayerThread(Surface surface, String filePath) {
            this.surface = surface;
            this.filePath = filePath;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(filePath);
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    decoder.configure(format, surface, null, 0);
                    break;
                }
            }

            if (decoder == null) {
                Log.e("DecodeActivity", "Can't find video info!");
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
                    int inIndex = decoder.dequeueInputBuffer(10000);
                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = decoder.dequeueOutputBuffer(info, 10000);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = decoder.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d("DecodeActivity", "New format " + decoder.getOutputFormat());
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                        break;
                    default:
                        ByteBuffer buffer = outputBuffers[outIndex];
//                        Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + buffer);

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
                    Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    isPlaying = false;
                    if (onTextureListener != null) {
                        onTextureListener.onCompleted();
                    }
                    break;
                }
            }

            decoder.stop();
            decoder.release();
            extractor.release();
        }
    }

    private OnTextureListener onTextureListener;

    public void setOnTextureListener(OnTextureListener onTextureListener) {
        this.onTextureListener = onTextureListener;
    }

    interface OnTextureListener {
        void onCompleted();

        void onTextureAvailable();
    }
}
