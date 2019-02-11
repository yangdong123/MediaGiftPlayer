# PlayVideo-OpenGL 通过Opengl ES去绘制视频实现礼物动效

### 实现礼物动效的方式
1.Lottie 动画是实现 ， json 文件描述，这个实现方式对于简单的动效实现好，但是对于复杂的动画就不能使用了

2.序列帧方式实现 ， 使用帧动画。可以实现比较复杂的动画，但是一个需要动画添加很多张图片，势必会导致apk体积变大，当然也可以使用后台配置序列帧图片，使用序列帧实现复杂的动画设备性能处理也较为复杂，并且还要根据不同的尺寸进行适配等。

3.GIF 方式实现 使用 Gif 占用空间较大，而且需要为各种屏幕尺寸、分辨率做适配，并且Android本是不支持gif直接展示的。

4.SVGA 方式实现 这个是由YY出的礼物动效方案

5.通过Opengl + Texture的方式实现 ，使用硬解码，直接在设置的Renderer实现类中实现gl展示即可

### 下面通过实现方式5的原理及步骤

 #### 1.设置绘制的区域尺寸  绘制次序
 
      private static float squareSize = 1.0f;
      private static float squareCoords[] = {
            -squareSize,  squareSize,   // top left
            -squareSize, -squareSize,   // bottom left
            squareSize, -squareSize,    // bottom right
            squareSize,  squareSize,   // top right
        };
        
 #### 2.绘制次序
 
      private static short drawOrder[] = {
         0, 1, 2, 
         0, 2, 3
       };
     
 #### 3.纹理坐标
 
    float[] textureBufferP = new float[]{0f, 1f, 0f, 1f, 0f, 0f, 0f, 1f, 0.5f, 0f, 0f, 1f, 0.5f, 1f, 0f, 1f};
    float[] textureBufferQ = new float[]{0.5f, 1f, 0f, 1f, 0.5f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 1f};
    float[] textureBufferS = new float[]{0f, 1f, 0f, 1f, 0f, 0.5f, 0f, 1f, 1f, 0.5f, 0f, 1f, 1f, 1f, 0f, 1f};
    float[] textureBufferT = new float[]{0f, 0.5f, 0f, 1f, 0f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0.5f, 0f, 1f};

 #### 4.生成纹理的真实数据
 
    int[] textures = new int[4];
    
 #### 5.初始化Gl相关参数
 
        initEGL();
        initGLComponents();
        
 #### 6.设置顶点缓存
 
        setupVertexBuffer();
        
 #### 7.接着初始化纹理
 
        setupTexture();
        
 #### 8.创建着色器（顶点着色器，片段着色器）
 
        setupGraphics（);
        
 #### 9.加载顶点与片段着色器
 
        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        shaderProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"texture", "vPosition", "vTexCoordinate", "textureTransform"});
                
 #### 10.进行纹理绘制 要是一直行绘制需要调用SurfaceTexture.updateTexImage()(从的图像流中更新纹理图像到最近的帧中。这个函数仅仅当拥有这个纹理的           OpenglES上下文当前正处在绘制线程时被调用)绘制出纹理图像
 
        drawTexture();
   
 #### 11.创建 TextureSurfaceRenderer  获取到Surface然后给播放器设置Surface
 
        new VideoTextureSurfaceRenderer(this, surfaceTexture, surfaceWidth, surfaceHeight);
         Surface surface = new Surface(videoRenderer.getVideoTexture());
         Media//硬解 软解处理
         Media.setDataSource(videoPath);
         Media.setSurface(surface);
  
 #### 12.等待onSurfaceTextureAvailable 回调成功后开始解码获取流数据
   

