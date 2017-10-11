package com.example.dengjx.opengldemo.glsurfacecamera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

/**
 * Created by dengjx on 2017/10/11.
 */

public class MyCamera {
    private Camera mCamera;
    private Camera.Parameters parameters;
    private boolean isCamera = false;

    void start(SurfaceTexture surfaceTexture){
        mCamera = Camera.open(0);
        parameters = mCamera.getParameters();
        try {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
            isCamera = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stop(){
        if(isCamera){
            if(null != mCamera){
                mCamera.stopPreview();
                mCamera.release();
                isCamera = false;
            }
        }
    }

}
