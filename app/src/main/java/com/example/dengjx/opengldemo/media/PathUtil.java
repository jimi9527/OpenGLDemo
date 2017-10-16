package com.example.dengjx.opengldemo.media;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * 音视频的路径
 * Created by dengjx on 2017/10/12.
 */

public class PathUtil {
    private static final String TAG = "PathUtil";

    public static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";

    public static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.aac";

    public static final String VIDEO_RECORDER_FOLDER = "VideoRecorder";
    public static final String VIDEO_RECORDER_TEMP_FILE = "video_temp.mp4";

    public static String getFilePath(String folder , String path){
        String rootpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(rootpath,folder);
        if(!file.exists()){
            file.mkdirs();
        }
        String filepath = file.getAbsolutePath() + File.separator + path;
        Log.d(TAG,"filepath:" + filepath);
        File tempFile = new File(filepath);
        if(tempFile.exists()){
            tempFile.delete();
        }
        return filepath;
    }

    // 返回已经存在的文件路径
    public static String getExistFilePath(String folder , String path){
        String rootpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(rootpath,folder);
        if(!file.exists()){
            file.mkdirs();
        }
        String filepath = file.getAbsolutePath() + File.separator + path;
        return filepath;
    }
}
