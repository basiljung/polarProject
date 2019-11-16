package com.example.polarapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.polarapp.IntroActivity;
import com.example.polarapp.R;
import com.example.polarapp.profile.EditDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment implements EditDialog.EditListener {

    private View root;
    private ImageView editImageView;
    private LinearLayout sexLayout, ageLayout, heightLayout, weightLayout, emailLayout, phoneLayout, locationLayout;
    private TextView nameText, sexText, ageText, heightText, weightText, emailText, phoneText, locationText;
    private EditDialog editDialog;
    private int selectedButton;
    private static final String USER_ID = "id";
    private Map<String, Object> userData = new HashMap<>();
    private SharedPreferences sp;
    private DocumentReference docRef;

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

        editImageView = root.findViewById(R.id.imageViewEdit);
        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), "", 10);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });

        sexLayout = root.findViewById(R.id.sexLayout);
        sexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sexValue = sexText.getText().toString();
                if (sexValue.equals("Male")) {
                    selectedButton = R.id.sexOption1;
                } else if (sexValue.equals("Female")) {
                    selectedButton = R.id.sexOption2;
                }
                editDialog = new EditDialog(ProfileFragment.this, getContext(), String.valueOf(selectedButton), 1);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });

        ageLayout = root.findViewById(R.id.ageLayout);
        ageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), "", 2);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });

        heightLayout = root.findViewById(R.id.heightLayout);
        heightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = heightText.getText().toString();
                value = value.substring(0, value.length() - 2);
                editDialog = new EditDialog(ProfileFragment.this, getContext(), value, 3);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });

        weightLayout = root.findViewById(R.id.weightLayout);
        weightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = weightText.getText().toString();
                value = value.substring(0, value.length() - 2);
                editDialog = new EditDialog(ProfileFragment.this, getContext(), value, 4);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });

        emailLayout = root.findViewById(R.id.emailLayout);
        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), "", 5);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });

        phoneLayout = root.findViewById(R.id.phoneLayout);
        phoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), "", 6);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });

        locationLayout = root.findViewById(R.id.locationLayout);
        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new EditDialog(ProfileFragment.this, getContext(), locationText.getText().toString(), 7);
                if (getFragmentManager() != null) {
                    editDialog.show(getFragmentManager(), "");
                }
            }
        });
        
        loadProfileData();
        
        
        return root;
    }

    private void loadProfileData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        sp = getContext().getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
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
                        printUserData(userData);
                    } else {
                        Log.d("MyApp", "No such document");
                    }
                } else {
                    Log.d("MyApp", "get failed with ", task.getException());
                }
            }
        });
    }

    public void printUserData(Map<String, Object> userData) {
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
    }

    public int getAge(int year, int monthOfYear, int dayOfMonth) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            LocalDate birthDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
            Period p = Period.between(birthDate, today);
            return p.getYears();
            //editListener.applyAgeChanges(p.getYears());
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            Calendar birthDate = new GregorianCalendar(year, monthOfYear + 1, dayOfMonth);
            Calendar currentDate = new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

            long end = birthDate.getTimeInMillis();
            long start = currentDate.getTimeInMillis();

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(TimeUnit.MILLISECONDS.toMillis(Math.abs(end - start)));
            return (c.get(Calendar.YEAR) - 1970);
            //editListener.applyAgeChanges(c.get(Calendar.YEAR) - 1970);
        }
    }

    @Override
    public void applySexChanges(int sex) {
        switch (sex) {
            case R.id.sexOption1:
                Toast.makeText(getContext(), "Male", Toast.LENGTH_SHORT).show();
                sexText.setText("Male");
                break;
            case R.id.sexOption2:
                Toast.makeText(getContext(), "Female", Toast.LENGTH_SHORT).show();
                sexText.setText("Female");
                break;
        }
    }

    @Override
    public void applyAgeChanges(int age) {
        ageText.setText(String.valueOf(age));
    }

    @Override
    public void applyHeightChanges(int height) {
        heightText.setText(height + "cm");
    }

    @Override
    public void applyWeightChanges(int weight) {
        weightText.setText(weight + "kg");
    }

    @Override
    public void applyEmailChanges(String email) {
        emailText.setText(email);
    }

    @Override
    public void applyPhoneChanges(String phone) {
        phoneText.setText(phone);
    }

    @Override
    public void applyLocationChanges(String location) {
        locationText.setText(location);
    }

    @Override
    public void applyProfileChanges(String name, String password) {
        if(!name.equals("")) {
            nameText.setText(name);
        }
    }
}