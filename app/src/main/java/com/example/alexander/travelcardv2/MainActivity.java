package com.example.alexander.travelcardv2;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import org.w3c.dom.Text;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private final static String iphone = "Monitored region Iphone";
    private final static String ipad = "monitored region Ipad";
    private final static String checkinMajorIndex = "checkinmajor";

    private final static Region[] regions = {

            new Region(
            ipad,
            UUID.fromString("8492e75f-4fd6-469d-b132-043fe94921d8"),
            1729, null),

            new Region(
            iphone,
            UUID.fromString("8492e75f-4fd6-469d-b132-043fe94921d8"),
            9842, null)

    };

    private Region getRegion(int major) {
        for(Region region: regions) {
            if(region.getMajor()==major) {
                return region;
            }
        }
        return null;
    }

    private BeaconManager beaconManager;
    private Button checkin;
    private TextView savings;
    private Button goto_registrations_button;
    private Button cancel_last_checkin;

    private TravelRegistrationDB mRegistrationDB;
    private int majorindex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRegistrationDB = TravelRegistrationDB.get();



        if(savedInstanceState != null) {

            majorindex = savedInstanceState.getInt(checkinMajorIndex, -1);
            if(majorindex != -1 && majorindex !=0) {
                Region region = getRegion(majorindex);
                checkin.setEnabled(true);
                checkin.setText("Check in at " + region.getIdentifier());
            }
        }

        if(mRegistrationDB.isCancellationAllowed()) {
            TravelRegistration registration = mRegistrationDB.getLastTravelRegistration();
            cancel_last_checkin.setEnabled(true);
            checkin.setText("Checked in at " + registration.getIdentifier() + "\n" + "Travel id: " + registration.getId());
            checkin.setEnabled(false);

        }


        goto_registrations_button = (Button) findViewById(R.id.goto_registrations_button);

        goto_registrations_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TravelListActivity.class);
                startActivity(intent);
            }
        });

        savings = (TextView) findViewById(R.id.textview_savings);
        savings.setText(mRegistrationDB.getSavings() + "");

        beaconManager = new BeaconManager(this);


        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {

                majorindex = region.getMajor();
                checkin.setEnabled(true);
                checkin.setText("Check in at " + region.getIdentifier());
                Log.i(TAG, "entered region: " + region.getIdentifier());


                if (mRegistrationDB.isCancellationAllowed()) {

                    if (mRegistrationDB.getLastTravelRegistration().getMajor() != region.getMajor()) {

                        mRegistrationDB.checkOut(region);

                        Toast.makeText(getApplicationContext(), "Thank you for using TravelCard", Toast.LENGTH_SHORT).show();

                    }

                }

            }

            @Override
            public void onExitedRegion(Region region) {

                if (region.getMajor() == majorindex) {

                    if(!mRegistrationDB.isCancellationAllowed()) {
                        checkin.setEnabled(false);
                        checkin.setText("No check in is allowed");
                    }

                    majorindex = -1;

                }

            }
        });


        checkin = (Button) findViewById(R.id.button_checkin);
        checkin.setEnabled(false);
        checkin.setText("No check in allowed");

        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = UUID.randomUUID().toString();

                Region region = getRegion(majorindex);
                mRegistrationDB.checkIn(region, id);

                cancel_last_checkin.setEnabled(true);
                checkin.setText("Checked in at " + region.getIdentifier() + "\n" + "Travel id: " + id);
                checkin.setEnabled(false);

            }
        });

        cancel_last_checkin = (Button) findViewById(R.id.cancel_last_checkin);

        if(mRegistrationDB.isCancellationAllowed()) {
           cancel_last_checkin.setEnabled(true);
        } else {
           cancel_last_checkin.setEnabled(false);
        }

        cancel_last_checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegistrationDB.cancelLastCheckin();
                cancel_last_checkin.setEnabled(false);


                if(majorindex != 0 && majorindex != -1) {
                    Region region = getRegion(majorindex);
                    checkin.setText("Check in at " + region.getIdentifier());
                    checkin.setEnabled(true);
                } else {
                    checkin.setText("No check in allowed");
                    checkin.setEnabled(false);
                }

            }
        });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(checkinMajorIndex, majorindex);
    }

    @Override
    protected void onStart() {
        super.onStart();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                for(Region region : regions) {
                    beaconManager.startMonitoring(region);
                }
            }
        });
        //beaconManager.setBackgroundScanPeriod(2000, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        savings.setText(mRegistrationDB.getSavings() + "");
    }


}
