/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nativecodec;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NativeCodec extends Activity
{
    static final String TAG = "NativeCodec";

    String mSourceString = null;
    String mMoviePath = "/sdcard/aristocrat/movies";

    SurfaceView mSurfaceView1;
    SurfaceHolder mSurfaceHolder1;

    VideoSink mSelectedVideoSink;
    VideoSink mNativeCodecPlayerVideoSink;

    SurfaceHolderVideoSink mSurfaceHolder1VideoSink;

    boolean mCreated = false;
    boolean mIsPlaying = false;
    boolean mIsFullScreen = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        initFileList();

        // set up the Surface 1 video sink
        mSurfaceView1 = (SurfaceView) findViewById(R.id.surfaceview1);
        mSurfaceHolder1 = mSurfaceView1.getHolder();

        mSurfaceHolder1.addCallback(new SurfaceHolder.Callback()
        {

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                Log.v(TAG, "surfaceChanged format=" + format + ", width=" + width + ", height="
                        + height);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                Log.v(TAG, "surfaceCreated");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                Log.v(TAG, "surfaceDestroyed");
            }

        });

        if (mSurfaceHolder1VideoSink == null)
        {
            mSurfaceHolder1VideoSink = new SurfaceHolderVideoSink(mSurfaceHolder1);
        }
        mSelectedVideoSink = mSurfaceHolder1VideoSink;

        ((Button) findViewById(R.id.start_native)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                start();
            }
        });

        // native MediaPlayer rewind
        ((Button) findViewById(R.id.rewind_native)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mNativeCodecPlayerVideoSink != null)
                {
                    rewindStreamingMediaPlayer();
                }
            }
        });

        ((Button) findViewById(R.id.btnFile)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showFileSelector();
            }
        });

        ((Button) findViewById(R.id.btnFullScreen)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fullScreen();
            }
        });

        ((Button) findViewById(R.id.btnMagicMike)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                start("MagicMikeXXL-Attract_4K.mp4");
            }
        });

        ((Button) findViewById(R.id.btnWickedWinnings)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                start("WickedWinnings4_Attracts_4K.mp4");
            }
        });

        ((Button) findViewById(R.id.btnWonder4)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                start("Wonder4_Attracts_4K.mp4");
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean handled = false;

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BUTTON_A:
                if (mIsFullScreen)
                {
                    fullScreen();
                    handled = true;
                }
                break;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    void initFileList ()
    {
        List<String> movieList = new ArrayList<String>();

        File f = new File(mMoviePath);

        if (!f.exists())
        {
            boolean created = f.mkdirs();
            if (created)
            {
                f.setReadable(true, false);
                f.setWritable(true, false);

                File parent = f.getParentFile();
                String name;
                while (parent != null && (name =parent.getPath()) != "/sdcard")
                {
                    parent.setReadable(true, false);
                    parent.setWritable(true, false);
                    parent = parent.getParentFile();
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Create directory failed", Toast.LENGTH_LONG);
        }
        File file[] = f.listFiles();
        if (file != null)
        {
            for (int i = 0; i < file.length; i++)
            {
                movieList.add(file[i].getName());
            }
        }

        // initialize content source spinner
        Spinner sourceSpinner = (Spinner) findViewById(R.id.source_spinner);
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, movieList);
        //ArrayAdapter<CharSequence> sourceAdapter = ArrayAdapter.createFromResource(this, R.array.source_array, android.R.layout.simple_spinner_item);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(sourceAdapter);
        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                mSourceString = parent.getItemAtPosition(pos).toString();
                Log.v(TAG, "onItemSelected " + mSourceString);
            }

            @Override
            public void onNothingSelected(AdapterView parent)
            {
                Log.v(TAG, "onNothingSelected");
                mSourceString = null;
            }

        });
    }

    void start ()
    {
        if (!mCreated)
        {
            if (mNativeCodecPlayerVideoSink == null)
            {
                if (mSelectedVideoSink == null)
                {
                    return;
                }
                mSelectedVideoSink.useAsSinkForNative();
                mNativeCodecPlayerVideoSink = mSelectedVideoSink;
            }
            if (mSourceString != null)
            {
                String sourceFullPath = mMoviePath + "/" + mSourceString;
                mCreated = createStreamingMediaPlayer(sourceFullPath);
            }
        }
        if (mCreated)
        {
            mIsPlaying = !mIsPlaying;
            setPlayingStreamingMediaPlayer(mIsPlaying);
        }
    }

    void start (String filename)
    {
        mSourceString = filename;
        start();
        fullScreen();
    }

    void showFileSelector ()
    {
        LinearLayout files = (LinearLayout)findViewById(R.id.filePanel);
        if (files.getVisibility() == View.GONE)
            files.setVisibility(View.VISIBLE);
        else
            files.setVisibility(View.GONE);
    }

    void fullScreen ()
    {
        LinearLayout buttons = (LinearLayout)findViewById(R.id.buttonPanel);
        if (buttons.getVisibility() == View.GONE)
        {
            buttons.setVisibility(View.VISIBLE);
            Button btnFullScreen = (Button)findViewById(R.id.btnFullScreen);
            btnFullScreen.requestFocus();
            mIsFullScreen = false;
        }
        else
        {
            buttons.setVisibility(View.GONE);
            LinearLayout files = (LinearLayout) findViewById(R.id.filePanel);
            files.setVisibility(View.GONE);
            mIsFullScreen = true;
        }
    }

    void switchSurface()
    {
        if (mCreated && mNativeCodecPlayerVideoSink != mSelectedVideoSink)
        {
            // shutdown and recreate on other surface
            Log.i("@@@", "shutting down player");
            shutdown();
            mCreated = false;
            mSelectedVideoSink.useAsSinkForNative();
            mNativeCodecPlayerVideoSink = mSelectedVideoSink;
            if (mSourceString != null)
            {
                Log.i("@@@", "recreating player");
                mCreated = createStreamingMediaPlayer(mSourceString);
                mIsPlaying = false;
            }
        }
    }

    /**
     * Called when the activity is about to be paused.
     */
    @Override
    protected void onPause()
    {
        mIsPlaying = false;
        setPlayingStreamingMediaPlayer(false);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /**
     * Called when the activity is about to be destroyed.
     */
    @Override
    protected void onDestroy()
    {
        shutdown();
        mCreated = false;
        super.onDestroy();
    }


    /**
     * Native methods, implemented in jni folder
     */
    //public static native void createEngine();

    public static native boolean createStreamingMediaPlayer(String filename);

    public static native void setPlayingStreamingMediaPlayer(boolean isPlaying);

    public static native void shutdown();

    public static native void setSurface(Surface surface);

    public static native void rewindStreamingMediaPlayer();

    /** Load jni .so on initialization */
    static
    {
        System.loadLibrary("native-codec-jni");
    }

    // VideoSink abstracts out the difference between Surface and SurfaceTexture
    // aka SurfaceHolder and GLSurfaceView
    static abstract class VideoSink
    {

        abstract void setFixedSize(int width, int height);

        abstract void useAsSinkForNative();

    }

    static class SurfaceHolderVideoSink extends VideoSink
    {

        private final SurfaceHolder mSurfaceHolder;

        SurfaceHolderVideoSink(SurfaceHolder surfaceHolder)
        {
            mSurfaceHolder = surfaceHolder;
        }

        @Override
        void setFixedSize(int width, int height)
        {
            mSurfaceHolder.setFixedSize(width, height);
        }

        @Override
        void useAsSinkForNative()
        {
            Surface s = mSurfaceHolder.getSurface();
            Log.i("@@@", "setting surface " + s);
            setSurface(s);
        }

    }

    static class GLViewVideoSink extends VideoSink
    {

        private final MyGLSurfaceView mMyGLSurfaceView;

        GLViewVideoSink(MyGLSurfaceView myGLSurfaceView)
        {
            mMyGLSurfaceView = myGLSurfaceView;
        }

        @Override
        void setFixedSize(int width, int height)
        {
        }

        @Override
        void useAsSinkForNative()
        {
            SurfaceTexture st = mMyGLSurfaceView.getSurfaceTexture();
            Surface s = new Surface(st);
            setSurface(s);
            s.release();
        }

    }

}
