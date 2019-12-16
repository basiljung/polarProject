package com.example.polarapp.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.polarapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ActivityDataAdapter extends ArrayAdapter<ActivityData> {
    public ActivityDataAdapter(Context context, ArrayList<ActivityData> activityDataArrayList) {
        super(context, R.layout.history_item, activityDataArrayList);
    }

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ActivityData base = getItem(position);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.ENGLISH);

        LayoutInflater historyInflater = LayoutInflater.from(getContext());
        View customView = historyInflater.inflate(R.layout.history_item, parent, false);

        ImageView imageView = customView.findViewById(R.id.activityImageView);
        if (base.getType().toLowerCase().equals("sleep")) {
            imageView.setImageResource(R.drawable.history_image_sleep);
        } else {
            imageView.setImageResource(R.drawable.history_image_run);
        }

        TextView type = customView.findViewById(R.id.typeText);
        type.setText(base.getType().toUpperCase());

        TextView timestamp = customView.findViewById(R.id.timestampText);
        Date date = new Date(base.getTimestamp().getTime());
        timestamp.setText(sdf.format(date));

        TextView time = customView.findViewById(R.id.timeText);
        time.setText(base.getTime() + " minutes");

        return customView;
    }
}
