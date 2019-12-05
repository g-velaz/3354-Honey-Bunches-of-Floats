package com.example.smsmessenger;

import android.view.MenuItem;
import android.widget.Toast;
/*
@param item represents the MenuItem object being passed through
@exception IllegalArgumentException is an exception raisesd when object item's id
is not equal to the search bar id, therefor the search bar cannot be used
 */
public class Test {
    public boolean onOptionsItemSelected(MenuItem item){
        String msg=" ";
        if (item.getItemId() == R.id.search_bar){ //send message if search clicked
            msg = "Search";
        }
        else
            throw newIllegalArgumentException("")
        Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }
}
