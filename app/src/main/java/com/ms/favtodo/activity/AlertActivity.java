package com.ms.favtodo.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;

import com.ms.favtodo.R;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;

import java.io.IOException;

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
    @BindView(R.id.iv_alert_silence) ImageView mIvSilence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        ButterKnife.bind(this);

        Window window = this.getWindow();

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        mDismissButton = findViewById(R.id.btn_dismiss);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        long rowId = 0;
        if (b != null) {
            rowId = b.getLong("TaskRowId");
        }

        TaskDbHelper dbHelper = new TaskDbHelper(this);
        Cursor c1 =  dbHelper.fetchTask(rowId);

        String notificationSound;
        notificationSound = c1.getString(c1.getColumnIndexOrThrow(TaskEntry.NOTIFICATION_SOUND));

        if (c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.NOTIFICATION_VIBRATE)) == 1)
            mVibrate = true;
        else mVibrate = false;

        Log.d(TAG, notificationSound +" mVibrate "+mVibrate);
        if (mVibrate)
            mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        startSound(notificationSound);

        mDismissButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanup();
                finish();
            }
        });
    }

    @OnClick(R.id.iv_alert_silence)
    public void silenceBtnClick(){
        stopSound();
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void startSound(String uriString){
        Uri soundUri = Uri.parse(uriString);

        if (mVibrate)
            mVibrator.vibrate(mVibratePattern,0);

        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
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

    private void stopSound() {
        if(mVibrator != null){
            mVibrator.cancel();
        }
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            //mMediaPlayer.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void cleanup() {
        if(mVibrator != null){
            mVibrator.cancel();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
