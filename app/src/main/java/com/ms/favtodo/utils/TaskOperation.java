package com.ms.favtodo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ms.favtodo.activity.MainActivity;
import com.ms.favtodo.activity.NewTask;
import com.ms.favtodo.R;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.adapter.CustomListAdapter;
import com.ms.favtodo.model.TaskDetails;


import java.util.ArrayList;

import static com.ms.favtodo.utils.DateUtility.isTomorrow;

public class TaskOperation {

    private TaskDbHelper dbHelper;
    private Context mContext;

   // private static final String TAG = TaskOperation.class.getSimpleName();

    public TaskOperation(Context context){
        mContext = context;
        dbHelper = new TaskDbHelper(context);
    }

    public int retrieveTasks(Boolean completedTasksOnly){

        ArrayList<TaskDetails> taskList = new ArrayList<>();
        taskList.clear();

        ArrayList finalTaskList = new ArrayList();
        finalTaskList.clear();

        ArrayList<TaskDetails>  overdueTasks = new ArrayList<>();
        overdueTasks.clear();
        ArrayList<TaskDetails>  todayTasks = new ArrayList<>();
        todayTasks.clear();
        ArrayList<TaskDetails>  tomorrowTasks = new ArrayList<>();
        tomorrowTasks.clear();
        ArrayList<TaskDetails>  thisWeekTasks = new ArrayList<>();
        thisWeekTasks.clear();
        ArrayList<TaskDetails>  nextWeekTasks = new ArrayList<>();
        nextWeekTasks.clear();
        ArrayList<TaskDetails> thisMonthTasks =new ArrayList<>();
        thisMonthTasks.clear();
        ArrayList<TaskDetails>  nextMonthTasks =new ArrayList<>();
        nextMonthTasks.clear();
        ArrayList<TaskDetails>  laterTasks = new ArrayList<>();
        laterTasks.clear();

        ArrayList<TaskDetails>  noDateTasks = new ArrayList<>();
        noDateTasks.clear();

        Cursor c1;
        int totalTasks = 0;

        if(completedTasksOnly){
            c1 = dbHelper.fetchCompletedTasks();
        }
        else{
            c1 = dbHelper.fetchInCompletedTasks();
        }

        //Log.d(TAG,"getCount "+c1.getCount());
       /* Log.d(TAG,"completedTasksOnly "+ completedTasksOnly);

        int totalTasks = dbHelper.rowcount();
        Log.d(TAG,"totalTasks "+totalTasks);*/

        if(c1!=null){
            totalTasks = c1.getCount();
            if(c1.getCount()!=0 && c1.moveToFirst()){
                do{
                    TaskDetails task = new TaskDetails();
                    task.setTitle(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
                    task.setDateAndTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE_AND_TIME)));

                 /*  Log.d(TAG,"Title "+c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE))
                            + " date "+c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE_IN_MS)));*/

                    task.setDate(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE)));
                    task.setTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TIME)));
                    task.setTaskDone(c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.TASK_DONE)));
                    task.setDateInMilliSeconds(c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS)));
                    task.setTaskId(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_ID)));
                    task.setTaskHour(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_HOUR)));
                    task.setTaskMinute(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_MINUTE)));
                    task.setRepeatValue(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_REPEAT)));
                    taskList.add(task);

                    long date = c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS));

                    if(date==0){
                        noDateTasks.add(task);
                    }
                    else if(DateUtility.isPassed(date,task.getTaskHour(),task.getTaskMinute())){
                       // Log.d(TAG,"isToday "+c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
                       // Log.d(TAG,"isPassed:title "+task.getTitle() + " hour "+task.getTaskHour() + " minute "+task.getTaskMinute());
                        overdueTasks.add(task);
                    }
                    else if(DateUtils.isToday(date)){
                       // Log.d(TAG,"isToday "+c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
                        todayTasks.add(task);
                    }
                    else if(isTomorrow(date)){
                        tomorrowTasks.add(task);
                    }
                    else if(DateUtility.checkIfThisWeek(date)){
                        thisWeekTasks.add(task);
                    }
                    else if(DateUtility.checkIfNextWeek(date)){
                        nextWeekTasks.add(task);
                    }
                    else if(DateUtility.checkIfThisMonth(date)){
                        thisMonthTasks.add(task);
                    }
                    else if(DateUtility.checkIfNextMonth(date)){
                        nextMonthTasks.add(task);
                    }
                    else if(DateUtility.checkIfLater(date)){
                        laterTasks.add(task);
                    }
                }
                while (c1.moveToNext());
            }
            else if(c1.getCount()==0){
                c1.close();
                setInfoText(completedTasksOnly);
            }
        }

       if(overdueTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.overdue));
            finalTaskList.addAll(overdueTasks);
        }
        if(todayTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.today));
            finalTaskList.addAll(todayTasks);
        }
        if(tomorrowTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.tomorrow));
            finalTaskList.addAll(tomorrowTasks);
        }
        if(thisWeekTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.this_week));
            finalTaskList.addAll(thisWeekTasks);
        }
        if(nextWeekTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.next_week));
            finalTaskList.addAll(nextWeekTasks);
        }
        if(thisMonthTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.this_month));
            finalTaskList.addAll(thisMonthTasks);
        }
        if(nextMonthTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.next_month));
            finalTaskList.addAll(nextMonthTasks);
        }
        if(laterTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.later));
            finalTaskList.addAll(laterTasks);
        }
        if(noDateTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.no_date));
            finalTaskList.addAll(noDateTasks);
        }

        CustomListAdapter adapter;
        if(completedTasksOnly){
            adapter = new CustomListAdapter(mContext,taskList,true);
        }
        else{
            adapter = new CustomListAdapter(mContext,finalTaskList,completedTasksOnly);
        }

        ListView listView = ((Activity)mContext).findViewById(R.id.todo_lists);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (parent.getItemAtPosition(position) instanceof TaskDetails) {
                        TaskDetails tasks = (TaskDetails) parent.getItemAtPosition(position);
                        Intent intent = new Intent(mContext, NewTask.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt(NewTask.TASK_ID, tasks.getTaskId());
                       // Log.d(TAG,"Item:title "+tasks.getTitle() +" "+tasks.getDate());
                        //Log.d(TAG,"Item:ms "+tasks.getTaskDone() +" "+tasks.getDateInMilliSeconds());
                        intent.putExtras(bundle);
                        ((Activity) mContext).startActivityForResult(intent,MainActivity.TODO_REQUEST_CODE);
                    }
                }
                catch (Exception e) {
                    //Log.d(TAG,"Selected Header ");
                }
            }
        });
        return totalTasks;
    }

    private void setInfoText(Boolean completedTasksOnly){
        TextView noFinishedTasks = ((Activity)mContext).findViewById(R.id.no_tasks);
        LinearLayout mEmptyLayout = ((Activity)mContext).findViewById(R.id.toDoEmptyView);
        if(completedTasksOnly){
            noFinishedTasks.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);
        }
        else {
            mEmptyLayout.setVisibility(View.VISIBLE);
            noFinishedTasks.setVisibility(View.GONE);
        }
    }

}
