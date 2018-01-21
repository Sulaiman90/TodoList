package com.ms.favtodo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ms.favtodo.R;
import com.ms.favtodo.activity.NewTask;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.sync.ReminderTasks;
import com.ms.favtodo.sync.TaskReminderIntentService;

/**
 * Created by MOHAMED SULAIMAN on 16-09-2017.
 */

public class NotificationUtils {
    private static final String TAG = NotificationUtils.class.getSimpleName();

    public static void createNotification(Context context,String TaskTitle,long rowId){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
       // Log.d(TAG,"createNotification "+alarmSound);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "M_CH_ID")
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(TaskTitle)
                .setContentText(context.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.app_name)))
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context,rowId))
                .addAction(taskCompletedAction(context,rowId))
                .addAction(ignoreReminderAction(context,rowId))
                .setAutoCancel(true)
                .setSound(alarmSound);

        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_INSISTENT;

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        /*  NOTIFICATION_ID allows you to update or cancel the notification later on */
        notificationManager.notify((int)rowId, notification);
    }

    private static PendingIntent contentIntent(Context context, long rowId) {
        Intent startActivityIntent = new Intent(context, NewTask.class);

        TaskDbHelper dbHelper = new TaskDbHelper(context);
        Cursor c1 =  dbHelper.fetchTask(rowId);

        //Log.e(TAG,"rowId "+rowId);

        TaskDetails task = new TaskDetails();
        task.setTaskId(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_ID)));
        task.setTitle(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
        task.setDateAndTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE_AND_TIME)));
        task.setDate(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE)));
        task.setTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TIME)));
        task.setTaskDone(c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.TASK_DONE)));
        task.setDateInMilliSeconds(c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS)));
        task.setTaskHour(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_HOUR)));
        task.setTaskMinute(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_MINUTE)));

        startActivityIntent.putExtra("title", task.getTitle());
        startActivityIntent.putExtra("id", task.getTaskId());
        startActivityIntent.putExtra("date", task.getDate());
        startActivityIntent.putExtra("time", task.getTime());
        startActivityIntent.putExtra("doneOrNot", task.getTaskDone());
        startActivityIntent.putExtra("timeInMs", task.getDateInMilliSeconds());
        startActivityIntent.putExtra("hour", task.getTaskHour());
        startActivityIntent.putExtra("minute", task.getTaskMinute());
        startActivityIntent.putExtra("TaskRowId",rowId);

        return PendingIntent.getActivity(
                context,
                (int)rowId,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Action taskCompletedAction(Context context, long rowID){
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

    public static Action ignoreReminderAction(Context context, long rowID){
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

