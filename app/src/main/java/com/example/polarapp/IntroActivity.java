package com.example.polarapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

public class IntroActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;
    private Button saveButton;
    private EditText userName, userEmail, userCity, userPhone;
    private CountryCodePicker ccpCountry, ccpPhone;
    RadioGroup sexGroup;
    NumberPicker pickerHeight, pickerWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager = new PreferencesManager(this);
        if (!preferencesManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        setContentView(R.layout.activity_intro);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);

        ccpCountry = findViewById(R.id.userCountry);
        ccpCountry.detectSIMCountry(true);
        ccpCountry.setCountryPreference("ES,FI,CH");

        userCity = findViewById(R.id.userCity);

        userPhone = findViewById(R.id.userPhone);
        ccpPhone = findViewById(R.id.ccpUserPhone);
        ccpPhone.registerCarrierNumberEditText(userPhone);
        ccpPhone.setCountryPreference("ES,FI,CH");
        ccpPhone.detectSIMCountry(true);

        sexGroup = findViewById(R.id.userSexGroup);

        pickerHeight = findViewById(R.id.userHeight);
        pickerHeight.setMinValue(100);
        pickerHeight.setMaxValue(250);
        pickerHeight.setValue(175);

        pickerWeight = findViewById(R.id.userWeight);
        pickerWeight.setMinValue(30);
        pickerWeight.setMaxValue(150);
        pickerWeight.setValue(90);

        saveButton = findViewById(R.id.saveUserProfileData);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    launchHomeScreen();
            }
        });
    }

    private void launchHomeScreen() {
        preferencesManager.setFirstTimeLaunch(false);
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }
}
