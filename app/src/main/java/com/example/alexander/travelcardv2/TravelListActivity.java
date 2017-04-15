package com.example.alexander.travelcardv2;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
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

    private RecyclerView mRegistrationRecyclerView;
    private TravelRegistrationDB mRegistrationDB;
    private TravelRegistrationAdapter mAdapter;
    private Button button_insert_money;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_list);



        mRegistrationRecyclerView = (RecyclerView) findViewById(R.id.travels_recycler_view);
        mRegistrationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRegistrationDB = TravelRegistrationDB.get();

        mAdapter = new TravelRegistrationAdapter(mRegistrationDB.getTravelRegistrations());
        mRegistrationRecyclerView.setAdapter(mAdapter);

        button_insert_money = (Button) findViewById(R.id.button_insert_money);

        button_insert_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(TravelListActivity.this).create();
                alertDialog.setTitle("Payment confirmation");
                alertDialog.setMessage("You are about to withdraw 100 kr from your bank");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mRegistrationDB.doPayment(100);
                                mAdapter.notifyDataSetChanged();

                                Toast.makeText(getApplicationContext(), "You have inserted 100 kr", Toast.LENGTH_SHORT).show();
                            }
                        });
                alertDialog.show();



            }
        });
    }

    private class TravelRegistrationHolder extends RecyclerView.ViewHolder {

        private TextView mTypeTextView;
        private TextView mDateTextView;
        private TextView mPriceTextView;
        private View mView;

        public void bindRegistration(final com.example.alexander.travelcardv2.TravelRegistration registration, final int position) {

            long timestamp = registration.getCreated();

            Date date = new Date(timestamp);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateFormatted = formatter.format(date);

            if(registration.getType().equals("checkin")) {
                mTypeTextView.setText("Checked in at: " + registration.getIdentifier());
            } else if(registration.getType().equals("checkout")) {
                mTypeTextView.setText("Checked out at: " + registration.getIdentifier());
                mView.setBackgroundColor(Color.GREEN);
            } else if(registration.getType().equals("payment")) {
                mTypeTextView.setText("Payment");
            }

            mDateTextView.setText(dateFormatted);
            mPriceTextView.setText(registration.getAmount() + "");

            if(registration.getType().equals("checkin") && registration.isCancelled()) {
                mView.setBackgroundColor(Color.RED);
            }

        }

        public TravelRegistrationHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTypeTextView = (TextView) itemView.findViewById(R.id.list_item_text_view_type);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_text_view_date);
            mPriceTextView = (TextView) itemView.findViewById(R.id.list_item_text_view_price);

        }

    }

    private class TravelRegistrationAdapter extends RealmRecyclerViewAdapter<TravelRegistration, TravelRegistrationHolder> {
        private OrderedRealmCollection<TravelRegistration> mRegistrationList;

        public TravelRegistrationAdapter(OrderedRealmCollection<TravelRegistration> registrationList) {
            super(TravelListActivity.this, registrationList, true);
            mRegistrationList = registrationList;
        }

        @Override
        public TravelRegistrationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(TravelListActivity.this);
            View view = layoutInflater.inflate(R.layout.registration_list_item, parent, false);
            return new TravelRegistrationHolder(view);
        }

        @Override
        public void onBindViewHolder(TravelRegistrationHolder holder, int position) {
            TravelRegistration registration = mRegistrationList.get(position);
            holder.bindRegistration(registration, position);
        }

        @Override
        public int getItemCount() {
            return mRegistrationList.size();
        }
    }
}
