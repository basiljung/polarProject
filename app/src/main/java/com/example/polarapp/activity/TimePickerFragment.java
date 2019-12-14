package com.example.polarapp.activity;

import android.app.*;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    private TimerListener timerListener;

    public TimePickerFragment(TimerListener tl) {
        this.timerListener = tl;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog picker = setTime();
        return picker;
    }

    public interface TimerListener {
        void applyTimeChange(String pickerTime);
    }

    public TimePickerDialog setTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timerListener.applyTimeChange(hourOfDay + ":" + minute);
            }
        }, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));

        return timePickerDialog;
    }
}
