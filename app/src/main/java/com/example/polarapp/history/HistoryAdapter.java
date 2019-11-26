package com.example.polarapp.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.polarapp.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HistoryAdapter extends ArrayAdapter<HistoryPart> {

    public HistoryAdapter(Context context, ArrayList<HistoryPart> YourHistoryParts){
        super(context, R.layout.history_item, YourHistoryParts);
    }

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final HistoryPart base = getItem(position);

        LayoutInflater historyInflater = LayoutInflater.from(getContext());
        View customView = historyInflater.inflate(R.layout.history_item, parent, false);

        //type of activity
        //TextView type = convertView.findViewById(R.id.typeText);
        TextView type = (TextView) customView.findViewById(R.id.typeText);
        type.setText(base.getType());

        //timestamp of activity
        //TextView timestamp = convertView.findViewById(R.id.timestampText);
        TextView timestamp = (TextView) customView.findViewById(R.id.timestampText);
        timestamp.setText(base.getTimeStamp());

        //length of activity
        //TextView length = convertView.findViewById(R.id.lengthText);

        TextView length = (TextView) customView.findViewById(R.id.lengthText);
        length.setText(base.getLength());

        return customView;

        //convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
        //return convertView;
    }




}

