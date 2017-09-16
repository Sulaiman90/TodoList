package com.ms.favtodo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ms.favtodo.R;

/**
 * Created by MOHAMED SULAIMAN on 16-09-2017.
 */

public class PreferenceUtils {

    public static String getDueTime12HrFormat(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String dueTime = sharedPref.getString(context.getString(R.string.pref_due_time_12_hr),"6 PM");
        return dueTime;
    }

    public static String getDueTimeHourAndMin(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String dueTime = sharedPref.getString(context.getString(R.string.pref_due_hour_minute),"18:0");
        return dueTime;
    }

    public static void updateDueTimeValue(Context context, String dueTime,String hourAndMin){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.pref_due_time_12_hr), dueTime);
        editor.putString(context.getString(R.string.pref_due_hour_minute),hourAndMin );
        editor.commit();
    }

    public static int getHour(Context context){
        String dueHourAndMin =  getDueTimeHourAndMin(context);
        String[] hourMinuteValues =  dueHourAndMin.split(":");
        return Integer.parseInt(hourMinuteValues[0]);
    }

    public static int getMinute(Context context){
        String dueHourAndMin =  getDueTimeHourAndMin(context);
        String[] hourMinuteValues =  dueHourAndMin.split(":");
        return Integer.parseInt(hourMinuteValues[1]);
    }


}
