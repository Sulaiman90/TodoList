package com.ms.favtodo.sync;

import android.content.ContentValues;
import android.content.Context;

import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.utils.ReminderManager;


public class ReminderTasks {

    public static final String ACTION_TASK_COMPLETED = "task_completed";
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss_notification";

    public static void executeTask(Context context, String action,long rowId) {
        if (ACTION_TASK_COMPLETED.equals(action)) {
            taskCompleted(context,(int)rowId);
            ReminderManager.cancelNotification(context,rowId);
        }
        else if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            ReminderManager.cancelNotification(context,rowId);
        }
    }

    private static void taskCompleted(Context context,int rowId){
        ContentValues values = new ContentValues();
        values.put(TaskEntry.TASK_DONE, 1);
        TaskDbHelper mTaskDbHelper = new TaskDbHelper(context);
        mTaskDbHelper.updateTask(rowId, values);
    }

}
