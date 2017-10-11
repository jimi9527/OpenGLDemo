package com.example.dengjx.opengldemo;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 通过纹理显示一张图片
 */
public class ShowImageActivity extends AppCompatActivity {
    GLSurfaceView glSurfaceView ;
    //判断是否设置渲染器成功
    boolean rendererSet;
    ImageRenderer imageRenderer ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        glSurfaceView = new GLSurfaceView(this);
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportEs2 = configurationInfo.reqGlEsVersion >= 0x0000;
        if(supportEs2){
            glSurfaceView.setEGLContextClientVersion(2);
            imageRenderer = new ImageRenderer(this);

            glSurfaceView.setRenderer(imageRenderer);

            //只有在绘制数据变化的时候才绘制view
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            rendererSet = true;
        }
        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(rendererSet){
            glSurfaceView.onPause();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(rendererSet){
            glSurfaceView.onResume();
        }
    }
}
