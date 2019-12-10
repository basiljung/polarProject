package com.example.polarapp.preferencesmanager;

import android.content.*;

public class IntroPreferencesManager {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context context;

    // shared sp mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String WELCOME_SCREEN = "welcome_screen";
    private static final String IS_FIRST_LAUNCH = "isFirstLaunch";

    public IntroPreferencesManager(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(WELCOME_SCREEN, PRIVATE_MODE);
        editor = sp.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return sp.getBoolean(IS_FIRST_LAUNCH, true);
    }
}
