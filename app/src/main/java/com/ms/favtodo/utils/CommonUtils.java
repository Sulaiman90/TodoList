package com.ms.favtodo.utils;

import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Created by MOHAMED SULAIMAN on 26-01-2018.
 */

public class CommonUtils {

    public static Uri getDefaultNotificationSound(){
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if(notificationSound == null){
            notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(notificationSound == null){
                notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return notificationSound;
    }



}
