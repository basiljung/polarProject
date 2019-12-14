package com.example.polarapp.preferencesmanager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfilePreferencesManager {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context context;
    private FirebaseFirestore db;

    // Shared preferences file name
    private static final String PROFILE_USER_ID = "profile_user_id";
    private static final String USER_ID = "id";
    private static final String PROFILE_USER_NAME = "profile_user_name";
    private static final String PROFILE_USER_EMAIL = "profile_user_email";
    private static final String PROFILE_USER_PHONE = "profile_user_phone";
    private static final String PROFILE_USER_CITY = "profile_user_city";
    private static final String PROFILE_USER_COUNTRY = "profile_user_country";
    private static final String PROFILE_USER_BIRTH = "profile_user_birth";
    private static final String PROFILE_USER_SEX = "profile_user_sex";
    private static final String PROFILE_USER_HEIGHT = "profile_user_height";
    private static final String PROFILE_USER_WEIGHT = "profile_user_weight";

    public ProfilePreferencesManager(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        sp = context.getSharedPreferences(PROFILE_USER_ID, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    /*public void setUserProfileData(Map<String, Object> userData) {
        editor.putString(PROFILE_USER_NAME, String.valueOf(userData.get("Name")));
        editor.putString(PROFILE_USER_EMAIL, String.valueOf(userData.get("Email")));
        editor.putString(PROFILE_USER_COUNTRY, String.valueOf(userData.get("Country")));
        editor.putString(PROFILE_USER_CITY, String.valueOf(userData.get("City")));
        editor.putString(PROFILE_USER_PHONE, String.valueOf(userData.get("Phone")));
        editor.putString(PROFILE_USER_SEX, String.valueOf(userData.get("Sex")));
        editor.putInt(PROFILE_USER_HEIGHT, Integer.parseInt(String.valueOf(userData.get("Height"))));
        editor.putInt(PROFILE_USER_WEIGHT, Integer.parseInt(String.valueOf(userData.get("Weight"))));
        editor.putString(PROFILE_USER_BIRTH, String.valueOf(userData.get("BirthDate")));
        editor.commit();

        db.collection("profile")
                .add(userData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        editor = sp.edit();
                        editor.putString(USER_ID, documentReference.getId());
                        editor.commit();
                        Log.d("MyApp", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "Error adding document", e);
                    }
                });
    }*/

    public String getStringProfileValue(String key) {
        return sp.getString(key, null);
    }

    public String getIntProfileValue(String key) {
        return String.valueOf(sp.getInt(key, 0));
    }

    public void setStringProfileValue(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void setIntProfileValue(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public Map<String, Object> getUserProfileData() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("Name", sp.getString(PROFILE_USER_NAME, ""));
        userData.put("Email", sp.getString(PROFILE_USER_EMAIL, ""));
        userData.put("Phone", sp.getString(PROFILE_USER_PHONE, ""));
        userData.put("City", sp.getString(PROFILE_USER_CITY, ""));
        userData.put("Country", sp.getString(PROFILE_USER_COUNTRY, ""));
        userData.put("Sex", sp.getString(PROFILE_USER_SEX, ""));
        userData.put("Weight", sp.getInt(PROFILE_USER_WEIGHT, 0));
        userData.put("Height", sp.getInt(PROFILE_USER_HEIGHT, 0));
        userData.put("BirthDate", sp.getString(PROFILE_USER_BIRTH, ""));
        return userData;
    }

    /*public void updateValue(String param, String value) {
        DocumentReference docRef = db.collection("profile").document(sp.getString(USER_ID, "Error"));

        docRef
                .update(param, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("MyApp", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "Error updating document", e);
                    }
                });
    }*/
}
