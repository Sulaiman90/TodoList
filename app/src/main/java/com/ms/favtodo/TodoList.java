package com.ms.favtodo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class TodoList extends Application {

    public static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }


    public static void hideKeyboard(@NonNull Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
