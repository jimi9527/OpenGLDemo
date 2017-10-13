package com.example.dengjx.opengldemo.media;

import android.app.Activity;
import android.media.AudioRecord;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.example.dengjx.opengldemo.R;

/**
 * Created by dengjx on 2017/10/12.
 */

public class RecordActivity extends Activity {
    Button mBtnStart;
    AudioRecordAndEncoder mAudioRecordAndEncoder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnStart = (Button) findViewById(R.id.start);
        mAudioRecordAndEncoder = new AudioRecordAndEncoder();
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mAudioRecordAndEncoder.prepare();
                    mAudioRecordAndEncoder.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioRecordAndEncoder.stop();
    }
}
