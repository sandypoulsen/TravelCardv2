package com.example.alexander.travelcardv2;

import android.app.Application;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

/**
 * Created by alexander on 07-04-17.
 */

public class MyApplication extends Application {

    BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();


    }
}
