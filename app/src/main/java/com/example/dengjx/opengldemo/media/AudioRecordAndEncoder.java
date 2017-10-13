package com.example.dengjx.opengldemo.media;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: daxiong9527
 * mail : 15570350453@163.com
 */

public class AudioRecordAndEncoder implements Runnable {
    private static final String TAG = "AudioRecord";

    private AudioRecord mAudioRecord;
    private MediaCodec mMediaCoderc;

    // 采样率
    private int smapleRate = 44100;
    // 采样通道
    private int channelCount = 2;
    // 通道设置
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 采样数据格式 16bit 兼容所有手机
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int buffsize;

    private String mime = "audio/mp4a-latm";
    private int rate=256000;
    private FileOutputStream mAudioFos;
    private Thread mThread;
    // 是否在开始了录制
    private boolean isRecording;

    // prepare init
    public void prepare(){
        try {
            String path = PathUtil.getFilePath(PathUtil.AUDIO_RECORDER_FOLDER,
                    PathUtil.AUDIO_RECORDER_TEMP_FILE);
            Log.d(TAG,"path:"+path);
            mAudioFos = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            Log.d(TAG,"audio FileNotFoundException");
            e.printStackTrace();
        }

        // 音频录制初始化
        buffsize = AudioRecord.getMinBufferSize(smapleRate,channelConfig,audioFormat) * 2;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,smapleRate,channelConfig,audioFormat,buffsize);

        // 音频编码
        MediaFormat format = MediaFormat.createAudioFormat(mime,smapleRate,channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, rate);
        try {
            mMediaCoderc = MediaCodec.createEncoderByType(mime);
            mMediaCoderc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 开始录制
    public void start() throws InterruptedException {
        mMediaCoderc.start();
        mAudioRecord.startRecording();
        if(mThread != null && mThread.isAlive()){
            isRecording = false;
            mThread.join();
        }
        isRecording = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void run() {
        while (isRecording){
            try {
                readOutputData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // 得到输入缓存
    private ByteBuffer getInputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mMediaCoderc.getInputBuffer(index);
        }else{
            return mMediaCoderc.getInputBuffers()[index];
        }
    }

    // 得到输出缓存
    private ByteBuffer getOutputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mMediaCoderc.getOutputBuffer(index);
        }else{
            return mMediaCoderc.getOutputBuffers()[index];
        }
    }

    private void readOutputData() throws IOException {
        int index = mMediaCoderc.dequeueInputBuffer(-1);
        if(index > 0){
            ByteBuffer buffer = getInputBuffer(index);
            buffer.clear();
            int legth = mAudioRecord.read(buffer,buffsize);
            if(legth > 0){
                mMediaCoderc.queueInputBuffer(index,0,legth,System.nanoTime()/1000,0);
            }else{
                Log.d(TAG,"Length:"+legth);
            }
        }
        MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
        int outIndex;
        do {
            outIndex = mMediaCoderc.dequeueOutputBuffer(mInfo , 0);
            Log.d(TAG,"mInfo.flags:" + mInfo.flags);
            if(outIndex > 0){
                ByteBuffer buffer = getOutputBuffer(outIndex);
                buffer.position(mInfo.offset);
                byte[] temp = new byte[mInfo.size + 7];
                buffer.get(temp , 7 , mInfo.size);
                addADTStoPacket(temp,temp.length);
                mAudioFos.write(temp);
                mMediaCoderc.releaseOutputBuffer(outIndex,false);
            }
        }while (outIndex >= 0);
    }
    /**
     * 给编码出的aac裸流添加adts头字段
     * @param packet 要空出前7个字节，否则会搞乱数据
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
        packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
        packet[4] = (byte)((packetLen&0x7FF) >> 3);
        packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
        packet[6] = (byte)0xFC;
    }

    //结束
    public void stop(){
        isRecording = false;
        if(mThread != null){
            try {
                mThread.join();
                mAudioRecord.stop();
                mMediaCoderc.stop();
                mMediaCoderc.release();
                mAudioFos.flush();
                mAudioFos.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
