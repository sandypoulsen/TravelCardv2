package com.example.alexander.travelcardv2;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by alexander on 07-04-17.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);


    }
}
