package com.ms.favtodo.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ms.favtodo.activity.MainActivity;

/**
 * Created by MOHAMED SULAIMAN on 15-09-2017.
 */

public class TaskReminderIntentService extends IntentService {

    private static final String TAG = "TaskReminder";

    private LocalBroadcastManager localBroadcastManager;
    public static final String SERVICE_RESULT = "com.service.result";
    public static final String SERVICE_MESSAGE = "com.service.message";


    public TaskReminderIntentService() {
        super("TaskReminderIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        long rowId = intent.getLongExtra("taskRowId",1);
        ReminderTasks.executeTask(this,action,rowId);

       if (ReminderTasks.ACTION_TASK_COMPLETED.equals(action)) {
            Log.d(TAG,"Main activity called");
            Intent intent2 = new Intent(SERVICE_RESULT);
            intent2.setAction(SERVICE_MESSAGE);
            localBroadcastManager.sendBroadcast(intent2);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }
}
