package com.ms.favtodo.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TimePicker;

import com.ms.favtodo.R;
import com.ms.favtodo.activity.NewTask.TimePickerFragment;
import com.ms.favtodo.utils.PreferenceUtils;
import com.ms.favtodo.utils.TaskOperation;

import java.util.Calendar;

/**
 * Created by MOHAMED SULAIMAN on 16-09-2017.
 */

public class PreferencesFragment  extends PreferenceFragmentCompat {

    private static Preference prefDueTime;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        Context context = getActivity();

        String timeString = PreferenceUtils.getDueTime12HrFormat(context);

        prefDueTime = findPreference(getString(R.string.pref_due_time_key));

        prefDueTime.setSummary(getString(R.string.pref_due_time_summary) + " "+timeString);

        prefDueTime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePickerDialog();
                return false;
            }
        });
    }

    public void showTimePickerDialog() {
       // Log.d(TAG,"showTimePickerDialog");
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment
            implements OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int hour = PreferenceUtils.getHour(getActivity());
            int minute = PreferenceUtils.getMinute(getActivity());

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, false);
        }

        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {

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

            String min = "";
            if (minutes < 10)
                min = "0" + minutes ;
            else
                min = String.valueOf(minutes);

            String timeString;

            if(minutes!=0){
                timeString = hour +":"+min +" "+timeSet;
            }
            else{
                timeString = hour +" "+timeSet;
            }

            TaskOperation.showDebugToast(getActivity(),timeString);
            prefDueTime.setSummary(getString(R.string.pref_due_time_summary) + " "+timeString);

            PreferenceUtils.updateDueTimeValue(getActivity(),timeString,selectedHour + ":"+ selectedMinute);
        }
    }


}
