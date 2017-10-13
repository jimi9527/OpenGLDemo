package com.example.dengjx.opengldemo.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by dengjx on 2017/10/12.
 */

public class VideoEncoder implements Runnable {

    private static final String TAG = "VideoEncoder";
    private String mime="video/avc";
    private int rate = 256000;
    private int frameRate=24;
    private int frameInterval=1;

    private FileOutputStream mVideofos;
    private MediaCodec mMediaCodec;
    private int mVideoWidth , mVideoHeight;
    private Thread mThread;
    private boolean mStartFlag;

    private byte[] nowFeedData;
    private long nowTimeStep;
    private boolean hasNewData=false;

    private byte[] mHeadInfo=null;
    private int fpsTime;

    public void setmVideoWidth(int mVideoWidth) {
        this.mVideoWidth = mVideoWidth;
    }

    public void setmVideoHeight(int mVideoHeight) {
        this.mVideoHeight = mVideoHeight;
    }

    public VideoEncoder(){
        fpsTime=1000/frameRate;
    }

    public void prepare(){
        try {
            String path  = PathUtil.getFilePath(PathUtil.VIDEO_RECORDER_FOLDER,PathUtil.VIDEO_RECORDER_TEMP_FILE);
            Log.d(TAG,"path:" + path);
            mVideofos = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            MediaFormat format = MediaFormat.createVideoFormat(mime,mVideoWidth,mVideoHeight);
            format.setInteger(MediaFormat.KEY_BIT_RATE,rate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE,frameRate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,frameInterval);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities
                    .COLOR_FormatYUV420Planar);
            mMediaCodec = MediaCodec.createEncoderByType(mime);
            mMediaCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //开始录制
    public void start() throws InterruptedException {
        nowTimeStep =System.nanoTime();
        if(mThread != null && mThread.isAlive()){
            mStartFlag = false;
            mThread.join();
        }
        mMediaCodec.start();
        mThread = new Thread(this);
        mStartFlag = true;
        mThread.start();
    }
    /**
     * 由外部喂入一帧数据
     * @param data RGBA数据
     */
    public void feedData(final byte[] data){

        hasNewData=true;
        nowFeedData=data;
    }

    byte[] yuv;
    private void rgbaToYuv(byte[] rgba,int width,int height,byte[] yuv){
        final int frameSize = width * height;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + frameSize/4;

        int R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                index = j * width + i;
                if(rgba[index*4]>127||rgba[index*4]<-128){
                    Log.e("color","-->"+rgba[index*4]);
                }
                R = rgba[index*4]&0xFF;
                G = rgba[index*4+1]&0xFF;
                B = rgba[index*4+2]&0xFF;

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv[uIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv[vIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }
            }
        }
    }
    private ByteBuffer getInputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mMediaCodec.getInputBuffer(index);
        }else{
            return mMediaCodec.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mMediaCodec.getOutputBuffer(index);
        }else{
            return mMediaCodec.getOutputBuffers()[index];
        }
    }

    private void readOutputData(byte[] data) throws IOException {
        int index = mMediaCodec.dequeueInputBuffer(-1);
        Log.d(TAG,"index:"+index);
        if(index >= 0){
            if(hasNewData){
                if(yuv == null){
                    yuv = new byte[mVideoWidth * mVideoHeight * 3 /2];
                }
                rgbaToYuv(data,mVideoWidth,mVideoHeight,yuv);

            }
            ByteBuffer buffer = getInputBuffer(index);
            buffer.clear();
            buffer.put(yuv);
            Log.d(TAG,"yuv.length:"+yuv.length);
            Log.d(TAG,"(System.nanoTime()-nowTimeStep):"+(System.nanoTime()-nowTimeStep)/1000);
            mMediaCodec.queueInputBuffer(index,0,yuv.length,(System.nanoTime()-nowTimeStep)/1000,
                    mStartFlag?0:MediaCodec.BUFFER_FLAG_END_OF_STREAM);

        }
        MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
        int outIndex = mMediaCodec.dequeueOutputBuffer(mInfo , 0);
        Log.d(TAG,"outIndex:"+outIndex);
        while (outIndex >= 0){

            ByteBuffer buffer = getOutputBuffer(outIndex);
            byte[] temp=new byte[mInfo.size];
            buffer.get(temp);
            if(mInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG){
                Log.e(TAG,"start frame");
                mHeadInfo = new byte[temp.length];
                mHeadInfo = temp ;
            }else if(mInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME){
                Log.e(TAG,"key frame");
                byte[] keyframe = new byte[temp.length + mHeadInfo.length];
                System.arraycopy(mHeadInfo, 0, keyframe, 0, mHeadInfo.length);
                System.arraycopy(temp, 0, keyframe, mHeadInfo.length, temp.length);
                Log.e(TAG,"other->"+mInfo.flags);
                mVideofos.write(keyframe,0,keyframe.length);
            }else if(mInfo.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                Log.e(TAG,"end frame");
            }else{
                mVideofos.write(temp,0,temp.length);
            }
            mMediaCodec.releaseOutputBuffer(outIndex,false);
            outIndex = mMediaCodec.dequeueOutputBuffer(mInfo,0);
        }

    }

    @Override
    public void run() {
        if(mStartFlag){
            long time = System.currentTimeMillis();
            Log.d(TAG,"nowFeedData:"+nowFeedData);
            if(nowFeedData != null){
                try {
                    readOutputData(nowFeedData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            long ltime = System.currentTimeMillis() - time;
            if(fpsTime > ltime){
                try {
                    Thread.sleep(fpsTime-ltime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
