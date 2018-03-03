package com.ms.favtodo.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.ms.favtodo.activity.NewTask;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.receiver.AlarmReceiver;

import java.util.Calendar;

public class ReminderManager {

    private static final String TAG = "ReminderManager";

    public static void scheduleReminder(Calendar when, Context context, long taskId){

        //Log.d(TAG,"scheduleReminder");
        AlarmManager mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra(NewTask.TASK_ID, taskId);

        PendingIntent pi = PendingIntent.getBroadcast(context, (int)taskId, i, PendingIntent.FLAG_CANCEL_CURRENT);
        if (mAlarmManager != null) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
        }
    }

    public static void cancelReminder(Context context,long taskId){
       // Log.d(TAG, "cancelReminder");
        AlarmManager mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int)taskId, i, PendingIntent.FLAG_UPDATE_CURRENT);
        pi.cancel();
        if (mAlarmManager != null) {
            mAlarmManager.cancel(pi);
        }
    }

    public static void cancelReminderAndNotification(Context context,long taskId){
        cancelReminder(context, taskId);
        cancelNotification(context, taskId);
    }

    public static void cancelNotification(Context context,long notificationId){
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        /*  NOTIFICATION_ID allows you to update or cancel the notification later on */
        if (notificationManager != null) {
            notificationManager.cancel((int)notificationId);
        }
    }

    // reschedule alarm after notification preference time change for task without time

    public static void rescheduleAlarm(Context context){
        TaskDbHelper dbHelper = new TaskDbHelper(context);
        Cursor c1 = dbHelper.fetchTasksWithoutDueTime();
        if(c1!=null) {
            if (c1.getCount() != 0 && c1.moveToFirst()) {
                do {
                    long dateInMs = c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS));
                    int hour = c1.getInt(c1.getColumnIndex(TaskEntry.TASK_HOUR));
                    int minute = c1.getInt(c1.getColumnIndex(TaskEntry.TASK_MINUTE));

                    if(!TaskOperation.isPassed(dateInMs,hour,minute)){

                        int rowId = c1.getInt(c1.getColumnIndex(TaskEntry.TASK_ID));

                        Calendar mCalendar = Calendar.getInstance();
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(dateInMs);

                        mCalendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                        mCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
                        mCalendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
                        mCalendar.set(Calendar.HOUR_OF_DAY, PreferenceUtils.getHour(context));
                        mCalendar.set(Calendar.MINUTE, PreferenceUtils.getMinute(context));

                        cancelReminder(context,rowId);
                        scheduleReminder(mCalendar,context,(long)rowId);
                    }
                }
                while (c1.moveToNext());
            }
            c1.close();
        }
    }

    public static void restartAlarms(Context context){
        TaskDbHelper dbHelper = new TaskDbHelper(context);
        Cursor c1 = dbHelper.fetchInCompletedTasksWithDate();

        if(c1!=null) {
            if (c1.getCount() != 0 && c1.moveToFirst()) {
                do {
                    long dateInMs = c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS));
                    int hour = c1.getInt(c1.getColumnIndex(TaskEntry.TASK_HOUR));
                    int minute = c1.getInt(c1.getColumnIndex(TaskEntry.TASK_MINUTE));

                    if(!TaskOperation.isPassed(dateInMs,hour,minute)){
                        int rowId = c1.getInt(c1.getColumnIndex(TaskEntry.TASK_ID));

                        Calendar mCalendar = Calendar.getInstance();

                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(dateInMs);

                        mCalendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                        mCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
                        mCalendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));

                        if(hour == -1){
                            mCalendar.set(Calendar.HOUR_OF_DAY, PreferenceUtils.getHour(context));
                            mCalendar.set(Calendar.MINUTE, PreferenceUtils.getMinute(context));
                        }
                        else{
                            mCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            mCalendar.set(Calendar.MINUTE, minute);
                        }
                        scheduleReminder(mCalendar,context,(long)rowId);
                    }
                }
                while (c1.moveToNext());
            }
            c1.close();
        }
    }
}
