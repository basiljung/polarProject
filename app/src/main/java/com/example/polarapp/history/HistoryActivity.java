package com.example.polarapp.history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.polarapp.R;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProfilePreferencesManager profilePreferencesManager;
    ArrayList<HistoryPart> YourHistoryParts = new ArrayList<>();
    ListView listView = null;

    private static final String PROFILE_USER_ID = "profile_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listView = findViewById(R.id.historyList);
        final HistoryAdapter historyAdapter = new HistoryAdapter(this, YourHistoryParts);
        listView.setAdapter(historyAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        profilePreferencesManager = new ProfilePreferencesManager(getApplication().getBaseContext());

        Query activity = db.collection("activities").whereEqualTo("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
                                                                                //profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID)

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

                                YourHistory part = new YourHistory();
                                part.setType(type);
                                part.setTimeStamp(timestamp);
                                part.setLength(length);

                                YourHistoryParts.add(part);
                                historyAdapter.notifyDataSetChanged();

                                Log.d("jolo", document.getId() + " => " + document.getData());
                            }
                        }
                    }

                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}














