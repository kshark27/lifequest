package com.levipayne.liferpg;

import android.app.Application;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

/**
 * Created by Levi on 4/29/2016.
 */
public class LifeRPGApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }

}
