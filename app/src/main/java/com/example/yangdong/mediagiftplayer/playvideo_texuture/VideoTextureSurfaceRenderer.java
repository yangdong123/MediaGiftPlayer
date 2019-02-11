package com.example.yangdong.mediagiftplayer.playvideo_texuture;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.yangdong.mediagiftplayer.R;
import com.example.yangdong.mediagiftplayer.playvideo_texuture.utils.RawResourceReader;
import com.example.yangdong.mediagiftplayer.playvideo_texuture.utils.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by MrDong on 2019/1/28.
 */

public class VideoTextureSurfaceRenderer extends TextureSurfaceRenderer implements
        SurfaceTexture.OnFrameAvailableListener {

    public static final String TAG = "yd";

    private static float squareSize = 1.0f;
    private static float squareCoords[] = {
            -squareSize, squareSize,   // top left
            -squareSize, -squareSize,   // bottom left
            squareSize, -squareSize,    // bottom right
            squareSize, squareSize}; // top right
    private static short drawOrder[] = {0, 1, 2, 0, 2, 3};
    private Context context;
    //纹理坐标
    float[] textureBufferP = new float[]{0f, 1f, 0f, 1f, 0f, 0f, 0f, 1f, 0.5f, 0f, 0f, 1f, 0.5f, 1f, 0f, 1f};
    float[] textureBufferQ = new float[]{0.5f, 1f, 0f, 1f, 0.5f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 1f};
    float[] textureBufferS = new float[]{0f, 1f, 0f, 1f, 0f, 0.5f, 0f, 1f, 1f, 0.5f, 0f, 1f, 1f, 1f, 0f, 1f};
    float[] textureBufferT = new float[]{0f, 0.5f, 0f, 1f, 0f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0.5f, 0f, 1f};
    private int[] textures = new int[4];
    private int shaderProgram;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    private SurfaceTexture videoTexture;
    private float[] videoTextureTransform;
    private boolean frameAvailable = false;

    private int isYUV;
    private int texture;
    private int s_texture_2D_y;
    private int s_texture_2D_u;
    private int s_texture_2D_v;
    private int vTexCoordinateAlpha;
    private int vTexCoordinateRgb;
    private int vPosition;


    public VideoTextureSurfaceRenderer(Context context, SurfaceTexture texture, int width, int height) {
        super(texture, width, height);
        this.context = context;
        videoTextureTransform = new float[16];
    }

    private void setupGraphics() {
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(context, R.raw.vetext_sharder_anim);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(context, R.raw.fragment_sharder_anim);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        shaderProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"texture", "vPosition", "vTexCoordinate", "textureTransform"});

        isYUV = GLES20.glGetUniformLocation(shaderProgram, "isYUV");
        texture = GLES20.glGetUniformLocation(shaderProgram, "texture");
        s_texture_2D_y = GLES20.glGetUniformLocation(shaderProgram, "s_texture_2D_Y");
        s_texture_2D_u = GLES20.glGetUniformLocation(shaderProgram, "s_texture_2D_U");
        s_texture_2D_v = GLES20.glGetUniformLocation(shaderProgram, "s_texture_2D_V");
        vTexCoordinateAlpha = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinateAlpha");
        vTexCoordinateRgb = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinateRgb");
        vPosition = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
    }

    private void setupVertexBuffer() {
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);


    }


    private FloatBuffer floatBufferAlpha;
    private FloatBuffer floatBufferRgb;

    private void setupTexture(int setupTexturelocation) {
        float[] floatRgb = setupTexturelocation == 1 ? textureBufferQ : textureBufferT;
        float[] floatAlpha = setupTexturelocation == 1 ? textureBufferP : textureBufferS;
        ByteBuffer v2 = ByteBuffer.allocateDirect(floatAlpha.length * 4);
        v2.order(ByteOrder.nativeOrder());
        floatBufferAlpha = v2.asFloatBuffer();
        floatBufferAlpha.put(floatAlpha);
        floatBufferAlpha.position(0);

        ByteBuffer v1_1 = ByteBuffer.allocateDirect(floatRgb.length * 4);
        v1_1.order(ByteOrder.nativeOrder());
        floatBufferRgb = v1_1.asFloatBuffer();
        floatBufferRgb.put(floatRgb);
        floatBufferRgb.position(0);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("Texture generate");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        checkGlError("Texture bind");


    }

    @Override
    protected boolean draw() {
        synchronized (this) {
            if (frameAvailable) {
                //SurfaceTexture对象所关联的OpenGLES中纹理对象的内容将被更新为Image Stream中最新的图片
                videoTexture.updateTexImage();
                videoTexture.getTransformMatrix(videoTextureTransform);
                frameAvailable = false;
            } else {
                return false;
            }

        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (frameAvailable) {
            GLES20.glViewport(0, 0, width, height);
        }
        drawTexture();

        return true;
    }

    //绘制处理
    private void drawTexture() {
        drawTextureAnim(false);
    }

    private void drawTextureAnim(boolean arg8) {


        GLES20.glUseProgram(shaderProgram);
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glUniform1i(isYUV, 0);
        if (!arg8) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glUniform1i(texture, 0);
        }

        bindAndActiveTexture();
        GLES20.glEnableVertexAttribArray(vTexCoordinateAlpha);
        GLES20.glVertexAttribPointer(vTexCoordinateAlpha, 4, GLES20.GL_FLOAT, false, 0, floatBufferAlpha);
        GLES20.glEnableVertexAttribArray(vTexCoordinateRgb);
        GLES20.glVertexAttribPointer(vTexCoordinateRgb, 4, GLES20.GL_FLOAT, false, 0, floatBufferRgb);
        GLES20.glDrawElements(5, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(vTexCoordinateAlpha);
        GLES20.glDisableVertexAttribArray(vTexCoordinateRgb);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glUseProgram(0);
    }

    private void bindAndActiveTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glUniform1i(s_texture_2D_y, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        GLES20.glUniform1i(s_texture_2D_u, 2);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[3]);
        GLES20.glUniform1i(s_texture_2D_v, 3);
    }

    @Override
    protected void initGLComponents() {
        //textures[0] OpenGL纹理对象名称
        videoTexture = new SurfaceTexture(textures[0]);
        videoTexture.setOnFrameAvailableListener(this);
        videoTexture.setDefaultBufferSize(width, height);

        setupVertexBuffer();
        setupTexture(1);
        initTextureMapFilter();
        setupGraphics();
    }

    @Override
    protected void deinitGLComponents() {
        GLES20.glDeleteTextures(4, textures, 0);
        GLES20.glDeleteProgram(shaderProgram);
        videoTexture.release();
        videoTexture.setOnFrameAvailableListener(null);
    }


    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    @Override
    public SurfaceTexture getVideoTexture() {
        return videoTexture;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            frameAvailable = true;
        }
    }

    //纹理映射过滤
    private void initTextureMapFilter() {
        GLES20.glGenTextures(4, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[3]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

}
