package com.example.polarapp.profile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.polarapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.Calendar;

public class EditDialog extends DialogFragment {
    private RadioGroup sexGroup;
    private EditListener editListener;
    private String defaultValue;
    private int action;
    private View view;
    private AlertDialog dialog;
    private DatePickerDialog picker;

    public EditDialog(EditListener cb, String defaultValue, int action) {
        this.editListener = cb;
        this.defaultValue = defaultValue;
        this.action = action;
    }

    public interface EditListener {
        void applySexChanges(int sex);

        void applyBirthChanges(String birthday);

        void applyHeightChanges(int height);

        void applyWeightChanges(int weight);

        void applyEmailChanges(String email);

        void applyPhoneChanges(String phone);

        void applyLocationChanges(String location);

        void applyProfileChanges(String name);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        switch (action) {
            case 1:
                dialog = editSex();
                break;
            case 2:
                picker = editAge(defaultValue);
                return picker;
            case 3:
                dialog = numberPickerDialog("Select your height", 100, 250, Integer.parseInt(defaultValue));
                break;
            case 4:
                dialog = numberPickerDialog("Select your weight", 30, 150, Integer.parseInt(defaultValue));
                ;
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

    private AlertDialog editSex() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_sex, null);
        sexGroup = view.findViewById(R.id.groupSex);
        sexGroup.check(Integer.parseInt(defaultValue));

        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = view.findViewById(R.id.saveButton);
                Button negativeButton = view.findViewById(R.id.cancelButton);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editListener.applySexChanges(sexGroup.getCheckedRadioButtonId());
                        dialog.dismiss();
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

    private AlertDialog numberPickerDialog(final String title, int minValue, int maxValue, int defaultValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_physical_data, null);

        final NumberPicker numberPicker = view.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setValue(defaultValue);

        TextView textView = view.findViewById(R.id.titlePhysicalData);
        textView.setText(title);

        builder.setView(view);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = view.findViewById(R.id.saveButton);
                Button negativeButton = view.findViewById(R.id.cancelButton);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (title.toLowerCase().contains("height")) {
                            editListener.applyHeightChanges(numberPicker.getValue());
                            dialog.dismiss();
                        } else if (title.toLowerCase().contains("weight")) {
                            editListener.applyWeightChanges(numberPicker.getValue());
                            dialog.dismiss();
                        }
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

    private DatePickerDialog editAge(String defaultValue) {
        String[] date = defaultValue.split("/");
        Log.d("Default Value", defaultValue);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
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
                        Log.d("Before EditListener", day + "/" + month + "/" + year);
                        editListener.applyBirthChanges(day + "/" + month + "/" + year);
                    }
                }, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0])); // Select BirthDate on DB

        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "SAVE", datePickerDialog);
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "CANCEL", datePickerDialog);

        return datePickerDialog;
    }

    private AlertDialog editEmail() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_email, null);
        final EditText email = view.findViewById(R.id.emailValue);

        final TextInputLayout layoutEmail = view.findViewById(R.id.inputLayout);
        layoutEmail.setErrorTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentDark)));

        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = view.findViewById(R.id.saveButton);
                Button negativeButton = view.findViewById(R.id.cancelButton);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layoutEmail.setError(null);
                        if (isValidEmail(email.getText().toString())) {
                            editListener.applyEmailChanges(email.getText().toString());
                            dialog.dismiss();
                        } else {
                            layoutEmail.setError("Invalid email");
                        }
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        return alertDialog;
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private AlertDialog editPhone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_phone, null);
        final EditText editText = view.findViewById(R.id.editPhoneValue);
        final CountryCodePicker ccp = view.findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(editText);
        ccp.setCountryPreference("ES,FI,CH");
        ccp.detectSIMCountry(true);

        builder.setView(view);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = view.findViewById(R.id.saveButton);
                Button negativeButton = view.findViewById(R.id.cancelButton);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editListener.applyPhoneChanges(ccp.getFormattedFullNumber());
                        dialog.dismiss();
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

    private AlertDialog editLocation() {
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

        builder.setView(view);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = view.findViewById(R.id.saveButton);
                Button negativeButton = view.findViewById(R.id.cancelButton);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editListener.applyLocationChanges(editText.getText().toString() + "/" +
                                ccp.getSelectedCountryName());
                        dialog.dismiss();
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

    private AlertDialog editProfile() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.layout_dialog_edit_profile, null);
        final TextView nameValue = view.findViewById(R.id.nameValue);

        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = view.findViewById(R.id.saveButton);
                Button negativeButton = view.findViewById(R.id.cancelButton);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editListener.applyProfileChanges(nameValue.getText().toString());
                        dialog.dismiss();

                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        return alertDialog;
    }
}
