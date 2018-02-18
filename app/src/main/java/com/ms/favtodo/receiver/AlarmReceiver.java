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
            String taskTitle = b.getString("TaskTitle");
            long rowId =  b.getLong("TaskRowId");

           // NotificationUtils.createNotification(context,taskTitle,rowId);

            Intent newIntent = new Intent(context, AlertActivity.class);
            newIntent.putExtras(b);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        }
    }
}
