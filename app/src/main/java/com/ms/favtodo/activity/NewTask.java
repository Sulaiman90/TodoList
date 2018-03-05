package com.ms.favtodo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ms.favtodo.R;
import com.ms.favtodo.TodoList;
import com.ms.favtodo.db.TaskContract;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.dialog.AlertDialogFragment;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.utils.DateUtility;
import com.ms.favtodo.utils.PreferenceUtils;
import com.ms.favtodo.utils.ReminderManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    private Boolean newTask = false;

    private Toast toastObject;

    private DialogFragment mDialog;

    private static long dateInMillis = 0;
    public static int taskHour = -1;
    public static int taskMinute = -1;

    private static String dateText = "";
    private static String timeText = "";
    private int taskId = 0;

    private static Calendar mCalendar;

    private static LinearLayout mTimeLayout;
    private static LinearLayout mRepeatLayout;
    private static RelativeLayout mAlertSoundLayout;
    private String notificationSound = "";

    int mRepeatSpinnerValue = 0;

    public static String TASK_ID = "taskId";
    public static String PLAY_SOUND = "playSound";



    @BindView(R.id.enableVibrate_switch) Switch mVibrateSwitch;
    @BindView(R.id.enableSound_cb) CheckBox mSoundCheckBox;
    @BindView(R.id.choose_alarm_sound) Button mChooseSoundButton;

    @BindView(R.id.spinner_repeat) Spinner mRepeatSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        ButterKnife.bind(this);

        dateInMillis = 0;
        dateText = "";
        timeText = "";

        dbHelper = new TaskDbHelper(this);
        TaskDetails task = new TaskDetails();
        mCalendar = Calendar.getInstance();

        mTitleText =  findViewById(R.id.title);
        mDateText =  findViewById(R.id.dateText);
        mTimeText = findViewById(R.id.timeText);
        mTaskStatus =  findViewById(R.id.task_status);
        mTaskDone = findViewById(R.id.task_finished);

        mClearDate = findViewById(R.id.clear_date);
        mClearTime = findViewById(R.id.clear_time);

        mTimeLayout = findViewById(R.id.timeLinearLayout);
        mRepeatLayout = findViewById(R.id.layout_repeat);
        mAlertSoundLayout = findViewById(R.id.rl_sound_alert);

        LinearLayout linearLayout = findViewById(R.id.mark_as_done);

        Intent intent = getIntent();
        newTask = intent.getBooleanExtra("NewTask", false);

        mClearDate.setVisibility(View.GONE);
        mClearTime.setVisibility(View.GONE);
        mRepeatLayout.setVisibility(View.GONE);
        //Log.d(TAG,"New Task "+newTask);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_spinner_items,R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);

        mRepeatSpinner.setAdapter(adapter);

        if (newTask) {
            linearLayout.setVisibility(View.GONE);
            mTimeLayout.setVisibility(View.GONE);
            mAlertSoundLayout.setVisibility(View.GONE);
            taskHour = -1;
            taskMinute = -1;
            notificationSound = PreferenceUtils.getNotificationSound(this);
            mSoundCheckBox.setChecked(PreferenceUtils.isSoundEnabled(this));
            mVibrateSwitch.setChecked(PreferenceUtils.isVibrateEnabled(this));
        }
        else {
            if(getSupportActionBar() != null){
                getSupportActionBar().setTitle("");
            }
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                taskId  = extras.getInt(NewTask.TASK_ID);
            }
            else{
                return;
            }

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
            mRepeatSpinner.setSelection(c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.TASK_REPEAT)));

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
                mAlertSoundLayout.setVisibility(View.GONE);
            }
            else {
                mRepeatLayout.setVisibility(View.VISIBLE);
                mAlertSoundLayout.setVisibility(View.VISIBLE);
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
                TodoList.hideKeyboard(NewTask.this);
                showDatePickerDialog();
            }
        });

        mTimeText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TodoList.hideKeyboard(NewTask.this);
                showTimePickerDialog();
            }
        });

        mTaskDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TodoList.hideKeyboard(NewTask.this);
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
                mRepeatLayout.setVisibility(View.GONE);
                mAlertSoundLayout.setVisibility(View.GONE);
                mRepeatSpinner.setSelection(0);
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
                if(!DateUtility.isDatePassed(dateInMillis)){
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

        mRepeatSpinnerValue = mRepeatSpinner.getSelectedItemPosition();

        //Log.d(TAG,"time "+todoTitle);
       // Log.d(TAG,"repeatSpinnerPos "+mRepeatSpinnerValue);
        if (!TextUtils.isEmpty(todoTitle)) {
            if(dateText!= null){
                todoDate = dateText;
            }
            else{
                todoDate = "";
            }

            todoTime = mTimeText.getText().toString();

            todoDateAndTime = DateUtility.getDateAndTime(todoDate, todoTime);

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

            ContentValues contentValues = new ContentValues();

            contentValues.put(TaskEntry.TASK_TITLE, todoTitle);
            contentValues.put(TaskEntry.TASK_DATE, todoDate);
            contentValues.put(TaskEntry.TASK_TIME, todoTime);
            contentValues.put(TaskEntry.TASK_DATE_AND_TIME, todoDateAndTime);
            contentValues.put(TaskEntry.TASK_DONE, todoFinished);
            contentValues.put(TaskEntry.TASK_DATE_IN_MS, dateInMillis);
            contentValues.put(TaskEntry.TASK_HOUR, taskHour);
            contentValues.put(TaskEntry.TASK_MINUTE, taskMinute);
            contentValues.put(TaskEntry.NOTIFICATION_SOUND, notificationSound);
            contentValues.put(TaskEntry.NOTIFICATION_VIBRATE, vibrateEnabled);
            contentValues.put(TaskEntry.NOTIFICATION_SOUND_ENABLED, soundEnabled);
            contentValues.put(TaskContract.TaskEntry.SNOOZE_ON , 1);
            contentValues.put(TaskEntry.TASK_REPEAT, mRepeatSpinnerValue);

            long rowId = taskId;
            if (newTask) {
                rowId = dbHelper.insertTask(contentValues);
            } else {
                dbHelper.updateTask(taskId, contentValues);
            }

            Calendar now = Calendar.getInstance();
            boolean showRepeatTaskAlert = false;

            //TaskOperation.showDebugToast(this,mCalendar.getTimeInMillis() + " " +now.getTimeInMillis());
           // Log.d(TAG,"mCalendar "+mCalendar.getTimeInMillis() + " now " + now.getTimeInMillis());
            //Log.d(TAG,"newTask "+newTask);
            //Log.d(TAG,"dateText "+dateText);
            //Log.d(TAG,"dateInMillis "+dateInMillis + " " + now.getTimeInMillis());

            if (!dateText.matches("") ) {
                //Log.d(TAG,"dateText not empty ");
                if (newTask && mCalendar.getTimeInMillis() > now.getTimeInMillis() ) {
                    ReminderManager.scheduleReminder(mCalendar,this,rowId);
                }
                else if (!newTask) {
                    //Log.d(TAG,"else  ");
                    if(todoFinished==0  && (dateInMillis > now.getTimeInMillis() )){
                      //  Log.d(TAG,"in  ");
                        ReminderManager.scheduleReminder(mCalendar,this,taskId);
                        //Log.d(TAG,"scheduled ");
                    }
                    else if(todoFinished==1){
                        ReminderManager.cancelReminderAndNotification(this,taskId);
                        //Log.d(TAG,"cancelled Reminder ");
                        if(mRepeatSpinnerValue != 0){
                            showRepeatTaskAlert = true;
                            showRepeatAlertDialog();
                        }
                        else{
                            Toast.makeText(this, getString(R.string.task_completed),Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            else if(!newTask && dateText.matches("")){
                ReminderManager.cancelReminderAndNotification(this,taskId);
            }

            if(!showRepeatTaskAlert){
                exitActivity();
            }
        } else {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for specific milliseconds
            if (v != null) {
                v.vibrate(100);
            }
            if(toastObject!= null){
                toastObject.cancel();
            }
            toastObject = Toast.makeText(getApplicationContext(), getResources().getString(R.string.task_alert), Toast.LENGTH_SHORT);
            toastObject.show();
        }
    }

    public void continueDelete(Boolean delete) {
        if(delete) {
            int deleteCount = dbHelper.deleteTask(taskId);
           // Log.i(TAG, "task deleted " + deleteCount);
            if(toastObject!= null){
                toastObject.cancel();
            }
            if(deleteCount > 0){
                toastObject = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.task_deleted), Toast.LENGTH_SHORT);
                toastObject.show();
                ReminderManager.cancelReminderAndNotification(this,taskId);
            }
            exitActivity();
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
        String result = DateUtility.checkDates(dateInMillis, mDateText.getContext());
        if (result.equals("")) {
            mDateText.setText(dateString);
        } else {
            mDateText.setText(result);
        }
        if(!mTaskDone.isChecked()) {
            if (DateUtility.isDatePassed(date)) {
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
            if(DateUtility.isTimePassed(selectedHour,selectedMinute)){
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

    public void showRepeatAlertDialog(){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.repeat_dialog_message);
                //.setTitle(R.string.repeat_dialog_title);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar repeatCal = dbHelper.repeatTask(taskId, mRepeatSpinnerValue);
                if(repeatCal != null){
                    ReminderManager.cancelNotification(NewTask.this,taskId);
                    ReminderManager.scheduleReminder(repeatCal,NewTask.this,taskId);
                }
                exitActivity();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitActivity();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog repeatAlertDialog = builder.create();
        repeatAlertDialog.show();
    }

    public void exitActivity(){
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
    }

    // DialogFragment used to pick a ToDoItem deadline date

    public static class DatePickerFragment extends DialogFragment implements OnDateSetListener {

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
           // int dayName = c.get(Calendar.DAY_OF_WEEK);

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

            SimpleDateFormat day_date = new SimpleDateFormat("EEE", Locale.US);
            SimpleDateFormat month_date = new SimpleDateFormat("MMM", Locale.US);
            String dayName = day_date.format(cal.getTime());
            String monthName = month_date.format(cal.getTime());
           // Log.d(TAG,"month_name "+	monthName +" day_name "+dayName);

            //Log.d(TAG,"dateInMillis "+	dateInMillis+" "+ Calendar.getInstance().getTimeInMillis());

            //Log.d(TAG,"result "+result);

            dateInMillis = cal.getTimeInMillis();

            dateText = DateUtility.setDateString(selectedYear, monthName, selectedDay, dayName);

            checkIfDatePassed(dateInMillis,dateText);

            mTimeLayout.setVisibility(View.VISIBLE);
            mTimeText.setVisibility(View.VISIBLE);
            mRepeatLayout.setVisibility(View.VISIBLE);
            mAlertSoundLayout.setVisibility(View.VISIBLE);
            showHideButtons(mClearDate, true);

        }
    }

    public static class TimePickerFragment extends DialogFragment implements OnTimeSetListener {

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

            String timeString = DateUtility.generateTime(selectedHour,selectedMinute);
            mTimeText.setText(timeString);
            timeText = timeString;
            showHideButtons(mClearTime, true);

            taskHour = selectedHour;
            taskMinute = selectedMinute;

           // Log.d(TAG,"timeString "+timeString);

          //  Log.d(TAG," selectedHour "+selectedHour + " selectedMinute "+selectedMinute);

            checkIfTimePassed(taskHour,taskMinute);
        }
    }
}
