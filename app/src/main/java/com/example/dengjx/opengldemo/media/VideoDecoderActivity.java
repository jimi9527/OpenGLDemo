package com.example.dengjx.opengldemo.media;

import android.app.Activity;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static com.example.dengjx.opengldemo.media.PathUtil.VIDEO_RECORDER_FOLDER;
import static com.example.dengjx.opengldemo.media.PathUtil.VIDEO_RECORDER_TEMP_FILE;

/**
 * 视频解码
 * Created by dengjx on 2017/10/16.
 */

public class VideoDecoderActivity extends Activity implements SurfaceHolder.Callback {
    private VideoDecoderThread mVideoDecoderThread;
    private String path = PathUtil.getExistFilePath(VIDEO_RECORDER_FOLDER,VIDEO_RECORDER_TEMP_FILE);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.getHolder().addCallback(this);
        setContentView(surfaceView);
        mVideoDecoderThread = new VideoDecoderThread();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            if(mVideoDecoderThread != null){
                if (mVideoDecoderThread.init(surfaceHolder.getSurface(), path)) {
                    mVideoDecoderThread.start();
                }else {
                    mVideoDecoderThread = null;
                }
            }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (mVideoDecoderThread != null) {
            mVideoDecoderThread.close();
         }
    }
}
