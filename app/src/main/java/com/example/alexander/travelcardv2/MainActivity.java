package com.example.alexander.travelcardv2;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

public class MainActivity extends AppCompatActivity implements SyncUser.Callback {

    private final static String TAG = "MainActivity";

    private final static String DBNAME = "travels5"; // last travels2, travels4
    private final static String HOST = "130.226.142.162";
    private final static String USERNAME = "napo@itu.dk";
    private final static String PASSWORD = "mmad#2napo";
    private final static String AUTH_URL = "http://" + HOST + ":9080/auth";
    private final static String REALM_URL = "realm://" + HOST + ":9080/~/" + DBNAME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpRealmSync();
    }

    private FragmentManager fm;
    private MonitorFragment monitorFragment;

    private void setUpUI() {

        fm = getSupportFragmentManager();

        monitorFragment = (MonitorFragment) fm.findFragmentById(R.id.fragment_container);

        if (monitorFragment == null) {
            monitorFragment = new MonitorFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, monitorFragment)
                    .commit();
        }
    }


    @Override
    public void onSuccess(SyncUser user) {
        setupSync(user);
    }

    @Override
    public void onError(ObjectServerError error) {
        setUpUI();
        Toast.makeText(this, "Failed to login - Using local database only", Toast.LENGTH_LONG).show();

    }

    private void setUpRealmSync() {
        if (SyncUser.currentUser() == null) {
            SyncCredentials myCredentials = SyncCredentials.usernamePassword(USERNAME, PASSWORD, false);
            SyncUser.loginAsync(myCredentials, AUTH_URL, this);
        } else {
            setupSync(SyncUser.currentUser());
        }
    }

    private void setupSync(SyncUser user) {
        SyncConfiguration defaultConfig = new SyncConfiguration.Builder(user, REALM_URL).build();
        Realm.setDefaultConfiguration(defaultConfig);
        setUpUI();
        Toast.makeText(this, "Logged in", Toast.LENGTH_LONG).show();
    }
}
