package com.example.polarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.LatLng;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    //ArrayList<HistoryPart> YourHistoryActivities = new ArrayList<>();

    ListView listView = null;

    //GeoPoint geoPoint;

    /*
    String activityType = null;
    String activityLength = null;
    String activityTimestamp = null;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);




        listView = findViewById(R.id.historyList);
        //String activityType = findViewById(R.id.type);
        final TextView type = (TextView) findViewById(R.id.type);
        final TextView length = (TextView) findViewById(R.id.length);
        final TextView timestamp = (TextView) findViewById(R.id.timestamp);


        //final AdAdapter adAdapter = new AdAdapter(this, YourAdParts);
        //listView.setAdapter(adAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Query activity = db.collection("activities");//.whereEqualTo("UUID", sharedPreferences.getString("UUID", null));


        activity
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String type = document.get("type").toString();
                                String length = document.get("length").toString();
                                String timestamp = document.get("timestamp").toString();



                                Log.d("jolo", document.getId() + " => " + document.getData());
                                Log.d("jolo", type);
                                Log.d("jolo", length);
                                Log.d("jolo", timestamp);



                            }
                        }
                    }

                });
        type.setText("run");
        timestamp.setText("November 11, 2019");
        length.setText("3.7");

    }
}
