package com.example.polarapp.profile;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.DialogFragment;
import com.example.polarapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EditDialog extends DialogFragment {
    RadioGroup sexGroup;
    EditListener editListener;
    Context context;
    String defaultValue;
    int action;
    View view;
    AlertDialog dialog;
    DatePickerDialog picker;

    public EditDialog(EditListener cb, Context context, String defaultValue, int action) {
        this.editListener = cb;
        this.context = context;
        this.defaultValue = defaultValue;
        this.action = action;
    }

    public interface EditListener {
        void applySexChanges(int sex);
        void applyAgeChanges(int age);
        void applyHeightChanges(int height);
        void applyWeightChanges(int weight);
        void applyEmailChanges(String email);
        void applyPhoneChanges(String phone);
        void applyLocationChanges(String location);
        void applyProfileChanges(String name, String password);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        switch(action) {
            case 1:
                dialog = editSex();
                break;
            case 2:
                picker = editAge();
                return picker;
            case 3:
                dialog =  numberPickerDialog("Select your height", 100, 250, Integer.parseInt(defaultValue));
                break;
            case 4:
                dialog =  numberPickerDialog("Select your weight", 30, 150, Integer.parseInt(defaultValue));;
                break;
            case 5:
                dialog = editEmail();
                break;
            case 6:
                dialog = editPhone();
                break;
            case 7:
                dialog = editLocation();
                break;
            case 10:
                dialog = editProfile();
                break;
        }
        return dialog;
    }

    public AlertDialog editSex() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_sex, null);
        sexGroup = view.findViewById(R.id.groupSex);
        sexGroup.check(Integer.parseInt(defaultValue));

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editListener.applySexChanges(sexGroup.getCheckedRadioButtonId());
            }
        });

        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    public AlertDialog numberPickerDialog(final String title, int minValue, int maxValue, int defaultValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_physical_data, null);

        final NumberPicker numberPicker = view.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setValue(defaultValue);

        TextView textView = view.findViewById(R.id.titlePhysicalData);
        textView.setText(title);

        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (title.toLowerCase().contains("height")) {
                           editListener.applyHeightChanges(numberPicker.getValue());
                        } else if (title.toLowerCase().contains("weight")) {
                            editListener.applyWeightChanges(numberPicker.getValue());
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    public DatePickerDialog editAge() {
        final Calendar newCalendar = Calendar.getInstance();
        int day = newCalendar.get(Calendar.DAY_OF_MONTH);
        int month = newCalendar.get(Calendar.MONTH);
        int year = newCalendar.get(Calendar.YEAR);

        picker = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        getAge(year, monthOfYear, dayOfMonth);
                    }
                }, year, month, day);

        picker.updateDate(1998, 5, 24); // Select BirthDate on DB

        return picker;
    }

    public void getAge(int year, int monthOfYear, int dayOfMonth) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            LocalDate birthDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
            Period p = Period.between(birthDate, today);
            editListener.applyAgeChanges(p.getYears());
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            Calendar birthDate = new GregorianCalendar(year, monthOfYear+1, dayOfMonth);
            Calendar currentDate =  new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

            long end = birthDate.getTimeInMillis();
            long start = currentDate.getTimeInMillis();

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(TimeUnit.MILLISECONDS.toMillis(Math.abs(end - start)));
            editListener.applyAgeChanges(c.get(Calendar.YEAR)-1970);
        }
    }

    public AlertDialog editEmail() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_email, null);
        final EditText email = view.findViewById(R.id.emailValue);

        final TextInputLayout layoutEmail = view.findViewById(R.id.inputLayout);
        layoutEmail.setErrorTextColor(ColorStateList.valueOf(getResources().getColor(R.color.endblue)));

        builder.setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layoutEmail.setError(null);
                        if (isValidEmail(email.getText().toString())) {
                            Toast.makeText(getContext(), "Valid email", Toast.LENGTH_SHORT).show();
                            editListener.applyEmailChanges(email.getText().toString());
                            dialog.dismiss();
                        } else {
                            layoutEmail.setError("Invalid email");
                            Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public AlertDialog editPhone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_phone, null);
        final EditText editText = view.findViewById(R.id.editPhoneValue);
        final CountryCodePicker ccp = view.findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(editText);
        ccp.setCountryPreference("ES,FI,CH");
        ccp.detectSIMCountry(true);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editListener.applyPhoneChanges(ccp.getFormattedFullNumber());
            }
        });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    public AlertDialog editLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_location, null);
        final EditText editText = view.findViewById(R.id.editCityValue);
        final CountryCodePicker ccp = view.findViewById(R.id.ccp);
        ccp.detectSIMCountry(true);
        ccp.setCountryPreference("ES,FI,CH");
        String[] data = defaultValue.split("/");
        String city = data[0];
        editText.setText(city);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editListener.applyLocationChanges(editText.getText().toString() + "/" +
                        ccp.getSelectedCountryName());
            }
        });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    public AlertDialog editProfile() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_profile, null);
        final TextInputLayout layoutOldPass = view.findViewById(R.id.inputLayoutOldPass);
        final TextInputLayout layoutNewPass1 = view.findViewById(R.id.inputLayoutNewPass1);
        final TextInputLayout layoutNewPass2 = view.findViewById(R.id.inputLayoutNewPass2);
        layoutOldPass.setErrorTextColor(ColorStateList.valueOf(getResources().getColor(R.color.endblue)));
        layoutNewPass1.setErrorTextColor(ColorStateList.valueOf(getResources().getColor(R.color.endblue)));
        layoutNewPass2.setErrorTextColor(ColorStateList.valueOf(getResources().getColor(R.color.endblue)));
        final TextView nameValue = view.findViewById(R.id.nameValue);
        final TextView oldPass = view.findViewById(R.id.oldPassValue);
        final TextView newPass1 = view.findViewById(R.id.newPassValue1);
        final TextView newPass2 = view.findViewById(R.id.newPassValue2);

        builder.setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layoutOldPass.setError(null);
                        layoutNewPass1.setError(null);
                        layoutNewPass2.setError(null);
                        String oldPassValue = oldPass.getText().toString();
                        String newPassValue1 = newPass1.getText().toString();
                        String newPassValue2 = newPass2.getText().toString();

                        if(!(oldPassValue.equals("") && newPassValue1.equals("") && newPassValue2.equals(""))) {
                            if (!oldPassValue.equals("oldpass")) {
                                layoutOldPass.setError("Password doesn't match!");
                            } else if (newPassValue1.length() < 8 || newPassValue2.length() < 8) {
                                Log.d("MyApp", "Password pequeña");
                                layoutNewPass1.setError("Password must have 8 characters or more");
                                layoutNewPass2.setError("Password must have 8 characters or more");
                            } else if (!newPassValue1.equals(newPassValue2)) {
                                Log.d("MyApp", "Password no coincide");
                                layoutNewPass1.setError("Passwords don't match!");
                                layoutNewPass2.setError("Passwords don't match!");
                            } else {
                                Log.d("MyApp", "1: " + newPassValue1 + ", 2: " + newPassValue2 + ", equals: " + newPassValue1.equals(newPassValue2));
                                editListener.applyProfileChanges(nameValue.getText().toString(), newPassValue2);
                                dialog.dismiss();
                            }
                        } else {
                            editListener.applyProfileChanges(nameValue.getText().toString(), newPassValue2);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }
}
