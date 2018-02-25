package com.ms.favtodo.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TimePicker;

import com.ms.favtodo.R;
import com.ms.favtodo.preference.SeekBarPreference;
import com.ms.favtodo.utils.PreferenceUtils;
import com.ms.favtodo.utils.TaskOperation;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the VisualizerActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment{

        private static Preference prefDueTime;
        private Context context;
        private static final int TYPE_INT = 1;
        private static final int TYPE_STRING = 2;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            context = getActivity();

            String timeString = PreferenceUtils.getDueTime12HrFormat(context);

            prefDueTime = findPreference(getString(R.string.pref_due_time_key));
            prefDueTime.setSummary(getString(R.string.pref_due_time_summary) + " "+timeString);

            // notification preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_notification_sound_key)), 2);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_alarm_duration_key)), 1);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_snooze_interval_key)), 1);


            prefDueTime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showTimePickerDialog();
                    return true;
                }
            });
        }

        private static void bindPreferenceSummaryToValue(Preference preference, int type) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            if(type == TYPE_INT){
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference ,
                        PreferenceManager
                                .getDefaultSharedPreferences(preference.getContext())
                                .getInt(preference.getKey(), 0));
            }
            else if(type == TYPE_STRING){
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference ,
                        PreferenceManager
                                .getDefaultSharedPreferences(preference.getContext())
                                .getString(preference.getKey(), ""));
            }

        }

        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                String stringValue = newValue.toString();
                if(preference instanceof RingtonePreference){
                    //Log.d(TAG, "RingtonePreference");
                    if(preference.getKey().matches(preference.getContext().getString(R.string.pref_notification_sound_key))){
                        //Log.d(TAG, "pref_notification_sound_key");
                        if (TextUtils.isEmpty(stringValue)) {
                            preference.setSummary(R.string.no_sound);
                        }
                        else {
                            // For ringtone preferences, look up the correct display value
                            // using RingtoneManager.
                            Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                            if (ringtone == null) {
                                // Clear the summary if there was a lookup error.
                                preference.setSummary(R.string.pref_notification_sound_summary);
                            } else {
                                // Set the summary to reflect the new ringtone display name.
                                String name = ringtone.getTitle(preference.getContext());
                                preference.setSummary(name);
                            }
                        }
                    }
                }
                else  if(preference instanceof SeekBarPreference) {
                    if(preference.getKey().matches(preference.getContext().getString(R.string.pref_alarm_duration_key))){
                        int alarmDuration = PreferenceUtils.getAlertDuration(preference.getContext());
                        preference.setSummary(alarmDuration+" secs");
                    }
                    else if(preference.getKey().matches(preference.getContext().getString(R.string.pref_snooze_interval_key))){
                        int snoozeInterval = PreferenceUtils.getAutoSnoozeInterval(preference.getContext());
                        Resources res = preference.getContext().getResources();
                        String mins = res.getQuantityString(R.plurals.snoozeInterval, snoozeInterval, snoozeInterval);
                        preference.setSummary(mins);
                    }
                }
                return true;
            }
        };


        public void showTimePickerDialog() {
            // Log.d(TAG,"showTimePickerDialog");
            DialogFragment newFragment = new SettingsFragment.TimePickerFragment();
            newFragment.show(getActivity().getFragmentManager(), "timePicker");
        }

        public static class TimePickerFragment extends DialogFragment implements OnTimeSetListener {

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

                String min;
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

                //TaskOperation.showDebugToast(getActivity(),timeString);

                if(!timeString.equals(PreferenceUtils.getDueTime12HrFormat(getActivity()))){
                    prefDueTime.setSummary(getString(R.string.pref_due_time_summary) + " "+timeString);
                    PreferenceUtils.updateDueTimeValue(getActivity(),timeString,selectedHour + ":"+ selectedMinute);
                }

            }
        }
    }
}
