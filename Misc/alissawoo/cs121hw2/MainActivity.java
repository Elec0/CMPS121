package com.example.alissawoo.cs121hw2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public JSONObject jos = null;
    public JSONArray ja = null;
    private static final String TAG = "JSON_LIST";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
    protected void onResume(){
        super.onResume();
        GridView grid = findViewById(R.id.gridview);
//        ListView list = findViewById(R.id.data_list_view);
        TextView text = findViewById(R.id.text);
        text.setVisibility(View.INVISIBLE);

        Log.d(TAG, ""+getFilesDir());

        jos = null;
        try{
            // Reading a file that already exists
            File f = new File(getFilesDir(), "file.ser");
            FileInputStream fi = new FileInputStream(f);
            ObjectInputStream o = new ObjectInputStream(fi);
            // Notice here that we are de-serializing a String object (instead of
            // a JSONObject object) and passing the String to the JSONObject’s
            // constructor. That’s because String is serializable and
            // JSONObject is not. To convert a JSONObject back to a String, simply
            // call the JSONObject’s toString method.
            String j = null;
            try{
                j = (String) o.readObject();
            }
            catch(ClassNotFoundException c){
                c.printStackTrace();
            }
            try {
                jos = new JSONObject(j);
                ja = jos.getJSONArray("data");
            }
            catch(JSONException e){
                e.printStackTrace();
            }

            // Show the list
            final ArrayList<ListData> aList = new ArrayList<ListData>();
            for(int i = 0; i < ja.length(); i++){

                ListData ld = new ListData();
                try {
                    ld.title = ja.getJSONObject(i).getString("first");
                    ld.description = ja.getJSONObject(i).getString("second");
                    ld.date = ja.getJSONObject(i).getString("third");
                    ld.time = ja.getJSONObject(i).getString("fourth");
                    ld.gps = ja.getJSONObject(i).getString("fifth");



                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                aList.add(ld);
            }

            // Create an array and assign each element to be the title
            // field of each of the ListData objects (from the array list)
            String[] listItems = new String[aList.size()];

            for(int i = 0; i < aList.size(); i++){
                ListData listD = aList.get(i);
                listItems[i] = listD.title;
            }

            // Show the list view with the each list item an element from listItems
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
//            list.setAdapter(adapter);
//            grid.setAdapter(adapter);
            final ImageAdapter imageAdapter = new ImageAdapter(this);
            grid.setAdapter(new ImageAdapter(this));

            // Set an OnItemClickListener for each of the list items
            final Context context = this;
//            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {


                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        ListData selected = aList.get(position);

                        // Create an Intent to reference our new activity, then call startActivity
                        // to transition into the new Activity.
                        Intent detailIntent = new Intent(context, DetailActivity.class);

                        // pass some key value pairs to the next Activity (via the Intent)
                        detailIntent.putExtra("first", selected.title);
                        detailIntent.putExtra("second", selected.description);
                        detailIntent.putExtra("third", selected.date);
                        detailIntent.putExtra("fourth", selected.time);
                        detailIntent.putExtra("fifth", selected.gps);

                        detailIntent.putExtra("index",position);

                        startActivity(detailIntent);

                    } catch (Exception e){
                        return;
                    }

                }

            });
        }
        catch(IOException e){
            // There's no JSON file that exists, so don't
            // show the list. But also don't worry about creating
            // the file just yet, that takes place in AddText.

            //Here, disable the list view
//            list.setEnabled(false);
//            list.setVisibility(View.INVISIBLE);
            grid.setEnabled(false);
            grid.setVisibility(View.INVISIBLE);

            //show the text view
            text.setVisibility(View.VISIBLE);
        }

        if (ja.length() == 0) {
//            list.setEnabled(false);
//            list.setVisibility(View.INVISIBLE);
            grid.setEnabled(false);
            grid.setVisibility(View.INVISIBLE);

            text.setVisibility(View.VISIBLE);
        }

    }

    // This method will just show the menu item (which is our button "ADD")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        // the menu being referenced here is the menu.xml from res/menu/menu.xml
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    /* Here is the event handler for the menu button that I forgot in class.
    The value returned by item.getItemID() is
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, String.format("" + item.getItemId()));
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_favorite:
                /*the R.id.action_favorite is the ID of our button (defined in strings.xml).
                Change Activity here (if that's what you're intending to do, which is probably is).
                 */
                Intent i = new Intent(this, AddText.class);
                startActivity(i);
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

}
