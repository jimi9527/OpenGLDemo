package com.example.dengjx.opengldemo.glsurfacecamera;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by dengjx on 2017/10/11.
 */

public class GlcameraActivity extends Activity{
    MyGLSurfaceView myGLSurfaceView;
    private MyCamera myCamera;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myCamera = new MyCamera();
        myGLSurfaceView = new MyGLSurfaceView(this,myCamera);
        setContentView(myGLSurfaceView);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        myCamera.stop();
    }
}
