package com.ms.favtodo.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ms.favtodo.R;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.dialog.AlertDialogFragment;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.utils.PreferenceUtils;
import com.ms.favtodo.utils.ReminderManager;
import com.ms.favtodo.utils.TaskOperation;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewTask extends AppCompatActivity {

    private static final String TAG = "FavDo_NewTask";
    private static EditText mTitleText;
    private static EditText mDateText;
    private static EditText mTimeText;
    private static TextView mTaskStatus;
    private static CheckBox mTaskDone;

    private static ImageButton mClearDate;
    private static ImageButton mClearTime;

    private TaskDbHelper dbHelper;
    private static TaskOperation taskOperation;

    private Boolean newTask = false;

    private Toast toastobject;

    private DialogFragment mDialog;

    private static long dateInMillis = 0;
    public static int taskHour = -1;
    public static int taskMinute = -1;

    private static String dateText = "";
    private static String timeText = "";
    private int taskId = 0;

    private static Calendar mCalendar;

    private static LinearLayout mTimeLayout;
    private String notificationSound = "";
    private TaskDetails task;

    @BindView(R.id.enableVibrate_switch) Switch mVibrateSwitch;
    @BindView(R.id.enableSound_cb) CheckBox mSoundCheckBox;
    @BindView(R.id.choose_alarm_sound) Button mChooseSoundButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        ButterKnife.bind(this);

        dateInMillis = 0;
        dateText = "";
        timeText = "";

        dbHelper = new TaskDbHelper(this);
        taskOperation = new TaskOperation(this);
        task = new TaskDetails();
        mCalendar = Calendar.getInstance();

        mTitleText =  findViewById(R.id.title);
        mDateText =  findViewById(R.id.dateText);
        mTimeText = findViewById(R.id.timeText);
        mTaskStatus =  findViewById(R.id.task_status);
        mTaskDone = findViewById(R.id.task_finished);

        mClearDate = findViewById(R.id.clear_date);
        mClearTime = findViewById(R.id.clear_time);

        mTimeLayout = findViewById(R.id.timeLinearLayout);

        LinearLayout linearLayout = findViewById(R.id.mark_as_done);

        Intent intent = getIntent();
        newTask = intent.getBooleanExtra("NewTask", false);

        mClearDate.setVisibility(View.GONE);
        mClearTime.setVisibility(View.GONE);
        //Log.d(TAG,"New Task "+newTask);

        if (newTask) {
            linearLayout.setVisibility(View.GONE);
            mTimeLayout.setVisibility(View.GONE);
            taskHour = -1;
            taskMinute = -1;
            notificationSound = PreferenceUtils.getNotificationSound(this);
            mSoundCheckBox.setChecked(PreferenceUtils.isSoundEnabled(this));
            mVibrateSwitch.setChecked(PreferenceUtils.isVibrateEnabled(this));
        }
        else {
            getSupportActionBar().setTitle("");
            Bundle extras = getIntent().getExtras();
            taskId  = extras.getInt("id");

            Cursor c1 =  dbHelper.fetchTask(taskId);

            task.setTaskId(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_ID)));
            task.setTitle(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
            task.setDateAndTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE_AND_TIME)));
            task.setDate(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE)));
            task.setTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TIME)));
            task.setTaskDone(c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.TASK_DONE)));
            task.setDateInMilliSeconds(c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS)));
            task.setTaskHour(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_HOUR)));
            task.setTaskMinute(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_MINUTE)));
            task.setVibrateEnabled(c1.getInt(c1.getColumnIndex(TaskEntry.NOTIFICATION_VIBRATE)) == 1);
            task.setSoundEnabled(c1.getInt(c1.getColumnIndex(TaskEntry.NOTIFICATION_SOUND_ENABLED)) == 1);
            task.setNotificationSound(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.NOTIFICATION_SOUND)));

            mTitleText.setText(task.getTitle());
            boolean done = task.getTaskDone() == 1;
            dateInMillis = task.getDateInMilliSeconds();
            taskHour = task.getTaskHour();
            taskMinute = task.getTaskMinute();
            dateText = task.getDate();
            timeText = task.getTime();
            notificationSound = task.getNotificationSound();

            mDateText.setText(dateText);
            mTimeText.setText(timeText);
            mTaskDone.setChecked(done);
            mSoundCheckBox.setChecked(task.isSoundEnabled());
            mVibrateSwitch.setChecked(task.isVibrateEnabled());

            //Log.d(TAG,"Title " +extras.getString("title") + " Date " + extras.getString("date") +" time "+timeText);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dateInMillis);

            mCalendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
            mCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
            mCalendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
            mCalendar.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            mCalendar.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));

            if (dateText.matches("")) {
                showHideButtons(mClearDate, false);
                mTimeLayout.setVisibility(View.GONE);
            }
            else {
                showHideButtons(mClearDate, true);
                checkIfDatePassed(dateInMillis,dateText);
                mTimeText.setVisibility(View.VISIBLE);
                if (timeText.matches("")) {
                    showHideButtons(mClearTime, false);
                }
                else{
                    showHideButtons(mClearTime, true);
                    checkIfTimePassed(taskHour,taskMinute);
                }
            }
        }

        mChooseSoundButton.setEnabled(mSoundCheckBox.isChecked());
        if(mSoundCheckBox.isChecked()){
            mChooseSoundButton.setTextColor(getResources().getColor(R.color.black));
        }
        else{
            mChooseSoundButton.setTextColor(getResources().getColor(R.color.grey));
        }

        mTitleText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // mTitleText.setFocusable(true);
            }
        });

        mDateText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskOperation.hideKeyboard(NewTask.this);
                showDatePickerDialog();
            }
        });

        mTimeText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskOperation.hideKeyboard(NewTask.this);
                showTimePickerDialog();
            }
        });

        mTaskDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskOperation.hideKeyboard(NewTask.this);
                String stringDone = getResources().getString(R.string.task_finished_excl);
                String stringNotDone = getResources().getString(R.string.task_finished_ques);
                if (mTaskDone.isChecked()) {
                    mTaskStatus.setText(stringDone);
                    mTaskStatus.setTypeface(null, Typeface.BOLD);
                    mTaskStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    resetTextColors();
                } else {
                    mTaskStatus.setText(stringNotDone);
                    mTaskStatus.setTypeface(null, Typeface.NORMAL);
                    mTaskStatus.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
                    checkIfDatePassed(dateInMillis,dateText);
                    checkIfTimePassed(taskHour,taskMinute);
                }
            }
        });

        mClearDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideButtons(mClearDate, false);
                showHideButtons(mClearTime, false);
                mTimeLayout.setVisibility(View.GONE);
                resetTexts();
                resetTextColors();
            }
        });

        mClearTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // mClearTime.setVisibility(View.GONE);
                showHideButtons(mClearTime, false);
                resetTimeText();
                if(!taskOperation.isDatePassed(dateInMillis)){
                    mDateText.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
                }
            }
        });
    }

    @OnClick(R.id.enableSound_cb)
    void enableChangeSoundButton(){
        mChooseSoundButton.setEnabled(mSoundCheckBox.isChecked());
        if(mSoundCheckBox.isChecked()){
            mChooseSoundButton.setTextColor(getResources().getColor(R.color.black));
        }
        else{
            mChooseSoundButton.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    @OnClick(R.id.choose_alarm_sound)
    void chooseAlarmSound(){
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        Uri existingSound =  Uri.parse(PreferenceUtils.getNotificationSound(this));
        if (!newTask) {
            existingSound =  Uri.parse(notificationSound);
        }
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,existingSound);
        this.startActivityForResult(intent, 5);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        String chosenRingtone = "";
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                chosenRingtone = uri.toString();
                notificationSound = chosenRingtone;
            }
            else {
                chosenRingtone = null;
            }
        }
        Log.d(TAG,"chosenRingtone "+chosenRingtone);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       // Log.d(TAG, "newTask " + newTask);
        if (newTask) {
            menu.findItem(R.id.delete_task).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_task) {
            saveTodo();
            return true;
        } else if (id == R.id.delete_task) {
            // Create a new AlertDialogFragment
            mDialog = AlertDialogFragment.newInstance();
            // Show AlertDialogFragment
            mDialog.show(getFragmentManager(), "Alert");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveTodo() {
        String todoTitle = mTitleText.getText().toString().trim();
        String todoDate;
        String todoTime;
        String todoDateAndTime;
        int todoFinished;
        int vibrateEnabled = 0;
        int soundEnabled = 0;

        //Log.d(TAG,"time "+todoTitle);
        if (!TextUtils.isEmpty(todoTitle)) {
            if(dateText!= null){
                todoDate = dateText;
            }
            else{
                todoDate = "";
            }

            todoTime = mTimeText.getText().toString();
            if (!TextUtils.isEmpty(todoTime)) {
                todoDateAndTime = todoDate + ", " + todoTime;
            } else {
                todoDateAndTime = todoDate;
            }
            if (mTaskDone.isChecked()) {
                todoFinished = 1;
            } else {
                todoFinished = 0;
            }
            //Toast.makeText(getApplicationContext(),todoDateAndTime,Toast.LENGTH_LONG).show();
           //  Log.d(TAG,"time "+todoDateAndTime +" "+dateInMillis);
            // Log.d(TAG, "date & time "+ todoTitle + todoDate + " " + todoTime);
           //  Log.d(TAG, "taskHour " + taskHour + " taskMinute " + taskMinute);

            if(mVibrateSwitch.isChecked()){
                vibrateEnabled = 1;
            }
            if(mSoundCheckBox.isChecked()){
                soundEnabled = 1;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TaskEntry.TASK_TITLE, todoTitle);
            values.put(TaskEntry.TASK_DATE, todoDate);
            values.put(TaskEntry.TASK_TIME, todoTime);
            values.put(TaskEntry.TASK_DATE_AND_TIME, todoDateAndTime);
            values.put(TaskEntry.TASK_DONE, todoFinished);
            values.put(TaskEntry.TASK_DATE_IN_MS, dateInMillis);
            values.put(TaskEntry.TASK_HOUR, taskHour);
            values.put(TaskEntry.TASK_MINUTE, taskMinute);
            values.put(TaskEntry.NOTIFICATION_SOUND, notificationSound);
            values.put(TaskEntry.NOTIFICATION_VIBRATE, vibrateEnabled);
            values.put(TaskEntry.NOTIFICATION_SOUND_ENABLED, soundEnabled);

            long rowId = taskId;
            if (newTask) {
                rowId = dbHelper.insertTask(values);
            } else {
                dbHelper.updateTask(taskId, values);
            }

            Calendar now = Calendar.getInstance();

            //TaskOperation.showDebugToast(this,mCalendar.getTimeInMillis() + " " +now.getTimeInMillis());
            Log.d(TAG,"mCalendar "+mCalendar.getTimeInMillis() + " now " + now.getTimeInMillis());
            //Log.d(TAG,"newTask "+newTask);
            //Log.d(TAG,"dateText "+dateText);
            Log.d(TAG,"dateInMillis "+dateInMillis + " " + now.getTimeInMillis());

            if (!dateText.matches("") ) {
                //Log.d(TAG,"dateText not empty ");
                if (newTask && mCalendar.getTimeInMillis() > now.getTimeInMillis() ) {
                    ReminderManager.scheduleReminder(mCalendar,this,rowId,todoTitle);
                }
                else if (!newTask) {
                    Log.d(TAG,"else  ");
                    if(todoFinished==0  && (dateInMillis > now.getTimeInMillis() )){
                        Log.d(TAG,"in  ");
                        ReminderManager.cancelReminder(this,taskId);
                        ReminderManager.scheduleReminder(mCalendar,this,taskId,todoTitle);
                        //Log.d(TAG,"cancelled and scheduled ");
                    }
                    else if(todoFinished==1){
                        ReminderManager.cancelReminder(this,taskId);
                    }
                }
            }
            else{
               // TaskOperation.showDebugToast(this,"reminder not set");
            }

            db.close();

            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
            finish();

        } else {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for specific milliseconds
            v.vibrate(100);
            if(toastobject!= null){
                toastobject.cancel();
            }
            toastobject = Toast.makeText(getApplicationContext(), getResources().getString(R.string.task_alert), Toast.LENGTH_SHORT);
            toastobject.show();
        }
    }

    public void continueDelete(Boolean delete) {
        if(delete) {
            String todoTitle = mTitleText.getText().toString();
            int deleteCount = dbHelper.deleteTask(taskId);
           // Log.i(TAG, "task deleted " + deleteCount);
            if(toastobject!= null){
                toastobject.cancel();
            }
            toastobject = Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.task_deleted), Toast.LENGTH_SHORT);
            toastobject.show();
            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
            ReminderManager.cancelReminder(this,taskId);
            finish();
        }
        else{
            mDialog.dismiss();
        }
    }

    public static void showHideButtons(ImageButton imageButton, Boolean show) {
        if (show) {
            imageButton.setVisibility(View.VISIBLE);
        } else {
            imageButton.setVisibility(View.GONE);
        }
    }

    public void resetTexts(){
        mDateText.setText("");
        dateText = "";
        dateInMillis = 0;
        resetTimeText();
    }

    public void resetTimeText(){
        mTimeText.setText("");
        timeText = "";
        taskHour = -1;
        taskMinute = -1;
    }

    public void resetTextColors(){
        mDateText.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
        mTimeText.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
    }

    public static void checkIfDatePassed(long date, String dateString) {
        String result = taskOperation.checkDates(dateInMillis);
        if (result.equals("")) {
            mDateText.setText(dateString);
        } else {
            mDateText.setText(result);
        }
        if(!mTaskDone.isChecked()) {
            if (taskOperation.isDatePassed(date)) {
               // Log.d(TAG,"isDatePassed ");
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.red));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.red));
            } else {
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.black));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.black));
            }
        }
    }

    public static void checkIfTimePassed(int selectedHour, int selectedMinute) {
        if(!mTaskDone.isChecked() && DateUtils.isToday(dateInMillis)){
           // Log.d(TAG,"isTimePassed "+selectedHour +" "+selectedMinute);
            if(taskOperation.isTimePassed(selectedHour,selectedMinute)){
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.red));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.red));
            }
            else {
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.black));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.black));
            }
        }
    }


    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog() {
        //Log.d(TAG,"showTimePickerDialog");
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    // DialogFragment used to pick a ToDoItem deadline date

    public static class DatePickerFragment extends DialogFragment implements
            OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current date as the default date in the picker

            final Calendar c = Calendar.getInstance();

           // Log.d(TAG,"DatePickerFragment");

            if(!TextUtils.isEmpty(mDateText.getText().toString())){
                c.setTimeInMillis(dateInMillis);
            }

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int dayName = c.get(Calendar.DAY_OF_WEEK);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth,
                              int selectedDay) {

            Calendar cal = Calendar.getInstance();

            mCalendar.set(Calendar.YEAR, selectedYear);
            mCalendar.set(Calendar.MONTH, selectedMonth);
            mCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);

            mCalendar.set(Calendar.HOUR_OF_DAY, PreferenceUtils.getHour(getActivity()));
            mCalendar.set(Calendar.MINUTE, PreferenceUtils.getMinute(getActivity()));

            cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
            cal.set(Calendar.MONTH, datePicker.getMonth());
            cal.set(Calendar.YEAR, datePicker.getYear());

            SimpleDateFormat day_date = new SimpleDateFormat("EEE");
            SimpleDateFormat month_date = new SimpleDateFormat("MMM");
            String dayName = day_date.format(cal.getTime());
            String monthName = month_date.format(cal.getTime());
           // Log.d(TAG,"month_name "+	monthName +" day_name "+dayName);

            //Log.d(TAG,"dateInMillis "+	dateInMillis+" "+ Calendar.getInstance().getTimeInMillis());

           /* Log.d(TAG,"result "+result);
            Log.d(TAG,"isPassed "+taskOperation.isPassed(dateInMillis));
            Log.d(TAG,"checkIfNextWeek "+taskOperation.checkIfNextWeek(dateInMillis));
            Log.d(TAG,"checkIfNextMonth "+taskOperation.checkIfNextMonth(dateInMillis));*/

            dateInMillis = cal.getTimeInMillis();

            dateText = setDateString(selectedYear, monthName, selectedDay, dayName);

            checkIfDatePassed(dateInMillis,dateText);

            mTimeLayout.setVisibility(View.VISIBLE);
            mTimeText.setVisibility(View.VISIBLE);

            showHideButtons(mClearDate, true);

        }
    }

    private static String setDateString(int year, String monthOfYear, int dayOfMonth, String dayName) {

        // Increment monthOfYear for Calendar/Date -> Time Format setting
        String mon = "" + monthOfYear;
        String day = "" + dayOfMonth;

        if (dayOfMonth < 10)
            day = "0" + dayOfMonth;

        return dayName + ", " + mon + " " + day + ", " + year;
    }


    public static class TimePickerFragment extends DialogFragment
            implements OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute;

           // Log.d(TAG," mTimeText "+hour);
          //  showDebugToast(String.valueOf(hour));
            if(TextUtils.isEmpty(mTimeText.getText().toString())){
                minute = 0;
                if(DateUtils.isToday(dateInMillis)){
                    if(hour >= 22){
                        hour = 23;
                    }
                    else{
                        hour = hour + 2;
                    }
                }
                else{
                    hour = 12;
                }
            }
            else{
                hour = taskHour;
                minute = taskMinute;
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, false
                    /*DateFormat.is24HourFormat(getActivity())*/);
        }

        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {

            mCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            mCalendar.set(Calendar.MINUTE, selectedMinute);

            dateInMillis = mCalendar.getTimeInMillis();

            int hour = selectedHour;
            int minutes = selectedMinute;
            String timeSet;
            if (hour > 12) {
                hour -= 12;
                timeSet = "PM";
            } else if (hour == 0) {
                hour += 12;
                timeSet = "AM";
            } else if (hour == 12){
                timeSet = "PM";
            }else{
                timeSet = "AM";
            }

            String min;
            if (minutes < 10)
                min = "0" + minutes ;
            else
                min = String.valueOf(minutes);

            String timeString;
            //timeString = setTimeString(hourOfDay, minute);
            timeString = hour +":"+min +" "+timeSet;
            mTimeText.setText(timeString);
            timeText = timeString;
            showHideButtons(mClearTime, true);

            taskHour = selectedHour;
            taskMinute = selectedMinute;

           // Log.d(TAG,"timeString "+timeString);

          //  Log.d(TAG," selectedHour "+selectedHour + " selectedMinute "+selectedMinute);
           // Log.d(TAG,"isPassed "+taskOperation.isPassed(dateInMillis));

            checkIfTimePassed(taskHour,taskMinute);
        }
    }

    private static String setTimeString(int hourOfDay, int minute) {

        String min = "" + minute;
        String hour = "" + hourOfDay;

        if (minute < 10)
            min = "0" + minute;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        String amOrPm;
        c = Calendar.getInstance();
        int am_pm = c.get(Calendar.AM_PM);
        if (am_pm == Calendar.AM) {
            amOrPm = "AM";
        } else {
            amOrPm = "PM";
        }
        return hour + ":" + min + " " + amOrPm;
    }


}
