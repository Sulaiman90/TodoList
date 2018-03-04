package com.ms.favtodo.adapter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.favtodo.R;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.utils.DateUtility;
import com.ms.favtodo.utils.ReminderManager;
import com.ms.favtodo.utils.TaskOperation;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomListAdapter extends BaseAdapter{

    private ArrayList<TaskDetails> taskList;
    private TaskDbHelper dbHelper;

    private TaskOperation taskOperation;
    private Boolean completedTasksOnly = false;

    private static final int TYPE_TASK = 0;
    private static final int TYPE_HEADER = 1;
    private ContentValues values;

    private LayoutInflater inflater;
    private Context mContext;
    private TaskDetails taskDetails;

    private Toast mToast;

    private String toastMsg;
    private View mListRow;

//    private static final String TAG = CustomListAdapter.class.getSimpleName();


    public CustomListAdapter(Context context, ArrayList<TaskDetails> list,Boolean completedOnly){
        mContext = context;
        taskList = list;
        dbHelper = new TaskDbHelper(context);
        taskOperation = new TaskOperation(context);
        completedTasksOnly = completedOnly;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getViewTypeCount() {
        // TYPE_TASK and TYPE_HEADER
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof TaskDetails) {
            return TYPE_TASK;
        }
        return TYPE_HEADER;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        int type = getItemViewType(position);

        if(convertView==null){
            viewHolder = new ViewHolder();

            switch (type) {
                case TYPE_TASK:
                    taskDetails = taskList.get(position);
                    convertView = inflater.inflate(R.layout.item_todo, parent, false);
                    viewHolder.tvTitle =  convertView.findViewById(R.id.title);
                    viewHolder.tvdateTime =  convertView.findViewById(R.id.date_time);
                    viewHolder.checkBox =  convertView.findViewById(R.id.task_done);
                    viewHolder.ivRepeat = convertView.findViewById(R.id.iv_repeat);
               /*   Log.d(TAG,"tasks:position "+position +" title "+taskDetails.getTitle());*/
                    break;
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.section_header, parent, false);
                    viewHolder.tvHeader =  convertView.findViewById(R.id.section_title);
                    break;
            }
            if (convertView != null) {
                convertView.setTag(viewHolder);
            }
        }
        else{
          //  Log.d(TAG,"Else block: type "+type +" "+position);
            viewHolder = (ViewHolder) convertView.getTag();
            if(type == TYPE_TASK){
                taskDetails = taskList.get(position);
            }
        }


        if(type == TYPE_TASK) {
            try {
               // Log.d(TAG,"if type "+type +" "+position);
                viewHolder.tvTitle.setText(taskDetails.getTitle());
                String dateAndTime = taskDetails.getDateAndTime();
                String time = taskDetails.getTime();

                long dateInMilliSeconds = taskDetails.getDateInMilliSeconds();

                String result = DateUtility.checkDates(dateInMilliSeconds, mContext);

                if(taskDetails.getRepeatValue() != 0){
                    viewHolder.ivRepeat.setVisibility(View.VISIBLE);
                }
                else{
                    viewHolder.ivRepeat.setVisibility(View.GONE);
                }
               //  Log.d(TAG,"Task Title "+taskDetails.getTitle() +" hour "+taskDetails.getTaskHour() + " minute "+taskDetails.getTaskMinute());

                if (!result.equals("")) {
                    viewHolder.tvdateTime.setVisibility(View.VISIBLE);
                    if(!time.matches("")){
                        result = result + ", " + time;
                        viewHolder.tvdateTime.setText(result);
                    }
                    else{
                        viewHolder.tvdateTime.setText(result);
                    }
                } else if (dateAndTime != null && !dateAndTime.matches("")) {
                    viewHolder.tvdateTime.setVisibility(View.VISIBLE);
                    viewHolder.tvdateTime.setText(dateAndTime);
                } else {
                    viewHolder.tvdateTime.setVisibility(View.GONE);
                }

                if(completedTasksOnly) {
                    //Log.d(TAG,"in "+taskDetails.getTitle());
                    viewHolder.checkBox.setChecked(true);
                    viewHolder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                    viewHolder.tvdateTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                }
                else{
                    //Log.d(TAG,"Task Done else "+viewHolder.tvdateTime.getText());
                    viewHolder.checkBox.setChecked(false);
                    viewHolder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    if(!TextUtils.isEmpty(viewHolder.tvdateTime.getText())){
                        if(DateUtility.isPassed(dateInMilliSeconds,taskDetails.getTaskHour(),taskDetails.getTaskMinute())){
                            // Log.d(TAG,"iff");
                            viewHolder.tvdateTime.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                        }
                        else{
                            // Log.d(TAG,"else");
                            viewHolder.tvdateTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                        }
                    }
                }

               viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                       // Log.d(TAG,"checkbox clicked "+((CheckBox) v).isChecked());
                      //  Log.d(TAG,"checked:task "+taskDetails.getTitle()+" "+position);
                        values = new ContentValues();
                        taskDetails = taskList.get(position);

                        if (((CheckBox) v).isChecked()) {
                            values.put(TaskEntry.TASK_DONE, 1);
                            toastMsg = mContext.getResources().getString(R.string.task_completed);
                        } else {
                            values.put(TaskEntry.TASK_DONE, 0);
                            toastMsg = mContext.getResources().getString(R.string.task_not_completed);
                        }

                        mListRow = (View) v.getParent();
                        if(!completedTasksOnly && taskDetails.getRepeatValue() != 0){
                            showRepeatAlertDialog(taskDetails.getTaskId(),taskDetails.getRepeatValue(),position);
                        }
                        else{
                            dbHelper.updateTask(taskDetails.getTaskId(), values);
                            ReminderManager.cancelReminderAndNotification(mContext,taskDetails.getTaskId());
                            slideListItem(position);
                        }
                    }
                });
            }
            catch(Exception e){
               //Log.d(TAG,"error "+type );
            }
        }
        else{
             //Log.d(TAG,"else type "+type +" "+position);
            String titleString = (String)getItem(position);
            viewHolder.tvHeader.setText(titleString);
            if (!completedTasksOnly) {
                if (titleString.equals(mContext.getResources().getString(R.string.overdue))) {
                    viewHolder.tvHeader.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                }
                else if (titleString.equals(mContext.getResources().getString(R.string.no_date))) {
                    viewHolder.tvHeader.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey600));
                } else {
                    viewHolder.tvHeader.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                }
            }
        }
        return convertView;
    }


    private void slideListItem(final int position) {
       // Log.d(TAG,"removeListItem");
        final Animation animation = AnimationUtils.loadAnimation(mListRow.getContext(), R.anim.slide_out);
        mListRow.startAnimation(animation);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {

            @Override
            public void run() {
                taskList.remove(position);
                notifyDataSetChanged();
                animation.cancel();
                taskOperation.retrieveTasks(completedTasksOnly);

                if(mToast!= null){
                    mToast.cancel();
                }
                mToast = Toast.makeText(mContext,toastMsg,Toast.LENGTH_SHORT);
                mToast.show();
            }
        }, 300);

    }

    private void showRepeatAlertDialog(final int taskId, final int repeatSpinnerValue, final int position){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new Builder(mContext);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.repeat_dialog_message);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar repeatCal = dbHelper.repeatTask(taskId, repeatSpinnerValue);
                if(repeatCal != null){
                    ReminderManager.scheduleReminder(repeatCal,mContext,taskId);
                }
                ReminderManager.cancelReminderAndNotification(mContext,taskDetails.getTaskId());
                slideListItem(position);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.updateTask(taskDetails.getTaskId(), values);
                ReminderManager.cancelReminderAndNotification(mContext,taskDetails.getTaskId());
                slideListItem(position);
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog repeatAlertDialog = builder.create();
        repeatAlertDialog.show();

        repeatAlertDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                CheckBox checkBox = mListRow.findViewById(R.id.task_done);
                checkBox.setChecked(false);
            }
        });
    }

    private static class ViewHolder{
        TextView tvTitle;
        TextView tvdateTime;
        CheckBox checkBox;
        TextView tvHeader;
        ImageView ivRepeat;

    }
}

