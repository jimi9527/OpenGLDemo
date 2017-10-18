package com.example.dengjx.opengldemo.draw;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
public interface CameraPreviewCallback {
    void onPreviewFrame(byte[] data, ICameraLoader cameraLoader);
}
