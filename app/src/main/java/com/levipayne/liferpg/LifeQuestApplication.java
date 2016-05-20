package com.levipayne.liferpg;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.batch.android.Batch;
import com.batch.android.Config;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

/**
 * Created by Levi on 4/29/2016.
 */
public class LifeQuestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        Batch.Push.setGCMSenderId(getResources().getString(R.string.batch_sender_id));
        Batch.setConfig(new Config(getResources().getString(R.string.batch_dev_key)));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
