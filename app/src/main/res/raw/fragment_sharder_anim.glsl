#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES texture;
uniform sampler2D s_texture_2D_Y;
uniform sampler2D s_texture_2D_U;
uniform sampler2D s_texture_2D_V;
varying vec2 v_TexCoordinateAlpha;
varying vec2 v_TexCoordinateRgb;
uniform bool isYUV;
mediump vec3 yuv2rgb(in mediump vec3 yuv) {
         const mediump vec3 offset = vec3(-0.0625, -0.5, -0.5);
         const mediump vec3 Rcoeff = vec3( 1.164, 0.000,  1.596);
         const mediump vec3 Gcoeff = vec3( 1.164, -0.391, -0.813);
         const mediump vec3 Bcoeff = vec3( 1.164, 2.018,  0.000);
         mediump vec3 rgb;
         yuv += offset;
         rgb.r = dot(yuv, Rcoeff);
         rgb.g = dot(yuv, Gcoeff);
         rgb.b = dot(yuv, Bcoeff);
         return rgb;
  }
  void main () {
      if (isYUV) {
          mediump vec3 yuv;
          yuv.x = texture2D(s_texture_2D_Y, v_TexCoordinateRgb).x;
          yuv.y  = texture2D(s_texture_2D_U, v_TexCoordinateRgb).x;
          yuv.z  = texture2D(s_texture_2D_V, v_TexCoordinateRgb).x;
          vec4 rgbColor = vec4(yuv2rgb(yuv), 1.0);
          yuv.x = texture2D(s_texture_2D_Y, v_TexCoordinateAlpha).x;
          yuv.y  = texture2D(s_texture_2D_U, v_TexCoordinateAlpha).x;
          yuv.z  = texture2D(s_texture_2D_V, v_TexCoordinateAlpha).x;
          vec4 alphaColor = vec4(yuv2rgb(yuv), 1.0);
          gl_FragColor = vec4(rgbColor.r, rgbColor.g, rgbColor.b, alphaColor.r);
      } else {
          vec4 alphaColor = texture2D(texture, v_TexCoordinateAlpha);
          vec4 rgbColor = texture2D(texture, v_TexCoordinateRgb);
          gl_FragColor = vec4(rgbColor.r, rgbColor.g, rgbColor.b, alphaColor.r);
      }
  }