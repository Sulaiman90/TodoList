package com.ms.favtodo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ms.favtodo.R;

public class PreferenceUtils {

    public static String getDueTime12HrFormat(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_due_time_12_hr),"6 PM");
    }

    public static String getNotificationSound(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_notification_sound_key),
                CommonUtils.getDefaultNotificationSound().toString());
    }

    public static boolean isVibrateEnabled(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_notification_vibrate_key), false);
    }

    private static String getDueTimeHourAndMin(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.pref_due_hour_minute),"18:0");
    }

    public static void updateDueTimeValue(Context context, String dueTime,String hourAndMin){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.pref_due_time_12_hr), dueTime);
        editor.putString(context.getString(R.string.pref_due_hour_minute),hourAndMin );
        editor.apply();

        ReminderManager.rescheduleAlarm(context);
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
