package com.example.alexander.travelcardv2;

import android.util.Log;

import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexander on 08-04-17.
 */

public class TravelRegistrationDB {

    private static TravelRegistrationDB sTravelRegistrationDB;
    private static Realm realm;

    public static TravelRegistrationDB get() {
        if(sTravelRegistrationDB == null) {
            realm = realm.getDefaultInstance();
            sTravelRegistrationDB  = new TravelRegistrationDB();
        }
        return sTravelRegistrationDB;
    }

    private TravelRegistrationDB() {
        //mTravelRegistrations = new ArrayList<>();
    }

    public void addTravelRegistration(TravelRegistration registration) {
        final TravelRegistration fregistration = registration;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(fregistration);
            }
        });
        //mTravelRegistrations.add(registration);
    }

    public void removeTravelRegistration(TravelRegistration registration) {
        final TravelRegistration fregistration = registration;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<TravelRegistration> rows = realm.where(TravelRegistration.class).equalTo("id", fregistration.getId()).findAll();
                if(rows.size() > 0) rows.get(0).deleteFromRealm();
            }
        });
        //mTravelRegistrations.remove(registration);
    }


    public TravelRegistration getLastTravelRegistration() {

        RealmResults<TravelRegistration> registrations = realm.where(TravelRegistration.class).findAll();

        ListIterator<TravelRegistration> iterator = registrations.listIterator(registrations.size());

        while(iterator.hasPrevious()) {
            TravelRegistration registration = iterator.previous();
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

        realm.beginTransaction();
        registration.setCancelled(true);
        realm.commitTransaction();
    }


    public OrderedRealmCollection<TravelRegistration> getTravelRegistrations() {
        return realm.where(TravelRegistration.class).findAll();
    }

    public boolean isCancellationAllowed() {
        TravelRegistration registration = getLastTravelRegistration();
        if(registration != null) {
            return true;
        } else {
            return false;
        }
    }

    public int getSavings() {

        RealmResults<TravelRegistration> registrations = realm.where(TravelRegistration.class).findAll();
        Iterator<TravelRegistration> iterator = registrations.iterator();
        int result = 0;

        while(iterator.hasNext()) {
            TravelRegistration registration = iterator.next();
            result += registration.getAmount();
        }

        return result;
    }

    public void checkIn(Region region, String id) {
        TravelRegistration registration = new TravelRegistration(id);

        registration.setIdentifier(region.getIdentifier());
        registration.setType("checkin");
        registration.setCreated(System.currentTimeMillis());
        registration.setMajor(region.getMajor());
        registration.setAmount(0);

        addTravelRegistration(registration);
    }

    public void checkOut(Region region) {

        TravelRegistration checkinRegistration = getLastTravelRegistration();

        TravelRegistration registration = new TravelRegistration();
        registration.setIdentifier(region.getIdentifier());
        registration.setType("checkout");
        registration.setCreated(System.currentTimeMillis());
        registration.setMajor(region.getMajor());
        registration.setAmount(-10);
        addTravelRegistration(registration);
    }

}
