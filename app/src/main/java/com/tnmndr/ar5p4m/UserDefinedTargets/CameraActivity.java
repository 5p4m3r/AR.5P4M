package com.tnmndr.ar5p4m.UserDefinedTargets;

/**
 * Created by tnmndr on 10.01.2016.
 */
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.tnmndr.ar5p4m.R;

public class CameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }

    }



}