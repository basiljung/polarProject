package com.example.polarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IntroActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;
    private Button saveButton;
    private EditText userName, userEmail, userCity, userPhone, userBirthDate;
    private CountryCodePicker ccpCountry, ccpPhone;
    private RadioGroup sexGroup;
    private NumberPicker pickerHeight, pickerWeight;
    private TextInputLayout nameLayout, emailLayout, cityLayout, phoneLayout, birthDateLayout;
    private RadioButton radioButtonError;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String PROFILE_USER_ID = "profile_user_id";
    private static final String USER_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager = new PreferencesManager(getApplicationContext());
        if (!preferencesManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        setContentView(R.layout.activity_intro);

        db = FirebaseFirestore.getInstance();

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
                    bridge();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(IntroActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String month = "";
                        String day = "";
                        if((monthOfYear+1) < 10) {
                            month = "0" + (monthOfYear+1);
                        } else {
                            month = String.valueOf(monthOfYear+1);
                        }
                        if(dayOfMonth < 10) {
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
        preferencesManager.setFirstTimeLaunch(false);
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void bridge() {
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

        userData.put("Name", userName.getText().toString());
        userData.put("Email", userEmail.getText().toString());
        userData.put("Country", ccpCountry.getSelectedCountryName());
        userData.put("City", userCity.getText().toString());
        userData.put("Phone", ccpPhone.getFormattedFullNumber());
        switch(sexGroup.getCheckedRadioButtonId()) {
            case R.id.userSexOption1:
                userData.put("Sex", "Male");
                break;
            case R.id.userSexOption2:
                userData.put("Sex", "Female");
                break;
        }
        userData.put("Height", pickerHeight.getValue());
        userData.put("Weight", pickerWeight.getValue());
        userData.put("BirthDate", userBirthDate.getText().toString());

        db.collection("profile")
                .add(userData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        sp = getApplicationContext().getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
                        sp.edit().putString(USER_ID, documentReference.getId()).commit();

                        Log.d("MyApp", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "Error adding document", e);
                    }
                });
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
        } else if(!isValidEmailPattern(userEmail.getText().toString())) {
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
