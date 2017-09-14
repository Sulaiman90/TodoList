package com.ms.favtodo.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by MOHAMED SULAIMAN on 15-09-2017.
 */

public class TaskReminderIntentService extends IntentService {

    public TaskReminderIntentService() {
        super("TaskReminderIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        long rowId = intent.getLongExtra("taskRowId",1);
        ReminderTasks.executeTask(this,action,rowId);
    }
}
