package com.ms.favtodo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.utils.ReminderManager;
import com.ms.favtodo.utils.TaskOperation;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class TaskDbHelper extends SQLiteOpenHelper {

    private Context mContext;

    private static final String TAG = "FavDo_TaskDbHelper";

    public TaskDbHelper(Context context){
        super(context, TaskContract.DATABASE_NAME, null, TaskContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+ TaskEntry.TABLE_NAME + " ( " +
                TaskEntry.TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskEntry.TASK_TITLE + " TEXT NOT NULL, " +
                TaskEntry.TASK_DATE + " TEXT, " +
                TaskEntry.TASK_TIME + " TEXT, " +
                TaskEntry.TASK_DATE_AND_TIME + " TEXT, " +
                TaskEntry.TASK_DONE + " INT DEFAULT 0, " +
                TaskEntry.TASK_DATE_IN_MS + " INT, " +
                TaskEntry.TASK_HOUR + " INT, " +
                TaskEntry.TASK_MINUTE + " INT, " +
                TaskEntry.NOTIFICATION_SOUND + " TEXT, " +
                TaskEntry.NOTIFICATION_VIBRATE + " INT DEFAULT 0, " +
                TaskEntry.NOTIFICATION_SOUND_ENABLED + " INT DEFAULT 0, " +
                TaskEntry.SNOOZE_ON + " INT DEFAULT 1, " +
                TaskEntry.TASK_REPEAT + " INT DEFAULT 0" +
                ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME);
        onCreate(db);
    }

    public Cursor fetchCompletedTasks(){
        Cursor cursor = this.getReadableDatabase().query(
                     TaskEntry.TABLE_NAME,
                    null,
                    TaskEntry.TASK_DONE + "=?",
                     new String[] {String.valueOf(1)},
                    null,
                    null,
                    TaskEntry.TASK_DATE_IN_MS+" ASC",
                    null);
        if (cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchInCompletedTasks(){
        Cursor cursor = this.getReadableDatabase().query(
                TaskEntry.TABLE_NAME,
                null,
                TaskEntry.TASK_DONE + "=?",
                new String[] {String.valueOf(0)},
                null,
                null,
                TaskEntry.TASK_ID+" DESC",
                null);
        if (cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchTask(long rowId){
        Cursor cursor = this.getReadableDatabase().query(
                TaskEntry.TABLE_NAME,
                null,
                TaskEntry.TASK_ID + "=?",
                new String[] {String.valueOf(rowId)},
                null,
                null,
                null,
                null);
        if (cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Calendar repeatTask(int taskId, int repeatValue){

        Cursor c1 = fetchTask(taskId);

        if(c1.getCount() == 0){
           return null;
        }

        //Log.d(TAG, "repeatValue "+repeatValue);

        long dateInMillis = c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS));
        String time =  c1.getString(c1.getColumnIndex(TaskEntry.TASK_TIME));

        Calendar repeat = Calendar.getInstance();
        Calendar current = Calendar.getInstance();

        repeat.setTimeInMillis(dateInMillis);
        current.setTimeInMillis(dateInMillis);

        if(repeatValue == 1){
            repeat.add(Calendar.DATE,1);
        }
        else if(repeatValue == 2){
            repeat.add(Calendar.DATE,7);
        }
        else if(repeatValue == 3){
            repeat.add(Calendar.MONTH, 2);
        }
        else if(repeatValue == 4){
            repeat.add(Calendar.YEAR, 1);
        }

        long updateDateInMillis = repeat.getTimeInMillis();

        SimpleDateFormat day_date = new SimpleDateFormat("EEE");
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");

        String dayName = day_date.format(repeat.getTime());
        String monthName = month_date.format(repeat.getTime());

        int year = repeat.get(Calendar.YEAR);
        int day =  repeat.get(Calendar.DAY_OF_MONTH);

        String date = TaskOperation.setDateString(year, monthName, day, dayName);
        String dateAndTime = TaskOperation.getDateAndTime(date, time);

        ContentValues cv = new ContentValues();
        cv.put(TaskEntry.TASK_DONE, 0);
        cv.put(TaskEntry.TASK_DATE_IN_MS, updateDateInMillis);
        cv.put(TaskEntry.TASK_DATE, date);
        cv.put(TaskEntry.TASK_DATE_AND_TIME, dateAndTime);

        int rowsUpdated = updateTask(taskId, cv);

        return repeat;
    }

    // get incomplete tasks with dates set

    public Cursor fetchInCompletedTasksWithDate(){
        Cursor cursor = this.getReadableDatabase().query(
                TaskEntry.TABLE_NAME,
                null,
                TaskEntry.TASK_DONE + "=?" + " AND " + TaskEntry.TASK_DATE + "!=?" ,
                new String[] {String.valueOf(0),""},
                null,
                null,
                TaskEntry.TASK_ID+" DESC",
                null);
        if (cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    // get incomplete tasks without due time

    public Cursor fetchTasksWithoutDueTime(){
        String empty = "";
        Cursor cursor = this.getReadableDatabase().query(
                TaskEntry.TABLE_NAME,
                null,
                TaskEntry.TASK_DONE + "=?"
                        + " AND " + TaskEntry.TASK_HOUR + "=?"
                        + " AND " + TaskEntry.TASK_DATE + "!=?" ,
                new String[] {String.valueOf(0),String.valueOf(-1),empty},
                null,
                null,
                null,
                null);
        if (cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }


    public int updateTask(int taskId,ContentValues cv) {
        SQLiteDatabase db = this.getWritableDatabase();
        // updating row
        int result = 0;
        try{
            result = db.update(TaskEntry.TABLE_NAME, cv,  TaskEntry.TASK_ID + " = "+taskId, null);
           // Log.d(TAG,"updateTask "+result);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public int deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        try {
            result = db.delete(TaskEntry.TABLE_NAME, TaskEntry.TASK_ID + " = ?", new String[]{Integer.toString(taskId)});
            //Log.d(TAG,"Deleted rows "+result);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public long insertTask(ContentValues values){
        SQLiteDatabase db = this.getWritableDatabase();
        long rowId = db.insert(TaskEntry.TABLE_NAME,null,values);
        db.close();
        return rowId;
    }

    public long insertTask(String title,String date,String time,String dateAndTime,int done,long dateInMs ,
                           int hour, int minute){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(TaskEntry.TASK_TITLE,title);
        values.put(TaskEntry.TASK_DATE,date);
        values.put(TaskEntry.TASK_TIME,time);
        values.put(TaskEntry.TASK_DATE_AND_TIME,dateAndTime);
        values.put(TaskEntry.TASK_DONE,done);
        values.put(TaskEntry.TASK_DATE_IN_MS,dateInMs);
        values.put(TaskEntry.TASK_HOUR,hour);
        values.put(TaskEntry.TASK_MINUTE,minute);

        long rowId = db.insert(TaskEntry.TABLE_NAME,null,values);
        db.close();
        return rowId;
    }

    public int rowcount() {
        int row;
        String selectQuery = "SELECT  * FROM " + TaskEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        row=cursor.getCount();
        cursor.close();
        return row;
    }
}
