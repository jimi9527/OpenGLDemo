package com.example.dengjx.opengldemo.draw;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

import com.example.dengjx.opengldemo.filter.GPUImageFilter;
import com.example.dengjx.opengldemo.filter.GPUImageFilterGroup;
import com.example.dengjx.opengldemo.filter.GPUImageFilterGroupBase;

/**
 * Created by dengjx on 2017/10/18.
 */

public class GPUImage {

    final static int DEFAULT_SURFACE_FIXED_WIDTH = 720;
    final static int DEFAULT_SURFACE_FIXED_HEIGHT = 1440;

    public final GPUImageRenderer mRenderer;
    private GLSurfaceView mGlSurfaceView;
    public Bitmap mCurrentBitmap;

    int mSurfaceFixedWidth = DEFAULT_SURFACE_FIXED_WIDTH;
    int mSurfaceFixedHeight = DEFAULT_SURFACE_FIXED_HEIGHT;


    public GPUImage(Context context , OnSurfaceListener onSurfaceListener) {
        if(!supportsOpenGLES2(context)){
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }
        mCurrentBitmap = null;
        GPUImageFilterGroupBase groupBase = new GPUImageFilterGroup();
        groupBase.addFilter(new GPUImageFilter());



    }


    // 检查是否支持openGles2.0
    public boolean supportsOpenGLES2(Context context){
        final ActivityManager activityManager = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }
}
