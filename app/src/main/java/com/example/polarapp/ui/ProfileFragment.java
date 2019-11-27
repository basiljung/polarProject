package com.example.polarapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.example.polarapp.R;
import com.example.polarapp.profile.EditDialog;

import java.time.*;
import java.util.*;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment implements EditDialog.EditListener {

    private View root;
    private ImageView editImageView;
    private LinearLayout sexLayout, ageLayout, heightLayout, weightLayout, emailLayout, phoneLayout, locationLayout;
    private TextView nameText, sexText, ageText, heightText, weightText, emailText, phoneText, locationText;
    private EditDialog editDialog;
    private int selectedButton;
    private ProfilePreferencesManager profilePreferencesManager;

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        nameText = root.findViewById(R.id.profileName);
        sexText = root.findViewById(R.id.sexValue);
        ageText = root.findViewById(R.id.ageValue);
        heightText = root.findViewById(R.id.heightValue);
        weightText = root.findViewById(R.id.weightValue);
        emailText = root.findViewById(R.id.emailValue);
        phoneText = root.findViewById(R.id.phoneValue);
        locationText = root.findViewById(R.id.locationValue);

        profilePreferencesManager = new ProfilePreferencesManager(getActivity().getBaseContext());

        editImageView = root.findViewById(R.id.imageViewEdit);
        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), "", 10);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit profile");
                }
            }
        });

        sexLayout = root.findViewById(R.id.sexLayout);
        sexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sexValue = profilePreferencesManager.getStringProfileValue(PROFILE_USER_SEX);
                if (sexValue.equals("Male")) {
                    selectedButton = R.id.sexOption1;
                } else if (sexValue.equals("Female")) {
                    selectedButton = R.id.sexOption2;
                }
                editDialog = new EditDialog(ProfileFragment.this, getContext(), String.valueOf(selectedButton), 1);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit sex");
                }
            }
        });

        ageLayout = root.findViewById(R.id.ageLayout);
        ageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), profilePreferencesManager.getStringProfileValue(PROFILE_USER_BIRTH), 2);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit age");
                }
            }
        });

        heightLayout = root.findViewById(R.id.heightLayout);
        heightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), String.valueOf(profilePreferencesManager.getIntProfileValue(PROFILE_USER_HEIGHT)), 3);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit height");
                }
            }
        });

        weightLayout = root.findViewById(R.id.weightLayout);
        weightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), String.valueOf(profilePreferencesManager.getIntProfileValue(PROFILE_USER_WEIGHT)), 4);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit weight");
                }
            }
        });

        emailLayout = root.findViewById(R.id.emailLayout);
        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), "", 5);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit email");
                }
            }
        });

        phoneLayout = root.findViewById(R.id.phoneLayout);
        phoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), "", 6);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit phone");
                }
            }
        });

        locationLayout = root.findViewById(R.id.locationLayout);
        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), locationText.getText().toString(), 7);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "Edit location");
                }
            }
        });

        //loadProfileData();

        nameText.setText(profilePreferencesManager.getStringProfileValue(PROFILE_USER_NAME));
        emailText.setText(profilePreferencesManager.getStringProfileValue(PROFILE_USER_EMAIL));
        phoneText.setText(profilePreferencesManager.getStringProfileValue(PROFILE_USER_PHONE));
        locationText.setText(profilePreferencesManager.getStringProfileValue(PROFILE_USER_CITY) + "/" +
                profilePreferencesManager.getStringProfileValue(PROFILE_USER_COUNTRY));
        sexText.setText(profilePreferencesManager.getStringProfileValue(PROFILE_USER_SEX));
        heightText.setText(profilePreferencesManager.getIntProfileValue(PROFILE_USER_HEIGHT) + "cm");
        weightText.setText(profilePreferencesManager.getIntProfileValue(PROFILE_USER_WEIGHT) + "kg");
        String[] date = profilePreferencesManager.getStringProfileValue(PROFILE_USER_BIRTH).split("/");
        ageText.setText(String.valueOf(getAge(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]))));

        Log.d("1234", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));

        return root;
    }

    /*private void loadProfileData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String id = sp.getString(USER_ID, "Error");
        Log.d("MyApp", id);
        docRef = db.collection("profile").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("MyApp", "DocumentSnapshot " + document.getId() + " data: " + document.getData());
                        userData = document.getData();
                        //printUserData(userData);
                    } else {
                        Log.d("MyApp", "No such document");
                    }
                } else {
                    Log.d("MyApp", "get failed with ", task.getException());
                }
            }
        });
    }*/

    /*public void printUserData(Map<String, Object> userData) {
        Object userName = userData.get("Name");
        Object userEmail = userData.get("Email");
        Object userPhone = userData.get("Phone");
        Object userCity = userData.get("City");
        Object userCountry = userData.get("Country");
        Object userBirthDate = userData.get("BirthDate");
        Object userSex = userData.get("Sex");
        Object userWeight = userData.get("Weight");
        Object userHeight = userData.get("Height");
        nameText.setText(String.valueOf(userName));
        emailText.setText(String.valueOf(userEmail));
        phoneText.setText(String.valueOf(userPhone));
        locationText.setText(userCity + "/" + userCountry);
        sexText.setText(String.valueOf(userSex));
        weightText.setText(userWeight + "kg");
        heightText.setText(userHeight + "cm");
        String[] date = String.valueOf(userBirthDate).split("/");
        ageText.setText(String.valueOf(getAge(Integer.parseInt(date[2]),Integer.parseInt(date[1]),Integer.parseInt(date[0]))));
    }*/

    public int getAge(int year, int monthOfYear, int dayOfMonth) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            LocalDate birthDate = LocalDate.of(year, monthOfYear, dayOfMonth);
            Period p = Period.between(birthDate, today);
            return p.getYears();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            Calendar birthDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            Calendar currentDate = new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

            long end = birthDate.getTimeInMillis();
            long start = currentDate.getTimeInMillis();

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(TimeUnit.MILLISECONDS.toMillis(Math.abs(end - start)));
            return (c.get(Calendar.YEAR) - 1970);
        }
    }

    @Override
    public void applySexChanges(int sex) {
        switch (sex) {
            case R.id.sexOption1:
                sexText.setText("Male");
                profilePreferencesManager.setStringProfileValue(PROFILE_USER_SEX, "Male");
                break;
            case R.id.sexOption2:
                profilePreferencesManager.setStringProfileValue(PROFILE_USER_SEX, "Female");
                sexText.setText("Female");
                break;
        }
    }

    @Override
    public void applyBirthChanges(String birthday) {
        String[] date = birthday.split("/");
        ageText.setText(String.valueOf(getAge(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]))));
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_BIRTH, birthday);
    }

    @Override
    public void applyHeightChanges(int height) {
        heightText.setText(height + "cm");
        profilePreferencesManager.setIntProfileValue(PROFILE_USER_HEIGHT, height);
    }

    @Override
    public void applyWeightChanges(int weight) {
        weightText.setText(weight + "kg");
        profilePreferencesManager.setIntProfileValue(PROFILE_USER_WEIGHT, weight);
    }

    @Override
    public void applyEmailChanges(String email) {
        emailText.setText(email);
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_EMAIL, email);
    }

    @Override
    public void applyPhoneChanges(String phone) {
        phoneText.setText(phone);
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_PHONE, phone);
    }

    @Override
    public void applyLocationChanges(String location) {
        String[] data = location.split("/");
        locationText.setText(location);
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_CITY, data[0]);
        profilePreferencesManager.setStringProfileValue(PROFILE_USER_COUNTRY, data[1]);
    }

    @Override
    public void applyProfileChanges(String name, String password) {
        if (!name.equals("")) {
            nameText.setText(name);
            profilePreferencesManager.setStringProfileValue(PROFILE_USER_NAME, name);
        }
    }
}