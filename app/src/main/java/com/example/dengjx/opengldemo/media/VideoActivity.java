package com.example.dengjx.opengldemo.media;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.dengjx.opengldemo.R;

import java.io.IOException;

/**
 * Created by dengjx on 2017/10/12.
 */

public class VideoActivity extends Activity implements SurfaceHolder.Callback ,Camera.PreviewCallback{

    private static final String TAG = "VideoActivity";
    SurfaceView mSurfaceView;
    Button mBtnStart;
    SurfaceHolder mSurfaceHolder;
    Camera camera;
    Camera.Parameters parameters;
    private int mWidth = 320;
    private int mHeight = 240;
    VideoEncoder mVideoEncoder;
    boolean isStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
         initSurfaceView();
         mBtnStart.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 try {
                     isStart = true;
                     mVideoEncoder.start();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         });
    }

    private void initSurfaceView() {
        mSurfaceView = findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mBtnStart = findViewById(R.id.start);
        mVideoEncoder = new VideoEncoder();
        mVideoEncoder.setmVideoWidth(mWidth);
        mVideoEncoder.setmVideoHeight(mHeight);
        mVideoEncoder.prepare();
    }

    private void initCamera(){
        camera = Camera.open();
        parameters = camera.getParameters();
        parameters.setFlashMode("off"); // 无闪光灯
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setPreviewFormat(ImageFormat.YV12);
        parameters.setPictureSize(mWidth, mHeight);
        parameters.setPreviewSize(mWidth, mHeight);
        camera.setParameters(parameters);
        setOrientation();
        byte[] buf = new byte[mWidth * mHeight * 3 / 2];
        camera.addCallbackBuffer(buf);
        camera.setPreviewCallback(this);
    }

    private void setOrientation() {
        // 横竖屏镜头自动调整
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            parameters.set("orientation", "portrait"); //
            parameters.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍）
            camera.setDisplayOrientation(90); // 在2.2以上可以使用
        } else {// 如果是横屏
            parameters.set("orientation", "landscape"); //
            camera.setDisplayOrientation(0); // 在2.2以上可以使用
        }
    }
    @Override
    protected void onDestroy() {
        releaseCamera();
        super.onDestroy();
    }

    /**
     * 释放掉camera的引用
     */
    private void releaseCamera() {
        if (camera != null) {
            this.camera.setPreviewCallback(null);
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        long time = System.currentTimeMillis();
        Log.d(TAG,"bytes:"+bytes);
        if(isStart){
            mVideoEncoder.feedData(bytes,time);
        }
    }
}
