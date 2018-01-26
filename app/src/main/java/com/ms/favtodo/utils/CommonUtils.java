package com.ms.favtodo.utils;

import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Created by MOHAMED SULAIMAN on 26-01-2018.
 */

public class CommonUtils {

    public static Uri getDefaultNotificationSound(){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        return alarmSound;
    }
}
