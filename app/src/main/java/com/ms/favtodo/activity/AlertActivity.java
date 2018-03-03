package com.ms.favtodo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.favtodo.R;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.receiver.PhoneStateReceiver;
import com.ms.favtodo.sync.ReminderTasks;
import com.ms.favtodo.sync.TaskReminderIntentService;
import com.ms.favtodo.utils.NotificationUtils;
import com.ms.favtodo.utils.PreferenceUtils;
import com.ms.favtodo.utils.ReminderManager;
import com.ms.favtodo.utils.TaskOperation;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlertActivity extends AppCompatActivity {

    private static final String TAG = AlertActivity.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;
    private boolean mVibrate;
    private final long[] mVibratePattern = { 0, 500, 500 ,500, 500 };
    private Button mDismissButton;
    private boolean mSoundOn = true;
    private Timer mTimer = null;
    private PlayTimerTask mTimerTask;
    private long mPlayTime;
    private boolean showNotification = true;
    private long taskId;
    private boolean mSnooze = true;
    private TaskDbHelper dbHelper;
    private PowerManager powerManager;
    private LocalBroadcastManager mLocalBroadcastManager;
    private String phoneState = "";

    public static final String PHONE_STATE_BROADCAST = "phone_state_broadcast";

    @BindView(R.id.iv_alert_silence) ImageView mIvSilence;
    @BindView(R.id.tv_alert_time) TextView mTvDateAndTime;
    @BindView(R.id.tv_alert_title) TextView mTvTaskTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        ButterKnife.bind(this);

        Window window = this.getWindow();

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        FrameLayout statusbar = findViewById(R.id.statusbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }
        else {
            statusbar.setVisibility(View.VISIBLE);
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        mDismissButton = findViewById(R.id.btn_dismiss);

        mDismissButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification = false;
                finish();
            }
        });

        dbHelper = new TaskDbHelper(this);

        mPlayTime = PreferenceUtils.getAlertDuration(this) * 1000;

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        start(getIntent());
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra(PhoneStateReceiver.PHONE_STATE))){
                mSnooze = false;
                finish();
            }
        }
    };


    @Override
    protected void onResume() {
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(PHONE_STATE_BROADCAST));
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy "+mSnooze +" showNotification "+showNotification);
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
        cleanup();
        if(showNotification){
            addNotification();
            if(PreferenceUtils.isAutoSnoozeEnabled(this)) {
                if (mSnooze) {
                    snoozeAlarm();
                }
            }
        }
        else {
            ReminderManager.cancelNotification(this, taskId);
        }
    }

    @Override
    protected void onPause() {
        // Log.d(TAG, "onPause");
        super.onPause();
        finish();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       // Log.d(TAG, " onNewIntent ");
    }

    private void start(Intent intent){
        Log.d(TAG, " start ");

        Bundle b = intent.getExtras();
        boolean playSound = false;
        if (b != null) {
            taskId = b.getLong(NewTask.TASK_ID);
            playSound = b.getBoolean(NewTask.PLAY_SOUND);
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager != null && telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            addNotification();
            mSnooze = false;
            finish();
            return;
        }

        mSnooze = playSound;

        boolean isScreenOn;

        if(Build.VERSION.SDK_INT >= 20) {
            isScreenOn = powerManager.isInteractive();
        }
        else{
            isScreenOn = powerManager.isScreenOn();
        }

        if(!isScreenOn){
            addNotification();
            return;
           /* WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyWakelockTag");
            wakeLock.acquire(100);*/
        }

        //Log.d(TAG, " taskId "+taskId + " mSnooze "+mSnooze);

        Cursor c1 =  dbHelper.fetchTask(taskId);

        String date = c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE));
        String time = c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TIME));
        String title =  c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE));

        String dateTime = String.format(getResources().getString(R.string.dateAndTime),date, time);

        mTvDateAndTime.setText(dateTime);
        mTvTaskTitle.setText(title);

        if(!mSnooze){
            mIvSilence.setVisibility(View.INVISIBLE);
            ReminderManager.cancelReminder(this, taskId);
           // Log.d(TAG,"cancelReminder");
            if (mTimer != null) {
               // Log.d(TAG,"timer cancelled");
                mTimer.cancel();
            }
            return;
        }

        boolean soundEnabled = c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.NOTIFICATION_SOUND_ENABLED)) == 1;

        mVibrate = c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.NOTIFICATION_VIBRATE)) == 1;

        //Log.d(TAG, "soundEnabled "+soundEnabled +" mVibrate "+mVibrate);

        if(!soundEnabled && !mVibrate){
            mIvSilence.setVisibility(View.INVISIBLE);
        }

        if (mVibrate){
            mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            startVibrate();
        }

        if(soundEnabled){
            String notificationSound = c1.getString(c1.getColumnIndexOrThrow(TaskEntry.NOTIFICATION_SOUND));
            startPlayingSound(notificationSound);
        }

        mTimerTask = new PlayTimerTask();
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, mPlayTime);
    }

    @OnClick(R.id.iv_alert_silence)
    public void silenceBtnClick(){
        mSoundOn = !mSoundOn;
        if(mSoundOn){
            mIvSilence.setImageResource(R.drawable.ic_mute_off);
            resumeSound();
        }
        else{
            mIvSilence.setImageResource(R.drawable.ic_mute_on);
            pauseSound();
        }
    }

    @OnClick(R.id.btn_complete)
    public void taskCompleted(){
        Intent launchIntent = new Intent(this, TaskReminderIntentService.class);
        launchIntent.setAction(ReminderTasks.ACTION_TASK_COMPLETED);
        launchIntent.putExtra(NewTask.TASK_ID,taskId);
        startService(launchIntent);
       // Log.d(TAG, "taskCompleted");
        showNotification = false;
        finish();
    }

    @OnClick(R.id.btn_edit)
    public void editTask(){
        showNotification = false;
        Intent intent = new Intent(this, NewTask.class);
        Bundle bundle = new Bundle();
        bundle.putInt(NewTask.TASK_ID,(int) taskId);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.btn_snooze)
    public void snoozeTask() {
        showNotification = false;
        mSnooze = true;
        snoozeAlarm();
        finish();
    }

    private void snoozeAlarm(){
        int snoozeMinutes = PreferenceUtils.getAutoSnoozeInterval(this);

        Log.d(TAG, "snoozeAlarm:snoozeMinutes "+snoozeMinutes);

        final long snoozeTime = System.currentTimeMillis() + (1000 * 60 * snoozeMinutes);

        final Calendar when = Calendar.getInstance();
        when.setTimeInMillis(snoozeTime);

        int hour = when.get(Calendar.HOUR_OF_DAY);
        int minute = when.get(Calendar.MINUTE);

        String timeString = TaskOperation.generateTime(hour,minute);

       // Log.d(TAG, "hour "+hour +" minute "+minute);
        Toast.makeText(this, "Remind you again at " + timeString,Toast.LENGTH_SHORT).show();

        ReminderManager.scheduleReminder(when, this, taskId);
    }


    private void startVibrate(){
        mVibrator.vibrate(mVibratePattern,0);
    }

    private void startPlayingSound(String uriString){
        Uri soundUri = Uri.parse(uriString);

        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
            if(PreferenceUtils.isRepeatAlertToneEnabled(this)){
                mMediaPlayer.setLooping(true);
            }
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                   // Log.d(TAG, "onPrepared");
                    mp.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    cleanup();
                }
            });
        }
        try{
            mMediaPlayer.setDataSource(this,soundUri);
            mMediaPlayer.prepareAsync();
        } catch (Exception  e) {
            e.printStackTrace();
        }
    }

    private void pauseSound() {
        if(mVibrator != null){
            mVibrator.cancel();
        }
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    private void resumeSound() {
        if(mVibrator != null && mVibrate){
            mVibrator.vibrate(mVibratePattern,0);
        }
        if(mMediaPlayer != null){
            mMediaPlayer.start();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void cleanup() {
        //Log.d(TAG, "cleanup");
        if(mVibrator != null){
            mVibrator.cancel();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void addNotification(){
        NotificationUtils.createNotification(this,taskId);
    }

    private class PlayTimerTask extends TimerTask{

        @Override
        public void run() {
            Log.d(TAG, "run");
            addNotification();
            finish();
        }
    }
}
