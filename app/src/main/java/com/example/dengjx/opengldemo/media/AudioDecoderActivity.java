package com.example.dengjx.opengldemo.media;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.example.dengjx.opengldemo.R;

/**
 * Created by dengjx on 2017/10/16.
 */

public class AudioDecoderActivity extends Activity {

    private  AudioDecoderThread audioDecoderThread ;
    Button mStart;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audiodecoder);
        mStart = findViewById(R.id.play);
        audioDecoderThread = new AudioDecoderThread();


        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = PathUtil.getExistFilePath(PathUtil.AUDIO_RECORDER_FOLDER,PathUtil.AUDIO_RECORDER_TEMP_FILE);
                audioDecoderThread.startPlay(path);
            }
        });
    }
}
