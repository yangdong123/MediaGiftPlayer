package com.example.yangdong.mediagiftplayer.playvideo;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangdong.mediagiftplayer.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by MrDong on 2019/1/28.
 */
public class TextureViewMediaActivity extends Activity implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener,  SurfaceHolder.Callback{
    private static final String TAG = "GLViewMediaActivity";

    //https://s17.aconvert.com/convert/p3r68-cdx67/niqqg-461m9.gif
    public static final String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"rocket.mp4";
//    public static final String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"championship.mp4";
    private TextureView textureView;
    private MediaPlayer mediaPlayer;

    private TextureSurfaceRenderer videoRenderer;
    private int surfaceWidth;
    private int surfaceHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mediapalyer);

        textureView = (TextureView) findViewById(R.id.id_textureview);
        TextView tv_background = (TextView) findViewById(R.id.tv_background);
        textureView.setOpaque(false);
        textureView.setSurfaceTextureListener(this);
        tv_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TextureViewMediaActivity.this,"这是背景",Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void playVideo(SurfaceTexture surfaceTexture) {
        videoRenderer = new VideoTextureSurfaceRenderer(this, surfaceTexture, surfaceWidth, surfaceHeight);
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        try {
            this.mediaPlayer = new MediaPlayer();

            while (videoRenderer.getVideoTexture() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Surface surface = new Surface(videoRenderer.getVideoTexture());
            if(!new File(videoPath).exists()){
                showErrorMessage("视频不存在");
            }
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setSurface(surface);

            surface.release();

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setLooping(true);
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            showErrorMessage(e1.getMessage());
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            showErrorMessage(e1.getMessage());
        } catch (IllegalStateException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            showErrorMessage(e1.getMessage());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            showErrorMessage(e1.getMessage());
        }
    }

    public void showErrorMessage(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
        Log.i(TAG,"message === " + message);
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


    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "GLViewMediaActivity::onResume()");
        super.onResume();
    }


    @Override
    protected void onStart()
    {
        Log.v(TAG, "GLViewMediaActivity::onStart()");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "GLViewMediaActivity::onPause()");
        super.onPause();
        if (videoRenderer != null) {
            videoRenderer.onPause();
            videoRenderer = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer =null;
        }
    }

    @Override
    protected void onStop()
    {
        Log.v(TAG, "GLViewMediaActivity::onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "GLViewMediaActivity::onDestroy()");
        super.onDestroy();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.v( TAG, "GLViewMediaActivity::onSurfaceTextureAvailable()"+ " tName:" + Thread.currentThread().getName() + "  tid:");

        surfaceWidth = width;
        surfaceHeight = height;
        playVideo(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    /****************************************************************************************/

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v( TAG, "GLViewMediaActivity::surfaceCreated()" );
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v( TAG, "GLViewMediaActivity::surfaceChanged()" );
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v( TAG, "GLViewMediaActivity::surfaceDestroyed()" );
    }
}
