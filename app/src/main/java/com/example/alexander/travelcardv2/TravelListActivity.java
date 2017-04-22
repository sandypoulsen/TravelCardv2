package com.example.alexander.travelcardv2;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class TravelListActivity extends AppCompatActivity {

    private FragmentManager fm;
    private TravelListFragment travelListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Registrations");

        fm = getSupportFragmentManager();

        travelListFragment = (TravelListFragment) fm.findFragmentById(R.id.fragment_container);

        if (travelListFragment == null) {
            travelListFragment = new TravelListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, travelListFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
