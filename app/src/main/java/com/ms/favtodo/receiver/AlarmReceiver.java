package com.ms.favtodo.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ms.favtodo.R;
import com.ms.favtodo.activity.AlertActivity;
import com.ms.favtodo.activity.MainActivity;
import com.ms.favtodo.activity.NewTask;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.sync.ReminderTasks;
import com.ms.favtodo.sync.TaskReminderIntentService;
import com.ms.favtodo.utils.NotificationUtils;

/**
 * Created by MOHAMED SULAIMAN on 09-09-2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    public void onReceive(Context context, Intent intent) {
        if(intent != null) {

            Bundle b = intent.getExtras();
            Intent newIntent = new Intent(context, AlertActivity.class);

            TaskDbHelper dbHelper = new TaskDbHelper(context);
            boolean snoozeOn = true;

            if (b != null) {
                b.putBoolean(NewTask.PLAY_SOUND, true);
                newIntent.putExtras(b);
                long taskId = b.getLong(NewTask.TASK_ID);
                Cursor c1 =  dbHelper.fetchTask(taskId);
                snoozeOn = c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.SNOOZE_ON)) == 1;
            }
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Log.d(TAG, "AlarmReceiver: snoozeOn "+snoozeOn);
            if(snoozeOn){
                context.startActivity(newIntent);
            }
        }
    }
}
