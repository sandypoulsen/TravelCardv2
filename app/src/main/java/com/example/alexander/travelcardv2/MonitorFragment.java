package com.example.alexander.travelcardv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;
import java.util.UUID;


public class MonitorFragment extends Fragment {

    private final static String iphone = "Monitored region Iphone";
    private final static String ipad = "monitored region Ipad";

    private final static String TAG = "MonitorFragment";

    private TravelRegistrationDB mRegistrationDB;
    private BeaconManager beaconManager;
    private Button checkin;
    private TextView savings;
    private Button goto_registrations_button;
    private Button cancel_last_checkin;
    private int majorindex;

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

            /*
            new Region(
                    "2. floor",
                    UUID.fromString("E3B54450-AB73-4D79-85D6-519EAF0F45D9"),
                    2, null),
            new Region(
                    "5. floor",
                    UUID.fromString("E3B54450-AB73-4D79-85D6-519EAF0F45D9"),
                    5, null)
                    */

    };

    private Region getRegion(int major) {
        for (Region region : regions) {
            if (region.getMajor() == major) {
                return region;
            }
        }
        return null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_monitor, container, false);


        mRegistrationDB = TravelRegistrationDB.get();

        checkin = (Button) v.findViewById(R.id.button_checkin);
        checkin.setEnabled(false);
        checkin.setText("You cannot check in here.");

        cancel_last_checkin = (Button) v.findViewById(R.id.cancel_last_checkin);

        if (savedInstanceState != null) {

            majorindex = savedInstanceState.getInt(checkinMajorIndex, -1);
            if (majorindex != -1 && majorindex != 0) {
                Region region = getRegion(majorindex);
                checkin.setEnabled(true);
                checkin.setText("Check in at " + region.getIdentifier());
            }
        }

        TravelRegistration checkInRegistration = mRegistrationDB.getLastTravelRegistration();

        if (checkInRegistration != null) {

            cancel_last_checkin.setEnabled(true);
            checkin.setText("Checked in at " + checkInRegistration.getIdentifier() + "\n" + "Travel id: " + checkInRegistration.getId());
            checkin.setEnabled(false);

        }


        goto_registrations_button = (Button) v.findViewById(R.id.goto_registrations_button);

        goto_registrations_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TravelListActivity.class);
                startActivity(intent);
            }
        });

        savings = (TextView) v.findViewById(R.id.textview_savings);
        savings.setText(mRegistrationDB.getSavings() + "");

        beaconManager = new BeaconManager(getActivity());


        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {

                majorindex = region.getMajor();
                checkin.setEnabled(true);
                checkin.setText("Check in at " + region.getIdentifier());
                cancel_last_checkin.setEnabled(false);
                Log.i(TAG, "entered region: " + region.getIdentifier());

                TravelRegistration checkInRegistration = mRegistrationDB.getLastTravelRegistration();

                if (checkInRegistration != null) {

                    if (checkInRegistration.getMajor() != region.getMajor()) {

                        mRegistrationDB.checkOut(region);
                        savings.setText(mRegistrationDB.getSavings() + "");

                        Toast.makeText(getActivity(), "Thank you for using TravelCard", Toast.LENGTH_LONG).show();

                    }

                }

            }

            @Override
            public void onExitedRegion(Region region) {

                Log.i(TAG, "exited region: " + region.getIdentifier());

                if (region.getMajor() == majorindex) {

                    if (mRegistrationDB.getLastTravelRegistration() == null) {
                        checkin.setEnabled(false);
                        checkin.setText("You cannot check in here.");
                    }

                    majorindex = -1;

                }

            }
        });


        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mRegistrationDB.getSavings() < 10) {
                    Toast.makeText(getActivity(), "You need to insert money on your TravelCard.", Toast.LENGTH_LONG).show();
                } else {
                    String id = UUID.randomUUID().toString();

                    Region region = getRegion(majorindex);
                    mRegistrationDB.checkIn(region, id);

                    cancel_last_checkin.setEnabled(true);
                    checkin.setText("Checked in at " + region.getIdentifier() + "\n" + "Travel id: " + id);
                    checkin.setEnabled(false);
                }


            }
        });

        if (checkInRegistration != null) {
            cancel_last_checkin.setEnabled(true);
        } else {
            cancel_last_checkin.setEnabled(false);
        }

        cancel_last_checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegistrationDB.cancelLastCheckin();
                cancel_last_checkin.setEnabled(false);


                if (majorindex != 0 && majorindex != -1) {
                    Region region = getRegion(majorindex);
                    checkin.setText("Check in at " + region.getIdentifier());
                    checkin.setEnabled(true);
                } else {
                    checkin.setText("You cannot check in here.");
                    checkin.setEnabled(false);
                }

            }
        });

        return v;


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(checkinMajorIndex, majorindex);
    }



    @Override
    public void onStart() {
        super.onStart();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                for (Region region : regions) {
                    beaconManager.startMonitoring(region);
                }
            }
        });
        beaconManager.setBackgroundScanPeriod(5000, 0);
    }

    @Override
    public void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());
        savings.setText(mRegistrationDB.getSavings() + "");
    }
}
