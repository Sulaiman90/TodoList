package com.ms.favtodo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import com.ms.favtodo.activity.AlertActivity;

public class PhoneStateReceiver extends BroadcastReceiver {

    public static final String PHONE_STATE = "phone_state";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(context != null && intent != null && intent.getAction() != null){

            if(intent.getAction().equals("android.intent.action.PHONE_STATE")){

                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

                Intent alertActivityIntent = new Intent(AlertActivity.PHONE_STATE_BROADCAST);
                alertActivityIntent.putExtra(PHONE_STATE, state);

                LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);

                if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                    //Toast.makeText(context,"Ringing State ",Toast.LENGTH_SHORT).show();
                    mLocalBroadcastManager.sendBroadcast(alertActivityIntent);
                }
                /*if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
                    //Toast.makeText(context,"Received State",Toast.LENGTH_SHORT).show();
                }
                if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                    //Toast.makeText(context,"Idle State",Toast.LENGTH_SHORT).show();
                }*/
            }
        }
    }
}
