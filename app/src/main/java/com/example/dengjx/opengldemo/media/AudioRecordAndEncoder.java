package com.example.dengjx.opengldemo.media;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.io.IOException;

/**
 * author: daxiong9527
 * mail : 15570350453@163.com
 */

public class AudioRecordAndEncoder implements Runnable {
    private AudioRecord mAudioRecord;
    private MediaCodec mMediaCoderc;

    //采样率
    private int smapleRate = 44100;
    //采样通道
    private int channelCount = 2;
    //通道设置
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    //采样数据格式 16bit 兼容所有手机
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int buffsize;

    private String mime = "audio/mp4a-latm";
    private int rate=256000;

    //prepare init
    public void prepare(){

        //音频录制初始化
        buffsize = AudioRecord.getMinBufferSize(smapleRate,channelConfig,audioFormat) * 2;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,smapleRate,channelConfig,audioFormat,buffsize);

        //音频编码
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

    @Override
    public void run() {

    }
}
