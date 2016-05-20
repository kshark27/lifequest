package com.levipayne.liferpg;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.batch.android.Batch;

/**
 * Created by Levi on 5/17/2016.
 */
public class BatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Batch.onStart(this);
    }

    @Override
    protected void onStop()
    {
        Batch.onStop(this);

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Batch.onDestroy(this);

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Batch.onNewIntent(this, intent);

        super.onNewIntent(intent);
    }
}
