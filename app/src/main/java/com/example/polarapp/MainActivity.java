package com.example.polarapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.*;

import com.example.polarapp.activity.ActivityData;
import com.example.polarapp.polar.PolarSDK;
import com.example.polarapp.preferencesmanager.DevicePreferencesManager;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.example.polarapp.ui.*;

import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //-
        //String uniqueID = UUID.randomUUID().toString();
        //Log.d("1234", uniqueID);
        //-

        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());
        devicePreferencesManager = new DevicePreferencesManager(getBaseContext());

        //createActivities();

        polarSDK = (PolarSDK) getApplicationContext();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);

        textViewName = hView.findViewById(R.id.textViewNameHeader);
        textViewEmail = hView.findViewById(R.id.textViewEmailHeader);

        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Open Home fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        String name = String.valueOf(profilePreferencesManager.getStringProfileValue(PROFILE_USER_NAME));
        textViewName.setText(name);
        String email = String.valueOf(profilePreferencesManager.getStringProfileValue(PROFILE_USER_EMAIL));
        textViewEmail.setText(email);

        checkBT();
    }

    private void createActivities() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> activity = new HashMap<>();

        activity.put("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
        activity.put("type", "sleep");
        activity.put("timestamp", System.currentTimeMillis());
        activity.put("time", 560);
        activity.put("nightMoves", 100);
        activity.put("deepSleepTime", 130);

        db.collection("activities")
                .add(activity)
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
