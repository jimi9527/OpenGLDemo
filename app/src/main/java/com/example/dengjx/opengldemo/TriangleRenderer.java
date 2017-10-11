package com.example.dengjx.opengldemo;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * 三角形的渲染器
 * Created by dengjx on 2017/10/9.
 */

public class TriangleRenderer implements GLSurfaceView.Renderer {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mPositionHandle;
    private int mColorHandle;
    private int mProgram;

    private FloatBuffer vertexBuffer;
    static final int COORDS_VERTEX = 3;
    static float triangleCoords[]={
            0.0f,  1.0f, 0.0f, // 顶点
            -1.0f, -0.0f, 0.0f, // 左下角
            1.0f, -0.0f, 0.0f  // 右下角
    };
    float color[]={0.0f, 1.0f, 0f, 1.0f
    };
    private final int vertexCount = triangleCoords.length / COORDS_VERTEX;
    private final int vertextStride = COORDS_VERTEX * 4 ;


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置背景颜色
        glClearColor(1.0f,1.0f,1.0f,1.0f);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        //编译shader
        int vertextShader = loadShader(GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = loadShader(GL_FRAGMENT_SHADER,fragmentShaderCode);
        mProgram = glCreateProgram();
        glAttachShader(mProgram,vertextShader);
        glAttachShader(mProgram,fragmentShader);
        glLinkProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        //绘制窗口
        glViewport(0,0,i,i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //重绘背景色
        glClear(GL_COLOR_BUFFER_BIT);
        onDraw();
    }

    public static int loadShader(int type, String shaderCode){
        int shader = glCreateShader(type);
        glShaderSource(shader,shaderCode);
        glCompileShader(shader);
        return shader;
    }

    //绘制
    private void onDraw(){
        glUseProgram(mProgram);
        mPositionHandle = glGetAttribLocation(mProgram,"vPosition");
        glEnableVertexAttribArray(mPositionHandle);
        glVertexAttribPointer(mPositionHandle, COORDS_VERTEX,GL_FLOAT,false,vertextStride,vertexBuffer);
        mColorHandle = glGetUniformLocation(mProgram,"vColor");
        glUniform4fv(mColorHandle,1,color,0);
        glDrawArrays(GL_TRIANGLES,0,vertexCount);
        glDisableVertexAttribArray(mPositionHandle);
    }
}
