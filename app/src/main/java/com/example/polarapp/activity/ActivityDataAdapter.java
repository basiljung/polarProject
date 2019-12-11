package com.example.polarapp.activity;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;

import com.example.polarapp.R;

import java.util.ArrayList;

public class ActivityDataAdapter extends ArrayAdapter<ActivityData> {
    public ActivityDataAdapter(Context context, ArrayList<ActivityData> activityDataArrayList) {
        super(context, R.layout.activity_item, activityDataArrayList);
    }

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ActivityData base = getItem(position);

        LayoutInflater historyInflater = LayoutInflater.from(getContext());
        View customView = historyInflater.inflate(R.layout.activity_item, parent, false);

        //type of activity
        //TextView type = convertView.findViewById(R.id.typeText);
        TextView type = (TextView) customView.findViewById(R.id.typeText);
        type.setText(base.getType());

        //timestamp of activity
        //TextView timestamp = convertView.findViewById(R.id.timestampText);
        TextView timestamp = (TextView) customView.findViewById(R.id.timestampText);
        timestamp.setText(String.valueOf(base.getTimestamp()));

        //length of activity
        //TextView length = convertView.findViewById(R.id.lengthText);

        TextView length = (TextView) customView.findViewById(R.id.lengthText);
        length.setText(String.valueOf(base.getDistance()));

        return customView;

        //convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_item, parent, false);
        //return convertView;
    }
}
