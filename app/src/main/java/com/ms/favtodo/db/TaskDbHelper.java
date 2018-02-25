package com.ms.favtodo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.model.TaskDetails;

import java.util.Calendar;


public class TaskDbHelper extends SQLiteOpenHelper {

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

    public Cursor fetchCompletedTasks(int doneOrNot){
        // 0 -- not completed , 1 -- completed
        String[] columns = new String[] { TaskEntry.TASK_ID, TaskEntry.TASK_TITLE, TaskEntry.TASK_DATE,
                                         TaskEntry.TASK_TIME,TaskEntry.TASK_DATE_AND_TIME , TaskEntry.TASK_DONE,
                                         TaskEntry.TASK_DATE_IN_MS , TaskEntry.TASK_HOUR ,TaskEntry.TASK_MINUTE };
        Cursor cursor = this.getReadableDatabase().query(
                    TaskEntry.TABLE_NAME,
                    null,
                    TaskEntry.TASK_DONE + "=?",
                    new String[] {String.valueOf(doneOrNot)},
                    null,null,
                    TaskEntry.TASK_DATE_IN_MS+" ASC",
                    null);
        if (cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchTask(long rowId){
        String[] columns = new String[] { TaskEntry.TASK_ID, TaskEntry.TASK_TITLE, TaskEntry.TASK_DATE,
                TaskEntry.TASK_TIME,TaskEntry.TASK_DATE_AND_TIME , TaskEntry.TASK_DONE,
                TaskEntry.TASK_DATE_IN_MS , TaskEntry.TASK_HOUR ,TaskEntry.TASK_MINUTE };
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
            Log.d(TAG,"updateTask "+result);
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
            Log.d(TAG,"Deleted rows "+result);
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
