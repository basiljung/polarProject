package com.example.polarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.example.polarapp.preferencesmanager.IntroPreferencesManager;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.*;

public class IntroActivity extends AppCompatActivity {

    private IntroPreferencesManager introPreferencesManager;
    private Button saveButton;
    private EditText userName, userEmail, userCity, userPhone, userBirthDate;
    private CountryCodePicker ccpCountry, ccpPhone;
    private RadioGroup sexGroup;
    private NumberPicker pickerHeight, pickerWeight;
    private TextInputLayout nameLayout, emailLayout, cityLayout, phoneLayout, birthDateLayout;
    private RadioButton radioButtonError;
    private ProfilePreferencesManager profilePreferencesManager;

    // Shared preferences file name
    private static final String PROFILE_USER_NAME = "profile_user_name";
    private static final String PROFILE_USER_EMAIL = "profile_user_email";
    private static final String PROFILE_USER_PHONE = "profile_user_phone";
    private static final String PROFILE_USER_CITY = "profile_user_city";
    private static final String PROFILE_USER_COUNTRY = "profile_user_country";
    private static final String PROFILE_USER_BIRTH = "profile_user_birth";
    private static final String PROFILE_USER_SEX = "profile_user_sex";
    private static final String PROFILE_USER_HEIGHT = "profile_user_height";
    private static final String PROFILE_USER_WEIGHT = "profile_user_weight";
    private static final String PROFILE_USER_ID = "profile_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPreferencesManager = new IntroPreferencesManager(getBaseContext());
        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());

        if (!introPreferencesManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        setContentView(R.layout.activity_intro);

        nameLayout = findViewById(R.id.userNameLayout);
        emailLayout = findViewById(R.id.userEmailLayout);
        cityLayout = findViewById(R.id.userCityLayout);
        phoneLayout = findViewById(R.id.userPhoneLayout);
        birthDateLayout = findViewById(R.id.userBirthDateLayout);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userBirthDate = findViewById(R.id.userBirthDate);
        userBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

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
        radioButtonError = findViewById(R.id.userSexOption2);

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
                doChecks();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(IntroActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String month;
                        String day;
                        if ((monthOfYear + 1) < 10) {
                            month = "0" + (monthOfYear + 1);
                        } else {
                            month = String.valueOf(monthOfYear + 1);
                        }
                        if (dayOfMonth < 10) {
                            day = "0" + dayOfMonth;
                        } else {
                            day = String.valueOf(dayOfMonth);
                        }
                        userBirthDate.setText(day + "/" + month + "/" + year);
                    }
                }, 2001, 0, 1); // Select BirthDate on DB

        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SAVE", datePickerDialog);
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "CANCEL", datePickerDialog);
        datePickerDialog.show();
    }

    private void launchHomeScreen() {
        introPreferencesManager.setFirstTimeLaunch(false);
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void doChecks() {
        if (isValidName() && isValidEmail() && isValidCity() && isValidPhone() && isValidSex() && isValidBirthDate()) {
            Toast.makeText(getApplicationContext(), "Good data", Toast.LENGTH_SHORT).show();
            saveUserData();
            launchHomeScreen();
        } else {
            Toast.makeText(getApplicationContext(), "Error with data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData() {
        Map<String, Object> userData = new HashMap<>();

        String uniqueID = UUID.randomUUID().toString();
        userData.put("UserID", uniqueID);
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_ID, uniqueID);
        //sharedPreferences.edit().putString("UUID", uniqueID).apply();
        userData.put("Name", userName.getText().toString().trim());
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_NAME, userName.getText().toString().trim());
        userData.put("Email", userEmail.getText().toString().trim());
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_EMAIL, userEmail.getText().toString().trim());
        userData.put("Country", ccpCountry.getSelectedCountryName().trim());
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_COUNTRY, ccpCountry.getSelectedCountryName().trim());
        userData.put("City", userCity.getText().toString().trim());
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_CITY, userCity.getText().toString().trim());
        userData.put("Phone", ccpPhone.getFormattedFullNumber().trim());
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_PHONE, ccpPhone.getFormattedFullNumber().trim());

        switch (sexGroup.getCheckedRadioButtonId()) {
            case R.id.userSexOption1:
                userData.put("Sex", "Male");
                profilePreferencesManager.setStringProfileValue(PROFILE_USER_SEX, "Male");
                break;
            case R.id.userSexOption2:
                userData.put("Sex", "Female");
                profilePreferencesManager.setStringProfileValue(PROFILE_USER_SEX, "Female");
                break;
        }

        userData.put("Height", pickerHeight.getValue());
        profilePreferencesManager.setIntProfileValue(PROFILE_USER_HEIGHT, pickerHeight.getValue());
        userData.put("Weight", pickerWeight.getValue());
        profilePreferencesManager.setIntProfileValue(PROFILE_USER_WEIGHT, pickerWeight.getValue());
        userData.put("BirthDate", userBirthDate.getText().toString().trim());
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_BIRTH, userBirthDate.getText().toString().trim());

    }

    private boolean isValidEmailPattern(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isValidName() {
        if (userName.getText().toString().isEmpty()) {
            nameLayout.setError("Empty name");
            return false;
        } else {
            nameLayout.setError(null);
            return true;
        }
    }

    private boolean isValidEmail() {
        if (userEmail.getText().toString().isEmpty()) {
            emailLayout.setError("Empty email");
            return false;
        } else if (!isValidEmailPattern(userEmail.getText().toString())) {
            emailLayout.setError("Email not valid");
            return false;
        } else {
            emailLayout.setError(null);
            return true;
        }
    }

    private boolean isValidPhone() {
        if (userPhone.getText().toString().isEmpty()) {
            phoneLayout.setError("Empty phone number");
            return false;
        } else {
            phoneLayout.setError(null);
            return true;
        }
    }

    @SuppressLint("ResourceType")
    private boolean isValidSex() {
        if (sexGroup.getCheckedRadioButtonId() <= 0) {
            radioButtonError.setError("Select Item");
            return false;
        } else {
            radioButtonError.setError(null);
            return true;
        }
    }

    private boolean isValidBirthDate() {
        if (userBirthDate.getText().toString().isEmpty()) {
            birthDateLayout.setError("Insert birth date");
            return false;
        } else {
            birthDateLayout.setError(null);
            return true;
        }
    }

    private boolean isValidCity() {
        if (userCity.getText().toString().isEmpty()) {
            cityLayout.setError("Empty city");
            return false;
        } else {
            cityLayout.setError(null);
            return true;
        }
    }
}
