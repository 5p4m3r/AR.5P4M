package com.tnmndr.ar5p4m;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tnmndr.ar5p4m.UserDefinedTargets.CameraActivity;


/**
 * Created by tnmndr on 10.01.2016.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this,CameraActivity.class);
        startActivity(intent);
        finish();
    }
}