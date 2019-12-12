package com.example.polarapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.polarapp.polar.PolarSDK;
import com.example.polarapp.preferencesmanager.DevicePreferencesManager;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.example.polarapp.ui.AboutFragment;
import com.example.polarapp.ui.DevicesFragment;
import com.example.polarapp.ui.HomeFragment;
import com.example.polarapp.ui.ProfileFragment;
import com.example.polarapp.ui.SettingsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private TextView textViewName, textViewEmail;
    private ProfilePreferencesManager profilePreferencesManager;
    private DevicePreferencesManager devicePreferencesManager;
    private PolarSDK polarSDK;
    private static final String PROFILE_USER_ID = "profile_user_id";

    // Shared preferences file name
    private static final String PROFILE_USER_NAME = "profile_user_name";
    private static final String PROFILE_USER_EMAIL = "profile_user_email";

    // Names in Database
    private static final String ACTIVITY_UUID = "UUID";
    private static final String ACTIVITY_TYPE = "type";
    private static final String ACTIVITY_TIMESTAMP = "timestamp";
    private static final String ACTIVITY_TIME = "time";
    private static final String ACTIVITY_DISTANCE = "distance";
    private static final String ACTIVITY_AVG_SPEED = "avgSpeed";
    private static final String ACTIVITY_LOCATION_POINTS = "locationPoints";
    private static final String ACTIVITY_AVG_HR = "avgHR";
    private static final String ACTIVITY_INTERVAL = "interval";
    private static final String ACTIVITY_DEEP_SLEEP_TIME = "deepSleepTime";
    private static final String ACTIVITY_NIGHT_MOVES = "nightMoves";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());
        devicePreferencesManager = new DevicePreferencesManager(getBaseContext());

        // createActivities();

        polarSDK = (PolarSDK) getApplicationContext();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);

        textViewName = hView.findViewById(R.id.textViewNameHeader);
        textViewEmail = hView.findViewById(R.id.textViewEmailHeader);

        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                textViewName.setText(String.valueOf(profilePreferencesManager.getStringProfileValue(PROFILE_USER_NAME)));
                textViewEmail.setText(String.valueOf(profilePreferencesManager.getStringProfileValue(PROFILE_USER_EMAIL)));
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Open Home fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        textViewName.setText(String.valueOf(profilePreferencesManager.getStringProfileValue(PROFILE_USER_NAME)));
        textViewEmail.setText(String.valueOf(profilePreferencesManager.getStringProfileValue(PROFILE_USER_EMAIL)));

        checkBT();
    }

    private void createActivities() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> activity1 = new HashMap<>();

 /*       DocumentReference docRef = db.collection("activities").document("2NFOpyRl6opEpzS2fZzt");

// Remove the 'capital' field from the document
        Map<String,Object> updates = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
        updates.put("avgSpeed", 0.5);

        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("MyApp", "File updated");
            }
        });*/

        activity1.put(ACTIVITY_UUID, profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
        activity1.put(ACTIVITY_TYPE, "run");
        activity1.put(ACTIVITY_TIMESTAMP, (long) 1575712800 * 1000);
        activity1.put(ACTIVITY_TIME, 290);
        activity1.put(ACTIVITY_DISTANCE, 22000);
        activity1.put(ACTIVITY_AVG_SPEED, 12.34);
        activity1.put(ACTIVITY_LOCATION_POINTS, null);
        activity1.put(ACTIVITY_AVG_HR, 149.50);
        activity1.put(ACTIVITY_INTERVAL, 1);

        db.collection("activities")
                .add(activity1)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MyApp", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "Error adding document", e);
                    }
                });

        Map<String, Object> activity2 = new HashMap<>();

        activity2.put(ACTIVITY_UUID, profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
        activity2.put(ACTIVITY_TYPE, "sleep");
        activity2.put(ACTIVITY_TIMESTAMP, (long) 1575712800 * 1000);
        activity2.put(ACTIVITY_TIME, 1500);
        activity2.put(ACTIVITY_DEEP_SLEEP_TIME, 320);
        activity2.put(ACTIVITY_NIGHT_MOVES, 56);
        activity2.put(ACTIVITY_AVG_HR, 58.50);

        db.collection("activities")
                .add(activity2)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MyApp", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "Error adding document", e);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
                break;
            case R.id.nav_device_list:
                Toast.makeText(this, "Device List", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DevicesFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_device_list);
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_profile);
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_settings);
                break;
            case R.id.nav_about:
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_about);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        }

        //requestPermissions() method needs to be called when the build SDK version is 23 or above
        if (Build.VERSION.SDK_INT >= 23) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        polarSDK.onPauseEntered();
    }

    @Override
    public void onResume() {
        super.onResume();
        polarSDK.onResumeEntered();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        polarSDK.onDestroyEntered();
    }
}
