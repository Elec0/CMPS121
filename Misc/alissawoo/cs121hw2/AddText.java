package com.example.alissawoo.cs121hw2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.EditText;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Date;


import android.support.v4.content.ContextCompat;



import android.view.View;
import android.content.Intent;


public class AddText extends AppCompatActivity {

    public JSONObject jo = null;
    public JSONArray ja = null;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        // Start up the Location Service
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);



        final EditText first = findViewById(R.id.editText);
        final EditText second = findViewById(R.id.editText2);
//        final EditText third = findViewById(R.id.editText3);
//        final EditText fourth = findViewById(R.id.editText4);
//        final EditText fifth = findViewById(R.id.editText5);



//

        Button b = findViewById(R.id.button);

        // Read the file


        try{
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
                jo = new JSONObject(j);
                ja = jo.getJSONArray("data");
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        catch(IOException e){
            // Here, initialize a new JSONObject
            jo = new JSONObject();
            ja = new JSONArray();
            try{
                jo.put("data", ja);
            }
            catch(JSONException j){
                j.printStackTrace();
            }
        }

        b.setOnClickListener(new Button.OnClickListener(){
            @SuppressLint("MissingPermission")
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat newDate = new SimpleDateFormat("MM-dd-yyyy");
                String setDate = newDate.format(cal.getTime());

                SimpleDateFormat newTime = new SimpleDateFormat("HH:mm:ss");
                String setTime = newTime.format(cal.getTime());

//                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                List<String> providers = lm.getProviders(true);
//                Location l;
//                // Go through the location providers starting with GPS, stop as soon
//                // as we find one.
//                for (int k = providers.size() - 1; k >= 0; k--) {
//                    l = lm.getLastKnownLocation(providers.get(k));
//                    String coord = (l.getLatitude()+ ", "+ l.getLongitude());
//                    fifth.setText(coord);
//                }



//                third.setText(setDate);
//                fourth.setText(setTime);

                String title = first.getText().toString();
                String description = second.getText().toString();

//                String date = third.getText().toString();
//                String time = fourth.getText().toString();
//                String gps = fifth.getText().toString();


                        JSONObject temp = new JSONObject();
                        try {
                        temp.put("first", title);
                        temp.put("second", description);

                        temp.put("third", setDate);
                        temp.put("fourth", setTime);
                        temp.put("fifth", "gps");
//                        temp.put("fifth",l.getLatitude()+ ", "+ l.getLongitude());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    ja.put(temp);

                        // write the file
                        try {
                            File f = new File(getFilesDir(), "file.ser");
                            FileOutputStream fo = new FileOutputStream(f);
                            ObjectOutputStream o = new ObjectOutputStream(fo);
                            String j = jo.toString();
                            o.writeObject(j);
                            o.close();
                            fo.close();
                        } catch (IOException e) {

                        }

                        //pop the activity off the stack
                        Intent i = new Intent(AddText.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }

        });

    }


}
