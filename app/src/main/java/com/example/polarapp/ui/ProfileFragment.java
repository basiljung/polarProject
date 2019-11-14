package com.example.polarapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.polarapp.R;
import com.example.polarapp.profile.EditDialog;

public class ProfileFragment extends Fragment implements EditDialog.EditListener {

    private View root;
    private ImageView editImageView;
    private LinearLayout sexLayout, ageLayout, heightLayout, weightLayout, emailLayout, phoneLayout, locationLayout;
    private TextView nameText, sexText, ageText, heightText, weightText, emailText, phoneText, locationText;
    private EditDialog editDialog;
    private int selectedButton;

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
        return root;
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