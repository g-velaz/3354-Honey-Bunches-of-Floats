package com.example.smsmessenger;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> smsMessagesList = new ArrayList<>(); // to list out messages
    ListView messages;
    ArrayAdapter arrayAdapter; // array to show messages
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST = 1;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;

    EditText input; // text box at the bottom
    SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = findViewById(R.id.messages); // select box where array is going to be displayed
        input = findViewById(R.id.input); // select input text box at the bottom
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList); // initialize array adapter to display text
        messages.setAdapter(arrayAdapter); // put array in messages box
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS(); // get permission if permission is not already granted
        } else {
            refreshSmsInbox(); // if permission granted, update inbox
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadContacts(); // get permission if permission is not already granted
        } else {
            refreshSmsInbox(); // if permission granted, update inbox
        }

        Button refresh = findViewById(R.id.refresh); // refresh button

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSmsInbox();
            }
        }); // refreshes inbox when refresh button pressed

    }


    // Function to format send button and what it will do
    public void onSendClick(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToSendSMS(); // if permission is not already granted
        } else {
            // parse out text
            smsManager.sendTextMessage("+1 2147999923", null, input.getText().toString(), null, null);
            // send confirmation text
            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
            // clear input field
            input.setText("");
        }
    }


    // Refreshing inbox
    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body"); // get text
        int indexAddress = smsInboxCursor.getColumnIndex("address"); // get address
        if (indexBody < 0 || !smsInboxCursor.moveToFirst())
            return; // if no messages
        arrayAdapter.clear(); // clear current adapter so multiple of same message does not appear
        do {
        String str = "SMS From: " + smsInboxCursor.getString(indexAddress) + "\n" + smsInboxCursor.getString(indexBody) + "\n";
        arrayAdapter.add(str); }
        while (smsInboxCursor.moveToNext()); // go through each message and display each one

    }

    // Gets permissions
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    public void getPermissionToReadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }

    // Gets permissions
    public void getPermissionToSendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSIONS_REQUEST);
        }
    }

    // Get permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                refreshSmsInbox();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
