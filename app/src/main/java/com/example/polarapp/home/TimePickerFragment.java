package com.example.polarapp.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.polarapp.R;

import java.text.DateFormat;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    TimerListener timerListener;
    public TimePickerFragment(TimerListener tl){
        this.timerListener = tl;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog picker = setTime();
        return picker;
    }


    public interface TimerListener {
        void applyTimeChage(String pickerTime);
    }

    public TimePickerDialog setTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timerListener.applyTimeChage(hourOfDay+":"+minute);
            }
        }, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));

        return timePickerDialog;
    }

}
