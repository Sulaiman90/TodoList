package com.ms.favtodo.sync;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.widget.CheckBox;

import com.ms.favtodo.activity.MainActivity;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.utils.TaskOperation;

import java.util.List;

/**
 * Created by MOHAMED SULAIMAN on 15-09-2017.
 */

public class ReminderTasks {

    public static final String ACTION_TASK_COMPLETED = "task_completed";
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss_notification";

    public static void executeTask(Context context, String action,long rowId) {
        if (ACTION_TASK_COMPLETED.equals(action)) {
            taskCompleted(context,(int)rowId);
            TaskOperation.cancelNotification(context,rowId);
           /* TaskOperation mTaskOperation = new TaskOperation(context.getApplicationContext());
            mTaskOperation.retrieveTasks(false);*/
        }
        else if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            TaskOperation.cancelNotification(context,rowId);
        }
    }

    private static void taskCompleted(Context context,int rowId){
        ContentValues values = new ContentValues();
        values.put(TaskEntry.TASK_DONE, 1);
        TaskDbHelper mTaskDbHelper = new TaskDbHelper(context);
        mTaskDbHelper.updateTask(rowId, values);
    }

    /*public static boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }*/
}
