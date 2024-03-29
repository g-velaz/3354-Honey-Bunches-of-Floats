package com.example.smsmessenger;

import android.Manifest;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> smsMessagesList = new ArrayList<>(); // to list out messages
    ListView messages;
    ArrayAdapter arrayAdapter; // array to show messages

    Toolbar toolbar; // toolbar on screen header
    SearchView mySearchView; //search view

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int SEND_SMS_PERMISSIONS_REQUEST =1;

    SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) { //first10charofobject
        //arrayadapeter.getitem.tostring compare to typed in text field!

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //will set up the default toolbar
        getSupportActionBar().setTitle("SMS Messenger"); //set title of toolbar
        messages = findViewById(R.id.messages); // select box where array is going to be displayed
        
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList); // initialize array adapter to display text
        ImageView newMessageBtn = findViewById(R.id.newMessage);
        messages.setAdapter(arrayAdapter); // put array in messages box
       // mySearchView = (mySearchView)findViewById(R.id.searchView);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS(); // get permission if permission is not already granted
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


        newMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //Allows me to navigate from main to add card
        startActivity(new Intent(MainActivity.this, SingleConversationActivity.class));
        //this is to implement after the fact bc that part doesnt exist yet
        //intent.putExtra("sender", "address");
        //intent.putExtra("prev", "bodyText");
        //MainActivity.this.startActivityForResult(intent, 100);
            }
        });

    }

//    // Function to format send button and what it will do
//    public void onSendClick(View view) {
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            getPermissionToSendSMS(); // if permission is not already granted
//        } else {
//            // parse out text
//            //TODO: transfer this information and send to the user and print out to the user
//            smsManager.sendTextMessage("+1 2147999923", null, input.getText().toString(), null, null);
//            // send confirmation text
//            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
//            // clear input field
//            input.setText("");
//        }
//    }
    
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
            String str = smsInboxCursor.getString(indexAddress) + "\n\n";
            // if message is too long, abbreviate
            if(smsInboxCursor.getString(indexBody).length() >= 35)
                str += smsInboxCursor.getString(indexBody).substring(0,34) + "...\n";
            // if message is not too long, output entire message
            else
                str += smsInboxCursor.getString(indexBody) + "\n";

            // to compare the phone numbers
            Object newString = str;
            boolean compareString = compareStringToAdapter(newString); // comparing new string to string in array adapter

            // only adding most recent text
            if(compareString)
                arrayAdapter.add(str);

        } while (smsInboxCursor.moveToNext()); // go through each message and display each one
    }

    // Comparing string to what is already in the array adapter
    public boolean compareStringToAdapter(Object comp) {
        String tempComp = comp.toString();
        String arrayString;

        // comparing first 11 characters to the first 11 characters in each object of array adapter
        for(int i = 0; i < arrayAdapter.getCount();i++) {
            arrayString = arrayAdapter.getItem(i).toString();
            if(tempComp.substring(0,10).equals(arrayString.substring(0,10)))
                return false;
        }
        return true;
    }

    // Gets permissions to read messages from inbox
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSIONS_REQUEST);
        }
    }


    // Gets permissions to send messages
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu){ //this will create our menu
        getMenuInflater().inflate(R.menu.options_menu, menu); //creates menu/inflates menu to add items to action bar

       // SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.search_bar);//menu item is the search bar

        SearchView searchView = (SearchView) menuItem.getActionView(); //will show UI, the search view, from the search bar

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query){
                return false;
            }

             @Override
             public boolean onQueryTextChange(String newText)  {
                 arrayAdapter.getFilter().filter(newText); //return true if new text matches
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        String msg=" ";
        if (item.getItemId() == R.id.search_bar){ //send message if search clicked
            msg = "Search";
        }
        Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

}
