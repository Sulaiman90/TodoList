package com.ms.favtodo.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.ms.favtodo.activity.MainActivity;
import com.ms.favtodo.activity.NewTask;

public class TaskReminderIntentService extends IntentService {

    private static final String TAG = "FavDo_TaskReminder";

    public static final String SERVICE_RESULT = "com.service.result";
    public static final String SERVICE_MESSAGE = "com.service.message";


    public TaskReminderIntentService() {
        super("TaskReminderIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        long rowId = intent != null ? intent.getLongExtra(NewTask.TASK_ID, 1) : 0;
        ReminderTasks.executeTask(this,action,rowId);

       if (ReminderTasks.ACTION_TASK_COMPLETED.equals(action)) {
            //Log.d(TAG,"Main activity called");
           sendMessage();
        }
    }

    private void sendMessage() {
        Intent intent = new Intent(SERVICE_RESULT);
        intent.putExtra(SERVICE_MESSAGE,MainActivity.UPDATE_LIST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
