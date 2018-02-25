package com.ms.favtodo.db;

import android.provider.BaseColumns;


public class TaskContract {

    public static final String DATABASE_NAME = "Todo";
    public static final int DATABASE_VERSION = 3;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";

        public static final String TASK_ID = "_id";
        public static final String TASK_TITLE = "title";
        public static final String TASK_DATE = "date";
        public static final String TASK_TIME = "time";
        public static final String TASK_DATE_AND_TIME = "datetime";
        public static final String TASK_DONE = "finishedOrNot";
        public static final String TASK_DATE_IN_MS ="dateInMs";
        public static final String TASK_HOUR = "hour";
        public static final String TASK_MINUTE = "minute";
        public static final String NOTIFICATION_SOUND = "notificationSound";
        public static final String NOTIFICATION_VIBRATE = "vibrate";
        public static final String NOTIFICATION_SOUND_ENABLED = "soundEnabled";
        public static final String SNOOZE_ON = "snoozeOn";
        public static final String TASK_REPEAT = "repeat";
    }
}
