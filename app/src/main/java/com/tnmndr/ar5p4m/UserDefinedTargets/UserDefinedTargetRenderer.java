/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.tnmndr.ar5p4m.UserDefinedTargets;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.qualcomm.ar.pl.DebugLog;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;
import com.tnmndr.ar5p4m.R;
import com.tnmndr.ar5p4m.SampleApplication.SampleApplicationControl;
import com.tnmndr.ar5p4m.SampleApplication.SampleApplicationSession;
import com.tnmndr.ar5p4m.SampleApplication.utils.CubeShaders;
import com.tnmndr.ar5p4m.SampleApplication.utils.SampleUtils;
import com.tnmndr.ar5p4m.SampleApplication.utils.Teapot;
import com.tnmndr.ar5p4m.SampleApplication.utils.Texture;

// for texture screenshot
import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import android.os.Environment;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;


// The renderer class for the ImageTargetsBuilder sample.
public class UserDefinedTargetRenderer extends UserDefinedTargets implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "UserDefinedTargetRenderer";

    public SampleApplicationSession vuforiaAppSession;
    
    public boolean mIsActive = false;
    
    private Vector<Texture> mTextures;
    
    private int shaderProgramID;
    
    private int vertexHandle;
    
    private int normalHandle;
    
    private int textureCoordHandle;
    
    private int mvpMatrixHandle;
    
    private int texSampler2DHandle;

    // Constants:
    static final float kObjectScale = 3.f;
    
    private Teapot mTeapot;
    
    // Reference to main activity
    private UserDefinedTargets mActivity;


   ////screenshot
  private int mViewWidth = 0; // screenshot
  private int mViewHeight = 0;  // screenshot

    private boolean TakeScreenShot = false;//screenshot
    public int mRotation2;
//public float yRot;

    public UserDefinedTargetRenderer(UserDefinedTargets activity,
        SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;

    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        // Call function to initialize rendering:
       //DisplayMetrics dm = new DisplayMetrics();
       //mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        initRendering();

        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):

    }



    //public float yRot;

    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        mViewWidth = width;    //screenshot
        mViewHeight = height;  //screenshot

        // Call function to update rendering when render surface
        // parameters have changed:


        Configuration config = mActivity.getResources().getConfiguration();

        mActivity.getScreenOrientation();
        mActivity.Rotation();




        mActivity.updateRendering();


        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);


    }



    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();
        //for screenshot
       if (TakeScreenShot) {
           saveScreenShot(0, 0, mViewWidth, mViewHeight, "screentexture.png", gl);

           TakeScreenShot = false;
       }
    }


    public void renderFrame()
    {


        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Get the state from Vuforia and mark the beginning of a rendering
        // section
        State state = Renderer.getInstance().begin();
        
        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera
            
        // Render the RefFree UI elements depending on the current state
        mActivity.refFreeFrame.render();
        
        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
            Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(trackableResult.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            
            float[] modelViewProjection = new float[16];
            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, kObjectScale);
            Matrix.rotateM(modelViewMatrix, 0, yRot,0, 0, 1);



            Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale,
                kObjectScale);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            GLES20.glUseProgram(shaderProgramID);
            
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
            
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);
            
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(0).mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                mTeapot.getIndices());
            
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
            
            SampleUtils.checkGLError("UserDefinedTargets renderFrame");
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        Renderer.getInstance().end();
    }




    private void initRendering()
    {
        Log.d(LOGTAG, "initRendering");
        
        mTeapot = new Teapot();
        
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        
        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");
    }




    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
    }

    public void saveScreenShot() {             //screenshot
        TakeScreenShot = true;
        //screenshot
    }


    //for screenshot
    private void saveScreenShot(int x, int y, int w, int h, String filename, GL10 gl) {
        Bitmap bmp = grabPixels(x, y, w, h, gl);
        try {
            File directory = new File (Environment.getExternalStorageDirectory() + "/DCIM/AR_5P4M");
            directory.mkdirs();
            Log.d("UserDefinedTarget", "Can write: " + Environment.getExternalStorageDirectory().canWrite());

            File folder = new File(Environment.getExternalStorageDirectory() + "/DCIM/AR_5P4M");
            boolean success = false;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (!success) {
            }
            else {
            }

            Log.d(LOGTAG, "dir_created");

            // int i = 0;

            String timeStamp =
                    new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            String filename1 = "AR_5P4M" + ".png";
            File File = new File(directory, filename1);

            while (File.exists())
            {
                int i = 0;
                i++;
                filename1 = "AR_5P4M" + timeStamp + ".png";
                File = new File(directory, filename1);
            }

            String path = Environment.getExternalStorageDirectory() + "/DCIM/AR_5P4M/" + filename;

            // DebugLog.LOGD(path);

            File file = new File(path);
            File.createNewFile();

            FileOutputStream fos = new FileOutputStream(File);
            bmp.compress(CompressFormat.PNG, 100, fos);

            fos.flush();

            fos.close();

        }
        catch (Exception e) {
            //DebugLog.LOGD(e.getStackTrace().toString());
        }
    }

    private Bitmap grabPixels(int x, int y, int w, int h, GL10 gl) {
        int b[] = new int[w * (y + h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);

        GLES20.glReadPixels(x, 0, w, y + h,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

        for (int i = 0, k = 0; i < h; i++, k++) {
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }

        Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
        return sb;
    }
//screenshot end

}
