package com.example.alexander.travelcardv2;

import android.util.Log;

import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexander on 08-04-17.
 */

public class TravelRegistrationDB {

    private static TravelRegistrationDB sTravelRegistrationDB;
    private List<TravelRegistration> mTravelRegistrations;

    public static TravelRegistrationDB get() {
        if(sTravelRegistrationDB == null) {
            sTravelRegistrationDB  = new TravelRegistrationDB();
        }
        return sTravelRegistrationDB;
    }

    private TravelRegistrationDB() {
        mTravelRegistrations = new ArrayList<>();
    }

    public void addTravelRegistration(TravelRegistration registration) {
        mTravelRegistrations.add(registration);
    }

    public void removeTravelRegistration(TravelRegistration registration) {
        mTravelRegistrations.remove(registration);
    }

    public TravelRegistration getLastTravelRegistration() {
        if(mTravelRegistrations.size() > 0) {

            for(int i = (mTravelRegistrations.size() - 1) ; i >= 0; --i) {
                Log.i("DB", "index: " + i);
                TravelRegistration registration = mTravelRegistrations.get(i);
                if(registration.getType().equals("checkout")) {
                    return null;
                } else if(registration.getType().equals("checkin")) {
                    if( !registration.isCancelled()) {
                        return registration;
                    } else {
                        return null;
                    }

                }


            }


        }
        return null;

    }

    public void doPayment(int amount) {

        TravelRegistration registration = new TravelRegistration();
        registration.setIdentifier("Payment");
        registration.setType("payment");
        registration.setCreated(System.currentTimeMillis());
        registration.setAmount(amount);
        registration.setMajor(0);
        addTravelRegistration(registration);
    }

    public void cancelLastCheckin() {
        TravelRegistration registration = getLastTravelRegistration();
        if(registration.getType().equals("checkin") && !registration.isCancelled()) {
            registration.setCancelled(true);
        }
    }

    public List<TravelRegistration> getTravelRegistrations() {
        return mTravelRegistrations;
    }

    public boolean isCancellationAllowed() {
        TravelRegistration registration = getLastTravelRegistration();
        if(registration != null && registration.getType().equals("checkin") && !registration.isCancelled()) {
            return true;
        } else {
            return false;
        }
    }

    public int getSavings() {
        int result = 0;
        for(TravelRegistration registration : mTravelRegistrations) {
            result += registration.getAmount();
        }
        return result;
    }

    public void checkIn(Region region, String id) {
        TravelRegistration registration = new TravelRegistration();

        registration.setIdentifier(region.getIdentifier());
        registration.setType("checkin");
        registration.setCreated(System.currentTimeMillis());
        registration.setMajor(region.getMajor());
        registration.setAmount(0);

        registration.setId(id);

        mTravelRegistrations.add(registration);
    }

    public void checkOut(Region region) {

        TravelRegistration checkinRegistration = getLastTravelRegistration();

        TravelRegistration registration = new TravelRegistration();
        registration.setIdentifier(region.getIdentifier());
        registration.setType("checkout");
        registration.setCreated(System.currentTimeMillis());
        registration.setMajor(region.getMajor());
        registration.setAmount(-10);
        registration.setId(checkinRegistration.getId());
        mTravelRegistrations.add(registration);
    }

}
