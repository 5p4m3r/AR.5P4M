package com.tnmndr.ar5p4m;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.tnmndr.ar5p4m.UserDefinedTargets.CameraActivity;
import com.tnmndr.ar5p4m.UserDefinedTargets.UserDefinedTargets;

/**
 * Created by 5p4m3r on 18.01.2016.
 */
public class StartAnimation extends Activity {

   //AnimationDrawable frameAnimation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_start);

       //ImageView rocketImage = (ImageView) findViewById(R.id.animation_img);
       //rocketImage.setBackgroundResource(R.drawable.a0002);
       //animation = (AnimationDrawable) rocketImage.getBackground();


        // Generates a Handler to launch the About Screen
        // after 2 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                // Starts the About Screen Activity
                startActivity(new Intent(StartAnimation.this,
                        CameraActivity.class));
            }
        }, 4000L);
    }

   //public boolean onWindowFocusChanged() {

   //        animation.start();
   //        return true;

   //}

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        ImageView animation = (ImageView) findViewById(R.id.animation_img);
        //animation.setBackgroundResource(R.drawable.a0002);
        Drawable drawable = getResources().getDrawable(R.drawable.animation_list);
        AnimationDrawable frameAnimation = (AnimationDrawable) animation.getBackground();
       if(hasFocus) {
           frameAnimation.start();
       } else {
           frameAnimation.stop();
       }
    }
    boolean hasclicked= false;
    public void onImgClick(View v){
        if(hasclicked==true) {

            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
            finish();

        }else{
           //new CountDownTimer(2000, 1000) {
           //    public void onFinish() {
           //        // When timer is finished
           //        // Execute your code here
           //        Intent intent = new Intent(StartAnimation.this, CameraActivity.class);
           //        startActivity(intent);
           //        finish();
           //    }

           //    public void onTick(long millisUntilFinished) {
           //        // millisUntilFinished    The amount of time until finished.
           //    }
           //}.start();
        }

    }
}
