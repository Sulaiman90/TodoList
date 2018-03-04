package com.ms.favtodo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ms.favtodo.R;
import com.ms.favtodo.TodoList;

public class PreferenceUtils {

    public static String getDueTime12HrFormat(Context context){
        return TodoList.sharedPreferences.getString(context.getString(R.string.pref_due_time_12_hr),"6 PM");
    }

    public static String getNotificationSound(Context context){
        return TodoList.sharedPreferences.getString(context.getString(R.string.pref_notification_sound_key),
                NotificationUtils.getDefaultNotificationSound().toString());
    }

    public static boolean isVibrateEnabled(Context context){
        return TodoList.sharedPreferences.getBoolean(context.getString(R.string.pref_notification_vibrate_key), false);
    }

    public static boolean isSoundEnabled(Context context){
        return TodoList.sharedPreferences.getBoolean(context.getString(R.string.pref_enable_sound_key), false);
    }

    public static boolean isAutoSnoozeEnabled(Context context){
        return TodoList.sharedPreferences.getBoolean(context.getString(R.string.pref_auto_snooze_key), false);
    }

    public static boolean isRepeatAlertToneEnabled(Context context){
        return TodoList.sharedPreferences.getBoolean(context.getString(R.string.pref_repeat_alert_tone_key), false);
    }

    public static int getAlertDuration(Context context){
        return TodoList.sharedPreferences.getInt(context.getString(R.string.pref_alarm_duration_key),
                context.getResources().getInteger(R.integer.alarm_duration_default));
    }

    public static int getAutoSnoozeInterval(Context context){
        return TodoList.sharedPreferences.getInt(context.getString(R.string.pref_snooze_interval_key),
                context.getResources().getInteger(R.integer.snooze_duration_default));
    }

    private static String getDueTimeHourAndMin(Context context){
        return TodoList.sharedPreferences.getString(context.getString(R.string.pref_due_hour_minute),"18:0");
    }

    public static void updateDueTimeValue(Context context, String dueTime,String hourAndMin){
        SharedPreferences.Editor editor = TodoList.sharedPreferences.edit();
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
