package com.example.dengjx.opengldemo.media;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dengjx on 2017/10/16.
 */

public class VideoDecoderThread extends Thread {
    private static String VIDEO = "video/";
    private static final String TAG = "VideoDecoder";
    private MediaCodec mMediaCodec;
    private MediaExtractor mMediaExtractor;
    private boolean bReceived;

    public boolean init(Surface surface , String filePath){
        bReceived = false;

        try {
            mMediaExtractor = new MediaExtractor();
            mMediaExtractor.setDataSource(filePath);
            for(int i = 0; i < mMediaExtractor.getTrackCount(); i++){
                MediaFormat format = mMediaExtractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                if(mine.startsWith(VIDEO)){
                    mMediaExtractor.selectTrack(i);
                    mMediaCodec =  MediaCodec.createDecoderByType(mine);

                    try {
                        Log.d(TAG, "format : " + format);
                        mMediaCodec.configure(format,surface,null,0);
                    }catch (IllegalStateException e){
                        Log.e(TAG, "codec '" + mine + "' failed configuration. " + e);
                        return false;
                    }
                    mMediaCodec.start();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
            return true;
    }

    @Override
    public void run() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        mMediaCodec.getOutputBuffers();

        boolean isInput = true;
        boolean first = false;
        long startWhen = 0;

        while (!bReceived){
            if(isInput){
                int inputIndex = mMediaCodec.dequeueInputBuffer(10000);
                if(inputIndex >= 0){
                    ByteBuffer inputBuffer = inputBuffers[inputIndex];
                    int sampleSize = mMediaExtractor.readSampleData(inputBuffer,0);
                    if(mMediaExtractor.advance() && sampleSize > 0){
                        mMediaCodec.queueInputBuffer(inputIndex,0,sampleSize,mMediaExtractor.getSampleTime(),0);
                    }else{
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mMediaCodec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isInput = false;
                    }
                }
            }
        int outIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo,10000);
        switch (outIndex){
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                mMediaCodec.getOutputBuffers();
                break;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mMediaCodec.getOutputFormat());
                break;
            default:
                if(!first){
                    startWhen = System.currentTimeMillis();
                    first = true;
                }
                long sleeptime = (bufferInfo.presentationTimeUs / 1000) - (System.currentTimeMillis() - startWhen);
                Log.d(TAG, "info.presentationTimeUs : " + (bufferInfo.presentationTimeUs / 1000) +
                        " playTime: " + (System.currentTimeMillis() - startWhen) + " sleepTime : " + sleeptime);
                if(sleeptime > 0){
                    try {
                        Thread.sleep(sleeptime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mMediaCodec.releaseOutputBuffer(outIndex,true);
                break;
        }
            // All decoded frames have been rendered, we can stop playing now
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaExtractor.release();
    }

    public void close(){
        bReceived = true;
    }
}
