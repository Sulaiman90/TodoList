package com.ms.favtodo.preference;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ms.favtodo.R;
import com.ms.favtodo.activity.AlertActivity;

// Based on a Stack Overflow example: http://stackoverflow.com/questions/1974193/slider-on-my-preferencescreen
public class SeekBarPreference extends DialogPreference
{
    private static final String TAG = AlertActivity.class.getSimpleName();

    private static final String SCHEMA_URL = "http://schemas.android.com/apk/res/android";

    private static final String SCHEMA_URL_2 = "http://schemas.android.com/apk/res-auto";

    private SeekBar seekBar;
    private TextView valueText;
    private final Context context;

    private final String dialogMessage;
    private final String suffix;
    private final int defaultValue;
    private final int maxValue;
    private final int minValue;
    private int currentValue;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        // Read the message from XML
        int dialogMessageId = attrs.getAttributeResourceValue(SCHEMA_URL, "dialogMessage", 0);
        if (dialogMessageId == 0) {
            dialogMessage = attrs.getAttributeValue(SCHEMA_URL, "dialogMessage");
        }
        else {
            dialogMessage = context.getString(dialogMessageId);
        }

        // Get the suffix for the number displayed in the dialog
        int suffixId = attrs.getAttributeResourceValue(SCHEMA_URL, "text", 0);
        if (suffixId == 0) {
            suffix = attrs.getAttributeValue(SCHEMA_URL, "text");
        }
        else {
            suffix = context.getString(suffixId);
        }

        int maxValueId = attrs.getAttributeResourceValue(SCHEMA_URL, "max", 0);
        if (maxValueId == 0) {
            maxValue = attrs.getAttributeIntValue(SCHEMA_URL, "max", 100);
        }
        else {
            maxValue = Integer.parseInt(context.getString(maxValueId));
        }

        int minValueId = attrs.getAttributeResourceValue(SCHEMA_URL_2, "min", 0);
        minValue = Integer.parseInt(context.getString(minValueId));

        int defaultValueId = attrs.getAttributeResourceValue(SCHEMA_URL, "defaultValue", 0);
        defaultValue = Integer.parseInt(context.getString(defaultValueId));

       // Get default, min, and max seekbar values
       /* defaultValue = attrs.getAttributeIntValue(SCHEMA_URL, "defaultValue",
                context.getResources().getInteger(R.integer.alarm_duration_default));*/
        //Log.d(TAG,"maxValue "+maxValue + " minValue  "+minValue+ " defaultValue  "+defaultValue);
    }

    @Override
    protected View onCreateDialogView() {

        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        TextView splashText = new TextView(context);
        splashText.setPadding(30, 10, 30, 10);
        if (dialogMessage != null) {
            splashText.setText(dialogMessage);
        }
        layout.addView(splashText);

        valueText = new TextView(context);
        valueText.setGravity(Gravity.CENTER_HORIZONTAL);
        valueText.setTextSize(32);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(valueText, params);

        seekBar = new SeekBar(context);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                if (value < minValue) {
                    seekBar.setProgress(minValue);
                    return;
                }

                String t = String.valueOf(value);
                String suffixStr = suffix;
                if(value > 1){
                    suffixStr = suffix+"s";
                }
                valueText.setText(suffix == null ? t : t.concat(suffix.length() > 1 ? " "+suffixStr : suffixStr));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        layout.addView(seekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (shouldPersist()) {
            currentValue = getPersistedInt(defaultValue);
        }

        seekBar.setMax(maxValue);
        seekBar.setProgress(currentValue);

        return layout;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        seekBar.setMax(maxValue);
        seekBar.setProgress(currentValue);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue)
    {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            currentValue = shouldPersist() ? getPersistedInt(this.defaultValue) : 0;
        }
        else {
            currentValue = (Integer) defaultValue;
        }
    }

    public void setProgress(int progress) {
        this.currentValue = progress;
        if (seekBar != null) {
            seekBar.setProgress(progress);
        }
    }
    public int getProgress() {
        return currentValue;
    }

    @Override
    public void showDialog(Bundle state) {
        super.showDialog(state);

        Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shouldPersist()) {
                    currentValue = seekBar.getProgress();
                    persistInt(seekBar.getProgress());
                    callChangeListener(seekBar.getProgress());
                }

                getDialog().dismiss();
            }
        });
    }
}