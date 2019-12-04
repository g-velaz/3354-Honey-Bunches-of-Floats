package com.example.smsmessenger;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class Receiver extends BroadcastReceiver {
    public static final String SMS_BUNDLE = "pdus";

    //constructor
    public Receiver() {
    }

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        Toast.makeText(context,"in loop", Toast.LENGTH_SHORT).show();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();

                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";

                Toast.makeText(context,"in loop", Toast.LENGTH_SHORT).show();

            }


            // calling the main activity as an instance
            MainActivity inst = MainActivity.instance();
            inst.updateInbox(smsMessageStr);

            Toast.makeText(context,"called it", Toast.LENGTH_SHORT).show();
        }
    }
}
