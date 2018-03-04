package com.ms.favtodo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;


import com.ms.favtodo.activity.AlertActivity;
import com.ms.favtodo.activity.NewTask;

public class AlarmReceiver extends BroadcastReceiver {

   // private static final String TAG = AlarmReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if(intent != null && context!= null) {

            Bundle b = intent.getExtras();
            Intent newIntent = new Intent(context, AlertActivity.class);

            if (b != null) {
                b.putBoolean(NewTask.PLAY_SOUND, true);
                newIntent.putExtras(b);
            }
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Log.d(TAG, "AlarmReceiver: ");
            context.startActivity(newIntent);
        }
    }
}
