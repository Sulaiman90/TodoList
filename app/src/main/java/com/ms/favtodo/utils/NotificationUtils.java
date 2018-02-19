package com.ms.favtodo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ms.favtodo.R;
import com.ms.favtodo.activity.AlertActivity;
import com.ms.favtodo.activity.NewTask;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.sync.ReminderTasks;
import com.ms.favtodo.sync.TaskReminderIntentService;

public class NotificationUtils {
    private static final String TAG = NotificationUtils.class.getSimpleName();

    public static void createNotification(Context context,long rowId){
       // Log.d(TAG,"createNotification "+alarmSound);
        TaskDbHelper dbHelper = new TaskDbHelper(context);
        Cursor c1 =  dbHelper.fetchTask(rowId);

        if(c1.getCount() == 0 || c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.TASK_DONE)) == 1){
            return;
        }

        String taskTitle = c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE));

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "M_CH_ID")
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(taskTitle)
                .setContentText(context.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.app_name)))
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentIntent(contentIntent(context,rowId))
                //.addAction(taskCompletedAction(context,rowId))
                //.addAction(ignoreReminderAction(context,rowId))
                .setAutoCancel(true);

        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        /*  NOTIFICATION_ID allows you to update or cancel the notification later on */
        notificationManager.notify((int)rowId, notification);
    }

    private static PendingIntent contentIntent(Context context, long rowId) {
        //Log.e(TAG,"rowId "+rowId);
        Intent startActivityIntent = new Intent(context, AlertActivity.class);

        Bundle bundle = new Bundle();
        bundle.putLong(NewTask.TASK_ID, rowId);
        bundle.putBoolean(NewTask.PLAY_SOUND, false);
        startActivityIntent.putExtras(bundle);

        return PendingIntent.getActivity(
                context ,
                (int)rowId ,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Action taskCompletedAction(Context context, long rowID){
        Intent launchIntent = new Intent(context, TaskReminderIntentService.class);
        launchIntent.setAction(ReminderTasks.ACTION_TASK_COMPLETED);
        launchIntent.putExtra("taskRowId",rowID);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                (int)rowID,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Action taskDoneAction = new Action(R.drawable.ic_done_white_24dp,
                context.getResources().getString(R.string.task_done_remainder),
                pendingIntent);
        return taskDoneAction;
    }

    private static Action ignoreReminderAction(Context context, long rowID){
        Intent ignoreReminderIntent = new Intent(context, TaskReminderIntentService.class);
        ignoreReminderIntent.setAction(ReminderTasks.ACTION_DISMISS_NOTIFICATION);
        ignoreReminderIntent.putExtra("taskRowId",rowID);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                (int)rowID,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Action ignoreReminderAction = new Action(R.drawable.ic_cancel_black_24px,
                context.getResources().getString(R.string.ignore_remainder),
                pendingIntent);
        return ignoreReminderAction;
    }
}

