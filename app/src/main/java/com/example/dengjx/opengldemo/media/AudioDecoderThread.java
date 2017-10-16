package com.example.dengjx.opengldemo.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dengjx on 2017/10/16.
 */

public class AudioDecoderThread {
    private static final int TIMEOUT_US = 1000;
    private static final String TAG = "AudioDecoder";
    private MediaCodec mMediaCodec;
    private MediaExtractor mMediaExtractor;

    private boolean bReceived;
    private int mSampleRate = 0 ;

    public void startPlay(String path ){
        bReceived = false;
        mMediaExtractor = new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int channle = 0;
        for(int i = 0 ; i < mMediaExtractor.getTrackCount() ; i++){
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith("audio/")){
                mMediaExtractor.selectTrack(i);
                Log.d("TAG", "format : " + mediaFormat);
                ByteBuffer csd = mediaFormat.getByteBuffer("csd-0");

                for(int k = 0 ; k < csd.capacity() ; k++){
                    Log.d("TAG", "csd : " + csd.array()[k]);
                }
                mSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                channle = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                break;
            }
        }
        MediaFormat format = makeAACCodeSpecificData(MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                mSampleRate , channle);
         if(format == null){
             return;
         }
        try {
            mMediaCodec = MediaCodec.createDecoderByType("audio/mp4a-latm");
            mMediaCodec.configure(format,null,null,0);

            if (mMediaCodec == null) {
                Log.e(TAG, "Can't find video info!");
                return;
            }
            mMediaCodec.start();
            Log.e(TAG, "start");
            new Thread(AACDecoderAndPlayRunnable).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private MediaFormat makeAACCodeSpecificData(int audioProfile, int smapleRate ,int channel){
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, smapleRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channel);

        int samplingFreq[] = {
                96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
                16000, 12000, 11025, 8000
        };
        int sampleIndex = -1;
        for(int i = 0; i < samplingFreq.length; i++){
            if(samplingFreq[i] == smapleRate){
                smapleRate = i ;
            }
        }
        if(sampleIndex == -1){
            return null;
        }
        ByteBuffer csd = ByteBuffer.allocate(2);
        csd.put((byte) ((audioProfile << 3) | (sampleIndex >> 1)));
        csd.position(1);
        csd.put((byte) ((byte) ((sampleIndex << 7) & 0x80) | (channel << 3)));
        csd.flip();
        format.setByteBuffer("csd-0", csd); // add csd-0
        for (int k = 0; k < csd.capacity(); ++k) {
            Log.e(TAG, "csd : " + csd.array()[k]);
        }

        return format;
    }
    Runnable AACDecoderAndPlayRunnable = new Runnable() {

        @Override
        public void run() {
            AACDecoderAndPlay();
        }
    };


    public void AACDecoderAndPlay(){
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outBuffers = mMediaCodec.getOutputBuffers();

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int buffsize = AudioTrack.getMinBufferSize(mSampleRate,AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC , mSampleRate , AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
        while (!bReceived){
            int inIndex = mMediaCodec.dequeueInputBuffer(10000);
            Log.d(TAG,"inIndex:"+inIndex);
            if(inIndex >= 0){
                ByteBuffer buffer = inputBuffers[inIndex];
                int sampleSize = mMediaExtractor.readSampleData(buffer,0);
                if(sampleSize < 0){
                    Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                    mMediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }else {
                    mMediaCodec.queueInputBuffer(inIndex,0,sampleSize,mMediaExtractor.getSampleTime(),0);
                    mMediaExtractor.advance();
                }

             int outIndex = mMediaCodec.dequeueOutputBuffer(info,10000);
                Log.d(TAG,"outIndex:"+outIndex);
             switch (outIndex){
                 case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                     outBuffers = mMediaCodec.getOutputBuffers();
                     break;
                 case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                     MediaFormat format = mMediaCodec.getOutputFormat();
                     Log.d("DecodeActivity", "New format " + format);
                     audioTrack.setPlaybackRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                     break;
                 case MediaCodec.INFO_TRY_AGAIN_LATER:
                     Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                     break;
                 default:
                     ByteBuffer outBuffer = outBuffers[outIndex];
                     final byte[] chunk = new byte[info.size];
                     outBuffer.get(chunk);
                     outBuffer.clear();

                     audioTrack.write(chunk , info.size , info.offset+info.size);
                     mMediaCodec.releaseOutputBuffer(outIndex , false);
                     break;
             }
                // All decoded frames have been rendered, we can stop playing now
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
            }

        }
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;

        mMediaExtractor.release();
        mMediaExtractor = null ;

        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;

    }
    public void stop(){
        bReceived = true ;
    }

}
