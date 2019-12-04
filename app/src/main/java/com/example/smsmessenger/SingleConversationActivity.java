package com.example.smsmessenger;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaDrm;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SingleConversationActivity extends AppCompatActivity {
    String phoneNum;
    ArrayList<String> smsMessagesList = new ArrayList<>(); // to list out messages
    static ArrayList<String> smsMessagesHistory = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    static ArrayAdapter conversationThread; // array to show messages
    SmsManager smsManager = SmsManager.getDefault();
    EditText phoneNumInput, messageInput;
    Button send;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_conversation);
        phoneNumInput = findViewById(R.id.input);
        messageInput = findViewById(R.id.msgInput);
        String contactName;
        messages = findViewById(R.id.messages); // select box where array is going to be displayed
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList); // initialize array adapter to display text
        conversationThread = new ArrayAdapter(this, android.R.layout.simple_list_item_2, smsMessagesHistory);
        //ImageView newMessageBtn = findViewById(R.id.newMessage);

        messages.setAdapter(arrayAdapter); // put array in messages box
        //TODO: okay so time to fix the toolbar ok so we gottaaaaa
        //TODO: maybe?????? put extras in an intent and transfer that information for when we update but no certain yet
        //CASES: contact name [letters] or number
        //NUMBER
        //NEW TEXT: FIRST put in a number to text, make the toolbar, textinput and send invisible
        final Toolbar toolbar = findViewById(R.id.app_bar);
        send = findViewById(R.id.send);
        send.setVisibility(View.INVISIBLE);
        messageInput.setVisibility(View.INVISIBLE);
        //once the user presses enter, take in the number as the textview, and
        //in the toolbar the button for a temporary refresh that sits in the toolbar section
        //also make edit text invisible
        //enter
        phoneNumInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    phoneNum = phoneNumInput.getText().toString();

                    phoneNumInput.setVisibility(View.INVISIBLE);
                    send.setVisibility(View.VISIBLE);
                    messageInput.setVisibility(View.VISIBLE);
                    setSupportActionBar(toolbar);
                    toolbar.setTitle(phoneNum);
                    refreshSmsInbox();
                }
                return false;
            }
        });

        //Button refresh = findViewById(R.menu.)
       // if(item.getItemId())
        //refresh.setOnClickListener(new View.OnClickListener() {
         //   @Override
          //  public void onClick(View v) {
           //     refreshSmsInbox();
           // }
        //}); // refreshes inbox when refresh button pressed

        //LETTER
        //Take in the contact and get the number from the contact and set it in the textview [for now]
        //The textview will also go into filtering the adapter for the recipient

        //TODO: implement another element into the adapter that lets you know who is sending
            //if you send, then it will be on the right, if you DIDNT send that then it will be on the left
            //but they will share the same adapter section
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_single_conversation, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.refresh:
            refreshSmsInbox();
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    // Refreshing inbox
    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsCursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
        int indexBody = smsCursor.getColumnIndex("body"); // get text
        int indexAddress = smsCursor.getColumnIndex("address"); // get address
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int inboxBody = smsCursor.getColumnIndex("body"); // get text
        int inboxAddress = smsCursor.getColumnIndex("address"); // get address
        if (indexBody < 0 || !smsCursor.moveToFirst())
            return; // if no messages
        //link up the inbox cursor to the all text convo cursor, if they match then they should be matched up and distinct

        arrayAdapter.clear(); // clear current adapter so multiple of same message does not appear
        do
        {
            if(!(inboxBody < 0 || !smsInboxCursor.moveToFirst())) {
                while (!phoneNum.equals(smsInboxCursor.getString(inboxAddress)))
                    smsInboxCursor.moveToNext();
            }

            if((smsInboxCursor.getString(inboxAddress)).equals(smsCursor.getString(indexAddress)) &&
                    (smsInboxCursor.getString(inboxBody)).equals(smsCursor.getString(indexBody)) )
            {
                //if the body and address match each other, then print SMS From:
                String str = "SMS From: " + smsCursor.getString(indexAddress) + "\n" + smsCursor.getString(indexBody) + "\n";
                if(phoneNum.equals(smsCursor.getString(inboxAddress)))
                    arrayAdapter.add(str);
            }
            else
            {
                //if that aint true tho print SMS to:
                String str = "SMS To: " + smsCursor.getString(indexAddress) + "\n" + smsCursor.getString(indexBody) + "\n";
                if(phoneNum.equals(smsCursor.getString(inboxAddress)))
                    arrayAdapter.add(str);
            }


        } // go through each message and display each one
        while (smsCursor.moveToNext() && smsInboxCursor.moveToNext() && phoneNum.equals(smsCursor.getString(indexAddress)));
    }

    // Function to format send button and what it will do
    public void onSendClick(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToSendSMS(); // if permission is not already granted
        } else {
            // parse out text
            smsManager.sendTextMessage(phoneNum, null, messageInput.getText().toString(), null, null);
            refreshSmsInbox();
            // send confirmation text
            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
            // clear input field
            messageInput.setText("");
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


}